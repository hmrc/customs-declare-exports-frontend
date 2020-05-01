/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.DeclarationPage
import forms.declaration.{RepresentativeEntity, RepresentativeStatus}
import javax.inject.Inject
import models.DeclarationType.DeclarationType
import models.declaration.RepresentativeDetails
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.declaration.representative_details_status

import scala.concurrent.{ExecutionContext, Future}

class RepresentativeStatusController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  representativeStatusPage: representative_details_status
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    request.cacheModel.parties.representativeDetails.map(_.statusCode) match {
      case Some(data) => Ok(representativeStatusPage(mode, navigationForm, form().fill(RepresentativeStatus(data))))
      case _          => Ok(representativeStatusPage(mode, navigationForm, form()))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RepresentativeStatus]) => Future.successful(BadRequest(representativeStatusPage(mode, navigationForm, formWithErrors))),
        validRepresentativeDetails => updateCache(validRepresentativeDetails).map(_ => navigator.continueTo(mode, nextPage(request.declarationType)))
      )
  }

  private def nextPage(declarationType: DeclarationType): Mode => Call =
    declarationType match {
      case DeclarationType.SUPPLEMENTARY =>
        controllers.declaration.routes.ConsigneeDetailsController.displayPage
      case DeclarationType.STANDARD | DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL | DeclarationType.CLEARANCE =>
        controllers.declaration.routes.CarrierDetailsController.displayPage
    }

  private def navigationForm()(implicit request: JourneyRequest[AnyContent]): DeclarationPage =
    if (request.cacheModel.parties.representativeDetails.flatMap(_.details).isDefined) RepresentativeStatus else RepresentativeEntity

  private def updateCache(formData: RepresentativeStatus)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      val representativeDetails: RepresentativeDetails = model.parties.representativeDetails.getOrElse(RepresentativeDetails())
      val updatedParties = model.parties.copy(representativeDetails = Some(representativeDetails.copy(statusCode = formData.statusCode)))
      model.copy(parties = updatedParties)
    }

  private def form()(implicit request: JourneyRequest[AnyContent]) =
    if (request.cacheModel.parties.representativeDetails.flatMap(_.details).nonEmpty)
      RepresentativeStatus.formRequired()
    else
      RepresentativeStatus.formOptional()
}
