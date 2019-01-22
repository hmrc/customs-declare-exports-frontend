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
import controllers.actions.AuthAction
import forms.supplementary.TransactionType
import forms.supplementary.TransactionType.{form, formId}
import services.CustomsCacheService
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.transaction_type

import scala.concurrent.{ExecutionContext, Future}

class TransactionTypeController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[TransactionType](appConfig.appName, formId).map {
      case Some(data) => Ok(transaction_type(appConfig, form.fill(data)))
      case _          => Ok(transaction_type(appConfig, form))
    }
  }

  def saveTransactionType(): Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest
      .fold(
        (formWithErrors: Form[TransactionType]) =>
          Future.successful(BadRequest(transaction_type(appConfig, formWithErrors))),
        form =>
          customsCacheService.cache[TransactionType](appConfig.appName, formId, form).map { _ =>
            Redirect(controllers.supplementary.routes.GoodItemNumberController.displayForm())
        }
      )
  }
}
