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
import controllers.general.{ModelCacheable, SubmissionErrors}
import controllers.section1.routes.{DeclarantDetailsController, DucrChoiceController}
import controllers.navigation.Navigator
import forms.section1.AdditionalDeclarationType.AdditionalDeclarationType
import forms.section1.AdditionalDeclarationTypePage
import models.DeclarationType.CLEARANCE
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.audit.AuditService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.section1.additional_declaration_type

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalDeclarationTypeController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val exportsCacheService: ExportsCacheService,
  navigator: Navigator,
  mcc: MessagesControllerComponents,
  additionalTypePage: additional_declaration_type
)(implicit ec: ExecutionContext, auditService: AuditService)
    extends FrontendController(mcc) with AmendmentDraftFilter with I18nSupport with ModelCacheable with SubmissionErrors
    with WithUnsafeDefaultFormBinding {

  val nextPage: JourneyRequest[_] => Call =
    request =>
      if (request.declarationType == CLEARANCE) DucrChoiceController.displayPage
      else DeclarantDetailsController.displayPage

  private val actionFilters = authenticate andThen journeyAction andThen nextPageIfAmendmentDraft

  val displayPage: Action[AnyContent] = actionFilters { implicit request =>
    val form = AdditionalDeclarationTypePage.form.withSubmissionErrors
    request.cacheModel.additionalDeclarationType match {
      case Some(data) => Ok(additionalTypePage(form.fill(data)))
      case _          => Ok(additionalTypePage(form))
    }
  }

  val submitForm: Action[AnyContent] = actionFilters.async { implicit request =>
    val form = AdditionalDeclarationTypePage.form.bindFromRequest()
    form
      .fold(
        formWithErrors => Future.successful(BadRequest(additionalTypePage(formWithErrors))),
        updateCache(_).map(_ => navigator.continueTo(nextPage(request)))
      )
  }

  private def updateCache(adt: AdditionalDeclarationType)(implicit request: JourneyRequest[AnyContent]): Future[ExportsDeclaration] =
    updateDeclarationFromRequest(_.copy(additionalDeclarationType = Some(adt)))
}
