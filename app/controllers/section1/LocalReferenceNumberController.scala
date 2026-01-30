/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section1

import controllers.actions.{AmendmentDraftFilter, AuthAction, JourneyAction}
import controllers.section1.routes.LinkDucrToMucrController
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import forms.section1.Lrn.form
import forms.section1.{ConsignmentReferences, Lrn, LrnValidator}
import models.DeclarationType.{allDeclarationTypesExcluding, SUPPLEMENTARY}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.local_reference_number

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocalReferenceNumberController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  lrnValidator: LrnValidator,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  LrnPage: local_reference_number
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call = _ => LinkDucrToMucrController.displayPage

  private val validTypes = allDeclarationTypesExcluding(SUPPLEMENTARY)

  private val actionFilters = authenticate andThen journeyAction(validTypes) andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.consignmentReferences.flatMap(_.lrn) match {
      case Some(data) => Ok(LrnPage(frm.fill(data)))
      case _          => Ok(LrnPage(frm))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    form
      .bindFromRequest()
      .verifyLrnValidity(lrnValidator)
      .flatMap(_.fold(formWithErrors => Future.successful(BadRequest(LrnPage(formWithErrors))), updateCacheAndContinue(_)))
  }

  private def updateCacheAndContinue(lrn: Lrn)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest { dec =>
      dec.copy(consignmentReferences = dec.ducr.map(ducr => ConsignmentReferences(Some(ducr), Some(lrn), hasDucr = dec.hasDucr)))
    } map (_ => navigator.continueTo(nextPage(request)))

}
