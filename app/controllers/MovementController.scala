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
import forms.{ChoiceForm, EnterDucrForm}
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.{choice_page, enterDUCR, movement, movement_confirmation_page}

import scala.concurrent.Future

class MovementController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

  val choiceForm = Form(ChoiceForm.choiceMapping)
  val choiceId = "Choice"

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[ChoiceForm](appConfig.appName, choiceId).map {
      case Some(data) => Ok(choice_page(appConfig, choiceForm.fill(data)))
      case _          => Ok(choice_page(appConfig, choiceForm))
    }
  }

  def sendChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    choiceForm.bindFromRequest().fold(
      (formWithErrors: Form[ChoiceForm]) =>
        Future.successful(BadRequest(choice_page(appConfig, formWithErrors))),
      form => {
        val mapping = MovementRequestMappingProvider.buildMapping(form.choice)

        customsCacheService.cache[ChoiceForm](appConfig.appName, choiceId, form).map { _ =>
          Ok(movement(appConfig, Form(mapping), form.choice))
        }
      }
    )
  }

  // TODO remove movementType as argument and start using mongo and retrieving data from database
  def sendMovement(movementType: String): Action[AnyContent] = authenticate.async { implicit request =>
    val movementForm = Form(MovementRequestMappingProvider.buildMapping(movementType))

    movementForm.bindFromRequest().fold(
      (formWithErrors: Form[InventoryLinkingMovementRequest]) =>
        Future.successful(BadRequest(movement(appConfig, formWithErrors, movementType))),
      form => {
        val eori = request.user.eori

        val validForm = form.copy(messageCode = form.messageCode)
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

  val enterDucrForm = Form(EnterDucrForm.ducrMapping)
  val enterDucrId = "EnterDucr"

  // TODO on DUCR page you can have arrive or depart, please add handling for this
  def displayDucrPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[EnterDucrForm](appConfig.appName, enterDucrId).map {
      case Some(data) => Ok(enterDUCR(appConfig, enterDucrForm.fill(data)))
      case _          => Ok(enterDUCR(appConfig, enterDucrForm))
    }
  }

  def saveDucr(): Action[AnyContent] = authenticate.async { implicit request =>
    enterDucrForm.bindFromRequest().fold(
      (formWithErrors: Form[EnterDucrForm]) =>
        Future.successful(BadRequest(enterDUCR(appConfig, formWithErrors))),
      form => {
        customsCacheService.cache[EnterDucrForm](appConfig.appName, enterDucrId, form).map { _ =>
          Ok("DONE")
        }
      }
    )
  }
}
