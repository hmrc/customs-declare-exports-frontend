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

package controllers.section1

import controllers.actions.{AmendmentDraftFilter, AuthAction, JourneyAction}
import controllers.declaration.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.section1.routes.LocalReferenceNumberController
import forms.declaration.Ducr.form
import forms.declaration.Ducr
import forms.section1.ConsignmentReferences
import models.DeclarationType.{allDeclarationTypesExcluding, SUPPLEMENTARY}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.ducr_entry

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DucrEntryController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  ducrEntryPage: ducr_entry
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  private val validTypes = allDeclarationTypesExcluding(SUPPLEMENTARY)

  private val actionFilters = authenticate andThen journeyAction(validTypes) andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.ducr match {
      case Some(data) => Ok(ducrEntryPage(frm.fill(data)))
      case _          => Ok(ducrEntryPage(frm))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    form
      .bindFromRequest()
      .fold(formWithErrors => Future.successful(BadRequest(ducrEntryPage(formWithErrors))), updateCacheAndContinue(_))
  }

  val nextPage: JourneyRequest[_] => Call = _ => LocalReferenceNumberController.displayPage

  private def updateCacheAndContinue(ducr: Ducr)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    updateDeclarationFromRequest { dec =>
      dec.copy(consignmentReferences = dec.consignmentReferences match {
        case Some(consignmentRefs) => Some(consignmentRefs.copy(ducr = Some(ducr)))
        case _                     => Some(ConsignmentReferences(Some(ducr), None))
      })
    } map (_ => navigator.continueTo(nextPage(request)))
}
