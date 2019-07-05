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

package controllers.declaration

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.cacheId
import forms.declaration.BorderTransport
import forms.declaration.BorderTransport._
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.border_transport

import scala.concurrent.{ExecutionContext, Future}

class BorderTransportController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  borderTransportPage: border_transport
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[BorderTransport](cacheId, BorderTransport.formId)
      .map(data => Ok(borderTransportPage(data.fold(form)(form.fill(_)))))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[BorderTransport]) => Future.successful(BadRequest(borderTransportPage(formWithErrors))),
        borderTransport =>
          for {
            _ <- updateCache(journeySessionId, borderTransport)
            _ <- customsCacheService.cache[BorderTransport](cacheId, BorderTransport.formId, borderTransport)
          } yield Redirect(routes.TransportDetailsController.displayForm())
      )
  }

  private def updateCache(sessionId: String, formData: BorderTransport): Future[Either[String, ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(borderTransport = Some(formData)))
    )
}
