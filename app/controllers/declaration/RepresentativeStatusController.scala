/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.declaration.RepresentativeStatus.form
import forms.declaration.{RepresentativeEntity, RepresentativeStatus}
import models.DeclarationType._
import models.declaration.RepresentativeDetails
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.representative_details_status

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepresentativeStatusController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  representativeStatusPage: representative_details_status
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form().withSubmissionErrors()
    request.cacheModel.parties.representativeDetails.map(_.statusCode) match {
      case Some(data) => Ok(representativeStatusPage(mode, navigationForm, frm.fill(RepresentativeStatus(data))))
      case _          => Ok(representativeStatusPage(mode, navigationForm, frm))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RepresentativeStatus]) => Future.successful(BadRequest(representativeStatusPage(mode, navigationForm, formWithErrors))),
        validRepresentativeDetails =>
          updateCache(validRepresentativeDetails).map { updatedCache =>
            navigator.continueTo(mode, nextPage(request.declarationType, updatedCache))
        }
      )
  }

  private def nextPage(declarationType: DeclarationType, cacheModel: Option[ExportsDeclaration]): Mode => Call =
    declarationType match {
      case SUPPLEMENTARY =>
        controllers.declaration.routes.ConsigneeDetailsController.displayPage
      case STANDARD | SIMPLIFIED | OCCASIONAL =>
        controllers.declaration.routes.CarrierEoriNumberController.displayPage
      case CLEARANCE =>
        if (cacheModel.exists(_.isNotExs))
          controllers.declaration.routes.ConsigneeDetailsController.displayPage
        else
          controllers.declaration.routes.CarrierEoriNumberController.displayPage
    }

  private def navigationForm()(implicit request: JourneyRequest[AnyContent]): DeclarationPage =
    if (request.cacheModel.parties.representativeDetails.flatMap(_.details).isDefined) RepresentativeStatus else RepresentativeEntity

  private def updateCache(formData: RepresentativeStatus)(implicit request: JourneyRequest[AnyContent]): Future[Option[ExportsDeclaration]] =
    updateExportsDeclarationSyncDirect { model =>
      val representativeDetails: RepresentativeDetails = model.parties.representativeDetails.getOrElse(RepresentativeDetails())
      val updatedParties = model.parties.copy(representativeDetails = Some(representativeDetails.copy(statusCode = formData.statusCode)))
      model.copy(parties = updatedParties)
    }
}
