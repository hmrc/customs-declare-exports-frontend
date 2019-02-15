/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.supplementary

import config.AppConfig
import connectors.{CustomsDeclarationsConnector, CustomsDeclareExportsConnector}
import controllers.actions.AuthAction
import controllers.util.CacheIdGenerator.supplementaryCacheId
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.declaration.supplementary.SupplementaryDeclarationData
import models.{CustomsDeclarationsResponse, Submission}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.{CustomsCacheService, NRSService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.MetaData
import views.html.supplementary.summary.{summary_page, summary_page_no_data}

import scala.concurrent.{ExecutionContext, Future}

class SummaryPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  customsDeclarationConnector: CustomsDeclarationsConnector,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  exportsMetrics: ExportsMetrics,
  nrsService: NRSService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetch(supplementaryCacheId).map {
      case Some(cacheMap) => Ok(summary_page(appConfig, SupplementaryDeclarationData(cacheMap)))
      case None           => Ok(summary_page_no_data(appConfig))
    }
  }

  //scalastyle:off method.length
  def submitSupplementaryDeclaration(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetch(supplementaryCacheId).flatMap {
      case Some(cacheMap) =>
        exportsMetrics.startTimer(submissionMetric)
        val suppDecData = SupplementaryDeclarationData(cacheMap)
        val metaData = MetaData.fromProperties(suppDecData.toMetadataProperties())

        customsDeclarationConnector.submitExportDeclaration(metaData).flatMap {
          case CustomsDeclarationsResponse(status, Some(conversationId)) =>
            val ducr = suppDecData.consignmentReferences.flatMap(_.ducr)
            val lrn = suppDecData.consignmentReferences.map(_.lrn)
            val declarationStatus = status match {
              case ACCEPTED => models.Accepted
              case _ => models.Rejected
            }

            val submission = new Submission(
              request.user.eori,
              conversationId,
              ducr.fold("")(_.ducr),
              lrn,
              None,
              declarationStatus
            )

            customsDeclareExportsConnector
              .saveSubmissionResponse(submission)
              .flatMap { _ =>
                exportsMetrics.incrementCounter(submissionMetric)
                customsCacheService
                  .remove(supplementaryCacheId)
                  .map(
                    _ =>
                    status match {
                      case ACCEPTED =>
                        Redirect(controllers.supplementary.routes.ConfirmationPageController.displayPage())
                          .flashing(prepareFlashScope(lrn.getOrElse(""), conversationId))
                      case _ =>
                        Redirect(controllers.supplementary.routes.ConfirmationPageController.displayRejectionPage())
                    }
                  )
              }
              .recover {
                case error: Throwable =>
                  exportsMetrics.incrementCounter(submissionMetric)
                  handleError(s"Error from Customs Declare Exports ${error.toString}")
              }

          case error =>
            Future.successful(handleError(s"Error from Customs Declarations API ${error.toString}"))
        }

      case None =>
        Future.successful(handleError(s"Could not obtain data from DB"))
    }
  }
  //scalastyle:on method.length

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    Logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = messagesApi("global.error.title"),
        heading = messagesApi("global.error.heading"),
        message = messagesApi("global.error.message")
      )
    )
  }

  private def prepareFlashScope(lrn: String, conversationId: String) =
    Flash(Map("LRN" -> lrn, "ConversationId" -> conversationId))

}
