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
import controllers.util.CacheIdGenerator.supplementaryCacheId
import forms.supplementary.DocumentsProduced
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.supplementary.documents_produced

import scala.concurrent.{ExecutionContext, Future}

class DocumentsProducedController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  customsCacheService: CustomsCacheService
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  import forms.supplementary.DocumentsProduced._

  def displayForm(): Action[AnyContent] = authenticate.async { implicit request =>
    customsCacheService.fetchAndGetEntry[DocumentsProduced](supplementaryCacheId, formId).map {
      case Some(data) => Ok(documents_produced(appConfig, form.fill(data)))
      case _          => Ok(documents_produced(appConfig, form))
    }
  }

  def saveForm(): Action[AnyContent] = authenticate.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DocumentsProduced]) =>
          Future.successful(BadRequest(documents_produced(appConfig, formWithErrors))),
        form =>
          customsCacheService.cache[DocumentsProduced](supplementaryCacheId, formId, form).map { _ =>
            Redirect(controllers.supplementary.routes.SummaryPageController.displayPage())
        }
      )
  }
}
