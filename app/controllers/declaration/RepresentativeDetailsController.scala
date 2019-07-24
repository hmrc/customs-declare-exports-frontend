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
import forms.Choice.AllowedChoiceValues._
import forms.declaration.RepresentativeDetails
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CustomsCacheService
import services.cache.{ExportsCacheModel, ExportsCacheService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.representative_details

import scala.concurrent.{ExecutionContext, Future}

class RepresentativeDetailsController @Inject()(
  appConfig: AppConfig,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  representativeDetailsPage: representative_details
)(implicit ec: ExecutionContext)
    extends {
  val cacheService = exportsCacheService
} with FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayRepresentativeDetailsPage(): Action[AnyContent] = (authenticate andThen journeyType).async {
    implicit request =>
      exportsCacheService.get(journeySessionId).map(_.flatMap(_.parties.representativeDetails)).map {
        case Some(data) => Ok(representativeDetailsPage(appConfig, RepresentativeDetails.form().fill(data)))
        case _          => Ok(representativeDetailsPage(appConfig, RepresentativeDetails.form()))
      }
  }

  def submitRepresentativeDetails(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    RepresentativeDetails.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RepresentativeDetails]) =>
          Future.successful(
            BadRequest(representativeDetailsPage(appConfig, RepresentativeDetails.adjustErrors(formWithErrors)))
        ),
        validRepresentativeDetails =>
          for {
            _ <- updateCache(journeySessionId, validRepresentativeDetails)
            _ <- customsCacheService
              .cache[RepresentativeDetails](cacheId, RepresentativeDetails.formId, validRepresentativeDetails)
          } yield Redirect(nextPage(request))
      )
  }

  private def nextPage(request: JourneyRequest[AnyContent]) =
    request.choice.value match {
      case SupplementaryDec =>
        controllers.declaration.routes.DeclarationAdditionalActorsController.displayForm()
      case StandardDec =>
        controllers.declaration.routes.CarrierDetailsController.displayForm()
    }

  private def updateCache(sessionId: String, formData: RepresentativeDetails): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(sessionId, model => {
      val updatedParties = model.parties.copy(representativeDetails = Some(formData))
      exportsCacheService.update(sessionId, model.copy(parties = updatedParties))
    })
}
