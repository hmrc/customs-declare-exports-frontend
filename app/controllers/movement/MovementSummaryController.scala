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

package controllers.movement

import java.util.UUID

import config.AppConfig
import connectors.{CustomsDeclareExportsConnector, CustomsDeclareExportsMovementsConnector, CustomsInventoryLinkingExportsConnector}
import controllers.actions.AuthAction
import controllers.util.CacheIdGenerator.movementCacheId
import forms.inventorylinking.MovementRequestSummaryMappingProvider
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.{ExportsMetrics, MetricIdentifiers}
import models.MovementSubmission
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.movement.{movement_confirmation_page, movement_summary_page}

import scala.concurrent.{ExecutionContext, Future}

class MovementSummaryController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  customsDeclareExportsMovementsConnector: CustomsDeclareExportsMovementsConnector,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  exportsMetrics: ExportsMetrics
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displaySummary(): Action[AnyContent] = authenticate.async { implicit request =>
    val form = Form(MovementRequestSummaryMappingProvider.provideMappingForMovementSummaryPage())

    customsCacheService.fetchMovementRequest(movementCacheId, request.user.eori).map {
      case Some(data) => Ok(movement_summary_page(appConfig, form.fill(data)))
      case _          => handleError(s"Could not obtain data from DB")
    }
  }

  def submitMovementRequest(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchMovementRequest(movementCacheId, request.user.eori).flatMap {
      case Some(data) => {
        val eoriVal = request.user.eori
        val ducrVal = ""
        val mucrVal = data.masterUCR
        val movementType = "EAL"

        val metricIdentifier = getMetricIdentifierFrom(data)
        exportsMetrics.startTimer(metricIdentifier)

        customsInventoryLinkingExportsConnector
          .sendMovementRequest(movementCacheId, data.toXml)
          .flatMap {
            case response if response.status == ACCEPTED => {
              val conversationId = UUID.randomUUID().toString
              val movementSubmission = MovementSubmission(eoriVal, conversationId, ducrVal, mucrVal, movementType)

              customsDeclareExportsMovementsConnector
                .saveMovementSubmission(movementSubmission)
                .map(
                  saveResponse =>
                    saveResponse.status match {
                      case OK =>
                        exportsMetrics.incrementCounter(metricIdentifier)
                        Redirect(controllers.movement.routes.MovementSummaryController.displayConfirmation())
                      case _ => handleError(s"Unable to save data")
                  }
                )
            }
            case _ => Future.successful(handleError(s"Could not obtain data from DB"))

          }
          .recover {
            case error: Throwable =>
              exportsMetrics.incrementCounter(metricIdentifier)
              Logger.error("Error from Customs Inventory Linking", error)
              handleError(s"Error from Customs Inventory Linking ${error.toString}")
          }
      }
      case _ => Future.successful(handleError(s"Could not obtain data from DB"))
    }
  }

  def displayConfirmation(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchMovementRequest(movementCacheId, request.user.eori).flatMap {
      case Some(data) =>
        customsCacheService.remove(movementCacheId).map { _ =>
          Ok(movement_confirmation_page(appConfig, data.messageCode, data.ucrBlock.ucr))
        }
      case _ =>
        Future.successful(handleError(s"Could not obtain data from DB"))
    }
  }

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

  private def getMetricIdentifierFrom(movementData: InventoryLinkingMovementRequest): String =
    movementData.messageCode match {
      case "EAL" => MetricIdentifiers.arrivalMetric
      case "EDL" => MetricIdentifiers.departureMetric
    }

}
