/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.section2

import controllers.actions.{AuthAction, JourneyAction}
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section2.routes.RepresentativeStatusController
import forms.section2.representative.RepresentativeEntity.form
import forms.section2.representative.RepresentativeEntity
import models.ExportsDeclaration
import models.declaration.RepresentativeDetails
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section2.representative.representative_details_entity

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepresentativeEntityController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  navigator: Navigator,
  override val exportsCacheService: ExportsCacheService,
  mcc: MessagesControllerComponents,
  representativeEntityPage: representative_details_entity
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.parties.representativeDetails.flatMap(_.details) match {
      case Some(data) => Ok(representativeEntityPage(frm.fill(RepresentativeEntity(data))))
      case _          => Ok(representativeEntityPage(frm))
    }
  }

  private def nextPage: Call = RepresentativeStatusController.displayPage

  def submitForm(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[RepresentativeEntity]) => Future.successful(BadRequest(representativeEntityPage(formWithErrors))),
        validRepresentativeDetails => updateCache(validRepresentativeDetails).map(_ => navigator.continueTo(nextPage))
      )
  }

  private def updateCache(formData: RepresentativeEntity)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest { model =>
      val representativeDetails: RepresentativeDetails = model.parties.representativeDetails.getOrElse(RepresentativeDetails())
      val updatedParties = model.parties.copy(representativeDetails = Some(representativeDetails.copy(details = Some(formData.details))))
      model.copy(parties = updatedParties)
    }
}
