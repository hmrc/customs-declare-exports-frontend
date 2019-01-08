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

package controllers

import config.AppConfig
import controllers.actions.AuthAction
import forms.Choice
import forms.Choice._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

class ChoiceController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[Choice](appConfig.appName, choiceId).map {
      case Some(data) => Ok(choice_page(appConfig, Choice.form().fill(data)))
      case _          => Ok(choice_page(appConfig, Choice.form()))
    }
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    Choice
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Choice]) => Future.successful(BadRequest(choice_page(appConfig, formWithErrors))),
        validChoice =>
          customsCacheService.cache[Choice](appConfig.appName, choiceId, validChoice).map { _ =>
            validChoice.choice match {
              case AllowedChoiceValues.SimplifiedDec =>
                Redirect(controllers.routes.RoleController.displayRolePage())
              case _ => Redirect(controllers.movement.routes.MovementController.displayDucrPage())
            }
        }
      )
  }

}
