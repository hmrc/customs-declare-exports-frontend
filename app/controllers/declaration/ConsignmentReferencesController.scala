/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.concurrent.{ExecutionContext, Future}

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.navigation.Navigator
import forms.declaration.ConsignmentReferences
import forms.declaration.ConsignmentReferences.{ducrId, form}
import javax.inject.Inject
import models.DeclarationType.SUPPLEMENTARY
import models.Mode
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.consignment_references

class ConsignmentReferencesController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  exportsConnector: CustomsDeclareExportsConnector,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form(request.declarationType, request.cacheModel.additionalDeclarationType).withSubmissionErrors()
    request.cacheModel.consignmentReferences match {
      case Some(data) => Ok(consignmentReferencesPage(mode, frm.fill(data)))
      case _          => Ok(consignmentReferencesPage(mode, frm))
    }
  }

  def submitConsignmentReferences(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val boundForm = form(request.declarationType, request.cacheModel.additionalDeclarationType).bindFromRequest()
    boundForm.fold(
      formWithErrors => Future.successful(BadRequest(consignmentReferencesPage(mode, formWithErrors))),
      verifyDucrDuplicationIfNotSupplementary(mode, boundForm, _)
    )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    if (request.declarationType == SUPPLEMENTARY) routes.DeclarantExporterController.displayPage
    else routes.LinkDucrToMucrController.displayPage

  private def updateCacheAndContinue(mode: Mode, formData: ConsignmentReferences)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateExportsDeclarationSyncDirect(_.copy(consignmentReferences = Some(formData)))
      .map(_ => navigator.continueTo(mode, nextPage))

  private def verifyDucrDuplicationIfNotSupplementary(mode: Mode, form: Form[ConsignmentReferences], consignmentReferences: ConsignmentReferences)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    if (request.declarationType == SUPPLEMENTARY) updateCacheAndContinue(mode, consignmentReferences)
    else verifyDucrDuplication(mode, form, consignmentReferences)

  private def verifyDucrDuplication(mode: Mode, form: Form[ConsignmentReferences], consignmentReferences: ConsignmentReferences)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    exportsConnector.findSubmissionByDucr(consignmentReferences.ducr).flatMap {
      _.fold(updateCacheAndContinue(mode, consignmentReferences)) { _ =>
        val data = Map(ducrId -> consignmentReferences.ducr.ducr, "lrn" -> consignmentReferences.lrn.value)
        val formWithErrors = form.copy(data = data, errors = ConsignmentReferences.duplicatedDucr)
        Future.successful(BadRequest(consignmentReferencesPage(mode, formWithErrors)))
      }
    }
}
