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
import forms.inventorylinking.{MovementChoiceForm, MovementRequestMappingProvider}
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.{choose_movement, movement, movement_confirmation_page}

import scala.concurrent.Future

class MovementController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

  val choiceForm = Form(MovementChoiceForm.movementChoiceMapping)

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    Future.successful(Ok(choose_movement(appConfig, choiceForm)))
  }

  def sendChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    // TODO add saving choice and redirect to movement form
    choiceForm.bindFromRequest().fold(
      (formWithErrors: Form[MovementChoiceForm]) =>
        Future.successful(BadRequest(choose_movement(appConfig, formWithErrors))),
      validForm => {
        val mapping = MovementRequestMappingProvider.buildMapping(validForm.movement)
        Future.successful(Ok(movement(appConfig, Form(mapping), MovementRequestMappingProvider.convertToMessageCode(validForm.movement))))
      }
    )
  }

  // TODO remove movementType as argument and start using mongo and retrieving data from database
  def sendMovement(movementType: String): Action[AnyContent] = authenticate.async { implicit request =>
    val movementForm = Form(MovementRequestMappingProvider.buildMapping(movementType))
    val messageCode = MovementRequestMappingProvider.convertToMessageCode(movementType)

    movementForm.bindFromRequest().fold(
      (formWithErrors: Form[InventoryLinkingMovementRequest]) =>
        Future.successful(BadRequest(movement(appConfig, formWithErrors, messageCode))),
      form => {
        val eori = request.user.eori
        val validForm = form.copy(messageCode = MovementRequestMappingProvider.convertToMessageCode(form.messageCode))
        customsInventoryLinkingExportsConnector.sendMovementRequest(eori, validForm.toXml).map {
          case accepted if accepted.status == ACCEPTED =>
            Ok(movement_confirmation_page(appConfig, movementType, validForm.ucrBlock.ucr))
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
