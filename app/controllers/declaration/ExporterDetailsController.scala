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
import controllers.navigation.Navigator
import forms.declaration.ExporterDetails
import javax.inject.Inject
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.exporter_details

import scala.concurrent.{ExecutionContext, Future}

class ExporterDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  exporterDetailsPage: exporter_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayForm(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.exporterDetails match {
      case Some(data) => Ok(exporterDetailsPage(ExporterDetails.form().fill(data)))
      case _          => Ok(exporterDetailsPage(ExporterDetails.form()))
    }
  }

  def saveAddress(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    ExporterDetails
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ExporterDetails]) => Future.successful(BadRequest(exporterDetailsPage(formWithErrors))),
        form =>
          updateCache(form)
            .map(_ => navigator.continueTo(controllers.declaration.routes.ConsigneeDetailsController.displayPage()))
      )
  }

  private def updateCache(
    formData: ExporterDetails
  )(implicit r: JourneyRequest[_]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect(model => {
      val updatedParties = model.parties.copy(exporterDetails = Some(formData))
      model.copy(parties = updatedParties)
    })
}
