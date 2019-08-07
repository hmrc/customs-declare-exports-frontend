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

import controllers.actions.{AuthAction, JourneyAction}
import forms.declaration.DispatchLocation
import forms.declaration.DispatchLocation.AllowedDispatchLocations
import javax.inject.Inject
import models.ExportsCacheModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.dispatch_location

import scala.concurrent.{ExecutionContext, Future}

class DispatchLocationController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  dispatchLocationPage: dispatch_location
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SessionIdAware {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    exportsCacheService.get(journeySessionId).map(_.flatMap(_.dispatchLocation)).map {
      case Some(data) => Ok(dispatchLocationPage(DispatchLocation.form().fill(data)))
      case _          => Ok(dispatchLocationPage(DispatchLocation.form()))
    }
  }

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    DispatchLocation
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DispatchLocation]) => Future.successful(BadRequest(dispatchLocationPage(formWithErrors))),
        validDispatchLocation =>
          updateCache(journeySessionId, validDispatchLocation)
            .map(_ => Redirect(specifyNextPage(validDispatchLocation)))
      )
  }

  private def specifyNextPage(providedDispatchLocation: DispatchLocation): Call =
    providedDispatchLocation.dispatchLocation match {
      case AllowedDispatchLocations.OutsideEU =>
        controllers.declaration.routes.AdditionalDeclarationTypeController.displayPage()
      case AllowedDispatchLocations.SpecialFiscalTerritory =>
        controllers.declaration.routes.NotEligibleController.displayPage()
    }

  private def updateCache(sessionId: String, formData: DispatchLocation): Future[Option[ExportsCacheModel]] =
    getAndUpdateExportCacheModel(sessionId, model => {
      exportsCacheService.update(sessionId, model.copy(dispatchLocation = Some(formData)))
    })

}
