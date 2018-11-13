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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.{departure_confirmation_page, departures}

import scala.concurrent.Future

class DeparturesController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

  val departuresForm = Form(MovementRequestMappingProvider.provideMappingForDeparture())

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    Future.successful(Ok(departures(appConfig, departuresForm)))
  }

  def submitForm(): Action[AnyContent] = authenticate.async { implicit request =>
    departuresForm.bindFromRequest().fold(
      (formWithErrors: Form[InventoryLinkingMovementRequest]) =>
        Future.successful(BadRequest(departures(appConfig, formWithErrors))),
      validForm => {
        val eori = request.user.eori
        customsInventoryLinkingExportsConnector.sendArrival(eori, validForm.toXml).map {
          case accepted if accepted.status == ACCEPTED =>
            Ok(departure_confirmation_page(appConfig, validForm.ucrBlock.ucr))
          case error =>
            Logger.error(s"There was an error during departure form submission: ${error.toString}")
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
