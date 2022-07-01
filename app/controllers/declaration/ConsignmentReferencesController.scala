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
import forms.{Ducr, LrnValidator}
import forms.declaration.ConsignmentReferences
import forms.declaration.ConsignmentReferences.form
import models.DeclarationType.SUPPLEMENTARY
import models.Mode
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.consignment_references

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignmentReferencesController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with ModelCacheable with SubmissionErrors with WithDefaultFormBinding {

  def displayPage(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val frm = form(request.declarationType, request.cacheModel.additionalDeclarationType).withSubmissionErrors()
    request.cacheModel.consignmentReferences match {
      case Some(data) => Ok(consignmentReferencesPage(mode, frm.fill(data)))
      case _          => Ok(consignmentReferencesPage(mode, frm))
    }
  }

  def submitConsignmentReferences(mode: Mode): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form(request.declarationType, request.cacheModel.additionalDeclarationType)
      .bindFromRequest()
      .verifyLrnValidity(lrnValidator)
      .flatMap(
        _.fold(formWithErrors => Future.successful(BadRequest(consignmentReferencesPage(mode, formWithErrors))), updateCacheAndContinue(mode, _))
      )
  }

  private def nextPage(implicit request: JourneyRequest[AnyContent]): Mode => Call =
    if (request.declarationType == SUPPLEMENTARY) routes.DeclarantExporterController.displayPage
    else routes.LinkDucrToMucrController.displayPage

  private def updateCacheAndContinue(mode: Mode, consignmentReferences: ConsignmentReferences)(
    implicit request: JourneyRequest[AnyContent]
  ): Future[Result] =
    updateDeclarationFromRequest(_.copy(consignmentReferences = Some(capitaliseDucr(consignmentReferences))))
      .map(_ => navigator.continueTo(mode, nextPage))

  private def capitaliseDucr(consignmentReferences: ConsignmentReferences): ConsignmentReferences =
    consignmentReferences.copy(ducr = Ducr(consignmentReferences.ducr.ducr.toUpperCase))
}
