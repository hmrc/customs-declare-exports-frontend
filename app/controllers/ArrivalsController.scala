/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import config.AppConfig
import connectors.CustomsInventoryLinkingExportsConnector
import controllers.actions.AuthAction
import forms.inventorylinking.MovementRequestMappingProvider
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.{arrival_confirmation_page, arrivals}

import scala.concurrent.Future

class ArrivalsController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

  object ArrivalForm {
    implicit val agentFormat = Json.format[AgentDetails]
    implicit val ucrFormat = Json.format[UcrBlock]
    implicit val transportFormat = Json.format[TransportDetails]
    implicit val inventoryMovementFormat = Json.format[InventoryLinkingMovementRequest]
  }

  val form = Form(MovementRequestMappingProvider.provideMappingForArrival())

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    Future.successful(Ok(arrivals(appConfig, form)))
  }

  def send(): Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest().fold(
      (formWithErrors: Form[InventoryLinkingMovementRequest]) =>
        Future.successful(BadRequest(arrivals(appConfig, formWithErrors))),
      form => {
        val eori = request.user.eori
        customsInventoryLinkingExportsConnector.sendMovementRequest(eori, form.toXml).map {
          case accepted if accepted.status == ACCEPTED =>
            Ok(arrival_confirmation_page(appConfig, form.ucrBlock.ucr))
          case error =>
            Logger.error(s"Error from Customs Inventory Linking ${error.toString}")
            BadRequest(
              errorHandler.standardErrorTemplate(
                pageTitle = messagesApi("global.error.title"),
                heading = messagesApi("global.error.heading"),
                message = messagesApi("global.error.message")
              )
            )
        }
      }
    )
  }
}
