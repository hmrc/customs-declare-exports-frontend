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
import forms.supplementary.PreviousDocuments
import forms.supplementary.PreviousDocuments._
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.previous_documents

import scala.concurrent.{ExecutionContext, Future}

class PreviousDocumentsController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[PreviousDocuments](appConfig.appName, formId).map {
      case Some(data) => Ok(previous_documents(appConfig, form.fill(data)))
      case _          => Ok(previous_documents(appConfig, form))
    }
  }

  def savePreviousDocuments(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[PreviousDocuments]) =>
          Future.successful(BadRequest(previous_documents(appConfig, formWithErrors))),
        validForm =>
          customsCacheService.cache[PreviousDocuments](appConfig.appName, formId, validForm).map { _ =>
            Redirect(controllers.supplementary.routes.GoodsItemNumberController.displayForm())
        }
      )
  }
}
