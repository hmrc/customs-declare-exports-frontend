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

import com.google.inject.Inject
import config.featureFlags.DeclarationAmendmentsConfig
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.routes.RootController
import forms.declaration.LegalDeclaration
import handlers.ErrorHandler
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary.legal_declaration_page

import scala.concurrent.{ExecutionContext, Future}

class AmendmentSummaryController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  override val exportsCacheService: ExportsCacheService,
  submissionService: SubmissionService,
  legalDeclarationPage: legal_declaration_page,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithUnsafeDefaultFormBinding {

  val form: Form[LegalDeclaration] = LegalDeclaration.form

  val displayPage: Action[AnyContent] = (authenticate andThen verifyEmail) {
    if (!declarationAmendmentsConfig.isEnabled) Redirect(RootController.displayPage)
    else NotImplemented
  }

  val displayDeclarationPage: Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType) { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Redirect(RootController.displayPage)
    else Ok(legalDeclarationPage(form, amend = true))
  }

  val submitAmendment: Action[AnyContent] = (authenticate andThen verifyEmail andThen journeyType).async { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Future.successful(Redirect(RootController.displayPage))
    else
      form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legalDeclarationPage(formWithErrors, amend = true))),
          legalDeclaration =>
            legalDeclaration.amendReason match {
              case Some(amendReason) =>
                val declaration = exportsCacheService.update(request.cacheModel.copy(statementDescription = Some(amendReason)))
                declaration.flatMap { _ =>
                  submissionService.amend.map { _ =>
                    Redirect(routes.AmendmentConfirmationController.displayHoldingPage)
                  }
                }
              case _ => errorHandler.displayErrorPage
            }
        )
  }
}
