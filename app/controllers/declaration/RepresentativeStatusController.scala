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
import controllers.declaration.routes.{CarrierEoriNumberController, ConsigneeDetailsController}
import controllers.navigation.Navigator
import forms.DeclarationPage
import forms.declaration.RepresentativeStatus.form
import forms.declaration.{RepresentativeEntity, RepresentativeStatus}
import models.DeclarationType._
import models.declaration.RepresentativeDetails
import models.requests.JourneyRequest
import models.{ExportsDeclaration, Mode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
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
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.representativeDetails.map(_.statusCode) match {
      case Some(data) => Ok(representativeStatusPage(mode, navigationForm, frm.fill(RepresentativeStatus(data))))
      case _          => Ok(representativeStatusPage(mode, navigationForm, frm))
    }
  }

  def submitForm(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form.bindFromRequest
      .fold(
        formWithErrors => Future.successful(BadRequest(representativeStatusPage(mode, navigationForm, formWithErrors))),
        validRepresentativeDetails =>
          updateCache(validRepresentativeDetails).map { updatedCache =>
            navigator.continueTo(mode, nextPage(request.declarationType, updatedCache))
        }
      )
  }

  private def nextPage(declarationType: DeclarationType, declaration: ExportsDeclaration): Mode => Call =
    declarationType match {
      case SUPPLEMENTARY => ConsigneeDetailsController.displayPage

      case STANDARD | SIMPLIFIED | OCCASIONAL => CarrierEoriNumberController.displayPage

      case CLEARANCE =>
        if (declaration.isNotExs) ConsigneeDetailsController.displayPage
        else CarrierEoriNumberController.displayPage
    }

  private def navigationForm(implicit request: JourneyRequest[AnyContent]): DeclarationPage =
    if (request.cacheModel.parties.representativeDetails.flatMap(_.details).isDefined) RepresentativeStatus
    else RepresentativeEntity

  private def updateCache(status: RepresentativeStatus)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val representativeDetails: RepresentativeDetails = model.parties.representativeDetails.getOrElse(RepresentativeDetails())
      val updatedParties = model.parties.copy(representativeDetails = Some(representativeDetails.copy(statusCode = status.statusCode)))
      model.copy(parties = updatedParties)
    }
}
