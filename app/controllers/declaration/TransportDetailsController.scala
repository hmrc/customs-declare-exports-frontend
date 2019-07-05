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
import controllers.declaration.routes._
import controllers.util.CacheIdGenerator.cacheId
import forms.Choice.AllowedChoiceValues
import forms.declaration.TransportDetails
import forms.declaration.TransportDetails._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.transport_details

import scala.concurrent.{ExecutionContext, Future}

class TransportDetailsController @Inject()(
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyAction).async { implicit request =>
    customsCacheService
      .fetchAndGetEntry[TransportDetails](cacheId, TransportDetails.formId)
      .map(data => Ok(transport_details(data.fold(form)(form.fill(_)))))
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyAction).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[TransportDetails]) => Future.successful(BadRequest(transport_details(formWithErrors))),
        transportDetails =>
          for {
            _ <- updateCache(journeySessionId, transportDetails)
            _ <- customsCacheService.cache[TransportDetails](cacheId, TransportDetails.formId, transportDetails)
          } yield redirect(transportDetails)
      )
  }

  private def redirect(transportDetails: TransportDetails)(implicit request: JourneyRequest[_]): Result =
    if (transportDetails.container) Redirect(TransportContainerController.displayPage())
    else if (request.choice.value == AllowedChoiceValues.StandardDec) Redirect(SealController.displayForm())
    else Redirect(SummaryPageController.displayPage())

  private def updateCache(sessionId: String, formData: TransportDetails): Future[Either[String, ExportsCacheModel]] =
    getAndUpdateExportCacheModel(
      sessionId,
      model => exportsCacheService.update(sessionId, model.copy(transportDetails = Some(formData)))
    )
}
