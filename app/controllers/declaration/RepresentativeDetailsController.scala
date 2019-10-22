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
import forms.Choice.AllowedChoiceValues._
import forms.declaration.RepresentativeDetails
import javax.inject.Inject
import models.{ExportsDeclaration, Mode}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.representative_details

import scala.concurrent.{ExecutionContext, Future}

class RepresentativeDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  representativeDetailsPage: representative_details
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.representativeDetails match {
      case Some(data) => Ok(representativeDetailsPage(mode, RepresentativeDetails.form().fill(data)))
      case _          => Ok(representativeDetailsPage(mode, RepresentativeDetails.form()))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    RepresentativeDetails
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RepresentativeDetails]) =>
          Future.successful(BadRequest(representativeDetailsPage(mode, RepresentativeDetails.adjustErrors(formWithErrors)))),
        validRepresentativeDetails => updateCache(validRepresentativeDetails).map(_ => navigator.continueTo(nextPage(mode, request)))
      )
  }

  private def nextPage(mode: Mode, request: JourneyRequest[AnyContent]) =
    request.choice.value match {
      case SupplementaryDec =>
        controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage(mode)
      case StandardDec | SimplifiedDec =>
        controllers.declaration.routes.CarrierDetailsController.displayPage(mode)
    }

  private def updateCache(formData: RepresentativeDetails)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      val updatedParties = model.parties.copy(representativeDetails = Some(formData))
      model.copy(parties = updatedParties)
    }
}
