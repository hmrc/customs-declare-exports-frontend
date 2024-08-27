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
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.navigation.Navigator
import controllers.summary.routes.SectionSummaryController
import forms.section1.Mucr._
import forms.section1.Mucr
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.mucr_code

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MucrController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  mucrPage: mucr_code
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call = _ => SectionSummaryController.displayPage(1)

  private val actionFilters = authenticate andThen journeyAction andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val frm = form.withSubmissionErrors
    request.cacheModel.mucr match {
      case Some(mucr) => Ok(mucrPage(frm.fill(mucr)))
      case _          => Ok(mucrPage(frm))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Mucr]) => Future.successful(BadRequest(mucrPage(formWithErrors))),
        mucr => updateCache(mucr).map(_ => navigator.continueTo(nextPage(request)))
      )
  }

  private def updateCache(mucr: Mucr)(implicit request: JourneyRequest[_]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.copy(mucr = Some(mucr)))
}
