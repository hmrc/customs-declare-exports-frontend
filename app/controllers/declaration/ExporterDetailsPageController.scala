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
import forms.declaration.ExporterDetails
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.exporter_details

import scala.concurrent.{ExecutionContext, Future}

class ExporterDetailsPageController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  exporterDetailsPage: exporter_details
)(implicit ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[ExporterDetails](cacheId, ExporterDetails.id).map {
      case Some(data) => Ok(exporterDetailsPage(appConfig, ExporterDetails.form.fill(data)))
      case _          => Ok(exporterDetailsPage(appConfig, ExporterDetails.form))
    }
  }

  def saveAddress(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    ExporterDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ExporterDetails]) =>
          Future.successful(BadRequest(exporterDetailsPage(appConfig, formWithErrors))),
        form =>
          for {
            _ <- updateCache(journeySessionId, form)
            _ <- customsCacheService.cache[ExporterDetails](cacheId, ExporterDetails.id, form)
          } yield Redirect(controllers.declaration.routes.ConsigneeDetailsPageController.displayForm())
      )
  }

  private def updateCache(sessionId: String, formData: ExporterDetails): Future[Either[String, ExportsCacheModel]] =
    getAndUpdateExportCacheModel(sessionId, model => {
      exportsCacheService.update(sessionId, model.copy(exporterDetails = Some(formData)))
    })
}
