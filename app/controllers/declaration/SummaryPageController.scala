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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import forms.declaration.ConsignmentReferences
import handlers.ErrorHandler
import javax.inject.Inject
import models.declaration.SupplementaryDeclarationData
import models.requests.JourneyRequest
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.summary.{summary_page, summary_page_no_data}

import scala.concurrent.{ExecutionContext, Future}

class SummaryPageController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  submissionService: SubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  implicit val appConfigImpl: AppConfig = appConfig
  private val logger = Logger(this.getClass())

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetch(cacheId).map {
      case Some(cacheMap) if containsMandatoryData(cacheMap) => Ok(summary_page(SupplementaryDeclarationData(cacheMap)))
      case _                                                 => Ok(summary_page_no_data())
    }
  }

  private def containsMandatoryData(cacheMap: CacheMap): Boolean =
    cacheMap.getEntry[ConsignmentReferences](ConsignmentReferences.id).exists(_.lrn.nonEmpty)

  def submitSupplementaryDeclaration(): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      customsCacheService.fetch(cacheId).flatMap {
        case Some(cacheMap) => handleDecSubmission(cacheMap)
        case None           => Future.successful(handleError("Could not obtain data from DB"))
      }
  }

  private def handleDecSubmission(
    cacheMap: CacheMap
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier): Future[Result] =
    submissionService.submit(cacheMap).map {
      case Some(lrn) =>
        Redirect(controllers.declaration.routes.ConfirmationPageController.displayPage())
          .flashing(Flash(Map("LRN" -> lrn)))
      case _ => handleError(s"Error from Customs Declarations API")
    }

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = messagesApi("global.error.title"),
        heading = messagesApi("global.error.heading"),
        message = messagesApi("global.error.message")
      )
    )
  }
}
