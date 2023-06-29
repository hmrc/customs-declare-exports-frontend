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

package controllers.declaration

import controllers.actions.{AmendmentDraftFilter, AuthAction, JourneyAction}
import controllers.declaration.routes.{DeclarantExporterController, LinkDucrToMucrController}
import controllers.navigation.Navigator
import forms.declaration.ConsignmentReferences
import forms.declaration.ConsignmentReferences.form
import forms.{Ducr, LrnValidator}
import models.DeclarationType.SUPPLEMENTARY
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.consignment_references

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentReferencesController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call =
    request =>
      if (request.declarationType == SUPPLEMENTARY) DeclarantExporterController.displayPage
      else LinkDucrToMucrController.displayPage

  private val actionFilters = authenticate andThen journeyAction andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val frm = form(request.declarationType, request.cacheModel.additionalDeclarationType).withSubmissionErrors
    request.cacheModel.consignmentReferences match {
      case Some(data) => Ok(consignmentReferencesPage(frm.fill(data)))
      case _          => Ok(consignmentReferencesPage(frm))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    form(request.declarationType, request.cacheModel.additionalDeclarationType)
      .bindFromRequest()
      .verifyLrnValidity(lrnValidator)
      .flatMap(_.fold(formWithErrors => Future.successful(BadRequest(consignmentReferencesPage(formWithErrors))), updateCacheAndContinue(_)))
  }

  private def updateCacheAndContinue(consignmentReferences: ConsignmentReferences)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest(_.copy(consignmentReferences = Some(capitaliseDucr(consignmentReferences))))
      .map(_ => navigator.continueTo(nextPage(request)))

  private def capitaliseDucr(consignmentReferences: ConsignmentReferences): ConsignmentReferences =
    consignmentReferences.ducr.fold(consignmentReferences)(ducr => consignmentReferences.copy(ducr = Some(Ducr(ducr.ducr.toUpperCase))))
}
