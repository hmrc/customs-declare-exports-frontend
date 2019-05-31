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
import controllers.util.CacheIdGenerator.eoriCacheId
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.Choice._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

class ChoiceController @Inject()(
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[Choice](eoriCacheId, choiceId).map {
      case Some(data) => Ok(choice_page(Choice.form().fill(data)))
      case _          => Ok(choice_page(Choice.form()))
    }
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Choice]) => Future.successful(BadRequest(choice_page(formWithErrors))),
        validChoice =>
          customsCacheService.cache[Choice](eoriCacheId, choiceId, validChoice).map { _ =>
            validChoice.value match {
              case SupplementaryDec | StandardDec =>
                Redirect(controllers.declaration.routes.DispatchLocationPageController.displayPage())
              case CancelDec =>
                Redirect(controllers.routes.CancelDeclarationController.displayForm())
              case Submissions =>
                Redirect(controllers.routes.SubmissionsController.displayListOfSubmissions())
              case _ =>
                Redirect(controllers.routes.ChoiceController.displayChoiceForm())
            }
        }
      )
  }
}
