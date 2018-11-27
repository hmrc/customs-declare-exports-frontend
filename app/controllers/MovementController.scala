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
import forms.MovementFormsAndIds._
import forms._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html._

import scala.concurrent.Future

class  MovementController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsInventoryLinkingExportsConnector: CustomsInventoryLinkingExportsConnector,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler
) extends FrontendController with I18nSupport {

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
      form =>
        customsCacheService.cache[ChoiceForm](appConfig.appName, choiceId, form).map { _ =>
          Redirect(controllers.routes.MovementController.displayDucrPage())
        }
    )
  }

  def displayDucrPage(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[ChoiceForm](appConfig.appName, choiceId).flatMap {
      case Some(choice) if !choice.choice.isEmpty =>
        customsCacheService.fetchAndGetEntry[EnterDucrForm](appConfig.appName, enterDucrId).map{
          case Some(data) => Ok(enterDUCR(appConfig, enterDucrForm.fill(data), choice.choice))
          case _          => Ok(enterDUCR(appConfig, enterDucrForm, choice.choice))
        }
      case _ =>
        Future.successful(
          BadRequest(
            errorHandler.standardErrorTemplate(
              pageTitle = messagesApi("global.error.title"),
              heading = messagesApi("global.error.heading"),
              message = messagesApi("global.error.message")
            )
          )
        )
    }
  }

  def saveDucr(): Action[AnyContent] = authenticate.async { implicit request =>
    enterDucrForm.bindFromRequest().fold(
      (formWithErrors: Form[EnterDucrForm]) =>
        Future.successful(BadRequest(enterDUCR(appConfig, formWithErrors, ""))),
      form =>
        customsCacheService.cache[EnterDucrForm](appConfig.appName, enterDucrId, form).map { _ =>
          Redirect(controllers.routes.MovementController.displayGoodsDate())
      }
    )
  }

  def displayGoodsDate(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[ChoiceForm](appConfig.appName, choiceId).flatMap {
      case Some(choice) if !choice.choice.isEmpty =>
        customsCacheService.fetchAndGetEntry[GoodsDateForm](appConfig.appName, goodsDateId).map{
          case Some(data) => Ok(goods_date(appConfig, goodsDateForm.fill(data), choice.choice))
          case _          => Ok(goods_date(appConfig, goodsDateForm, choice.choice))
        }
      case _ =>
        Future.successful(
          BadRequest(
            errorHandler.standardErrorTemplate(
              pageTitle = messagesApi("global.error.title"),
              heading = messagesApi("global.error.heading"),
              message = messagesApi("global.error.message")
            )
          )
        )
    }
  }

  def saveGoodsDate(): Action[AnyContent] = authenticate.async { implicit request =>
    goodsDateForm.bindFromRequest().fold(
      (formWithErrors: Form[GoodsDateForm]) =>
        Future.successful(BadRequest(goods_date(appConfig, formWithErrors, ""))),
      form =>
        customsCacheService.cache[GoodsDateForm](appConfig.appName, goodsDateId, form).map { _ =>
          Redirect(controllers.routes.MovementController.displayLocation())
        }
    )
  }

  def displayLocation(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[ChoiceForm](appConfig.appName, choiceId).flatMap {
      case Some(choice) if !choice.choice.isEmpty =>
        customsCacheService.fetchAndGetEntry[LocationForm](appConfig.appName, locationId).map {
          case Some(data) => Ok(goods_location(appConfig, locationForm.fill(data), choice.choice))
          case _          => Ok(goods_location(appConfig, locationForm, choice.choice))
        }
      case _ =>
        Future.successful(
          BadRequest(
            errorHandler.standardErrorTemplate(
              pageTitle = messagesApi("global.error.title"),
              heading = messagesApi("global.error.heading"),
              message = messagesApi("global.error.message")
            )
          )
        )
    }
  }

  def saveLocation(): Action[AnyContent] = authenticate.async { implicit request =>
    locationForm.bindFromRequest().fold(
      (formWithErrors: Form[LocationForm]) =>
        customsCacheService.fetchAndGetEntry[ChoiceForm](appConfig.appName, choiceId).map {
          case Some(choice) => BadRequest(goods_location(appConfig, formWithErrors, choice.choice))
          case _ => BadRequest(goods_location(appConfig, formWithErrors, "EAL"))
        },
      form =>
        customsCacheService.cache[LocationForm](appConfig.appName, locationId, form).map { _ =>
          Redirect(controllers.routes.MovementController.displayTransport())
        }
    )
  }

  def displayTransport(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[TransportForm](appConfig.appName, transportId).map {
      case Some(data) => Ok(transport(appConfig, transportForm.fill(data)))
      case _          => Ok(transport(appConfig, transportForm))
    }
  }

  def saveTransport(): Action[AnyContent] = authenticate.async { implicit request =>
    transportForm.bindFromRequest().fold(
      (formWithErrors: Form[TransportForm]) =>
        Future.successful(BadRequest(transport(appConfig, formWithErrors))),
      form =>
        customsCacheService.cache[TransportForm](appConfig.appName, transportId, form).map { _ =>
          Redirect(controllers.routes.MovementSummaryController.displaySummary())
        }
    )
  }
}
