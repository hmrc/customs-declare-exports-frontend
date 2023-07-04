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
import controllers.declaration.amendments.routes.AmendmentOutcomeController
import controllers.declaration.routes.ConfirmationController
import controllers.helpers.ErrorFixModeHelper.inErrorFixMode
import controllers.routes.RootController
import connectors.CustomsDeclareExportsConnector
import forms.declaration.LegalDeclaration
import forms.declaration.LegalDeclaration.{amendReasonKey, form}
import handlers.ErrorHandler
import models.declaration.submissions.EnhancedStatus.ERRORS
import models.declaration.submissions.Submission
import models.requests.JourneyRequest
import models.requests.SessionHelper._
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import services.cache.ExportsCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.summary.legal_declaration

import scala.concurrent.{ExecutionContext, Future}

class SubmissionController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  override val exportsCacheService: ExportsCacheService,
  submissionService: SubmissionService,
  legal_declaration: legal_declaration,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithUnsafeDefaultFormBinding {

  val actions = authenticate andThen verifyEmail andThen journeyType

  def displayLegalDeclarationPage(isAmendment: Boolean, isCancellation: Boolean): Action[AnyContent] = actions { implicit request =>
    if (isAmendment) {
      if (declarationAmendmentsConfig.isEnabled) Ok(legal_declaration(form, amend = true, isCancellation))
      else Redirect(RootController.displayPage)
    } else if (inErrorFixMode) {
      val msg = "Invalid mode while redirected to the 'Legal declaration' page"
      errorHandler.internalServerError(msg)
    } else Ok(legal_declaration(form))
  }

  val submitDeclaration: Action[AnyContent] = actions.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legal_declaration(formWithErrors))),
        submissionService.submitDeclaration(request.eori, request.cacheModel, _).map {
          case Some(submission) => Redirect(ConfirmationController.displayHoldingPage).withSession(session(submission))
          case _                => errorHandler.internalServerError("Error from Customs Declarations API")
        }
      )
  }

  def cancelAmendment(): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getValue(submissionUuid) match {
      case Some(submissionId) =>
        customsDeclareExportsConnector.findSubmission(submissionId) flatMap { maybeSubmission =>
          (for {
            submission <- maybeSubmission
            latestDecId <- submission.latestDecId
          } yield customsDeclareExportsConnector.findOrCreateDraftForAmendment(latestDecId, ERRORS) map { _ =>
            Redirect(routes.SubmissionController.displayLegalDeclarationPage(true, true))
          }) getOrElse {
            Future.successful(errorHandler.internalServerError("latestDecId does not exist in submission for amendment cancellation"))
          }
        }
      case _ =>
        Future.successful(errorHandler.internalServerError("No 'submissionUuid' from in session data for an amendment cancellation"))
    }

  }

  def submitAmendment(isCancellation: Boolean): Action[AnyContent] = actions.async { implicit request =>
    if (declarationAmendmentsConfig.isEnabled) {
      val binding = form.bindFromRequest()
      binding.fold(
        formWithErrors => Future.successful(BadRequest(legal_declaration(formWithErrors, amend = true, isCancellation))),
        legalDeclaration =>
          (legalDeclaration.amendReason, getValue(submissionUuid)) match {
            case (Some(amendReason), Some(submissionId)) =>
              for {
                declaration <- exportsCacheService.update(request.cacheModel.copy(statementDescription = Some(amendReason)))
                maybeActionId <- submissionService.submitAmendment(request.eori, declaration, legalDeclaration, submissionId, isCancellation)
              } yield maybeActionId match {
                case Some(actionId) =>
                  Redirect(AmendmentOutcomeController.displayHoldingPage).addingToSession(submissionActionId -> actionId)

                case _ => errorHandler.badRequest
              }

            case (Some(_), None) =>
              val declarationId = request.cacheModel.id
              val msg = s"Cannot retrieve 'submissionUuid' from session on Amendment submission for declaration(${declarationId})"
              errorHandler.internalError(msg)

            case _ => toPageWithErrorsForEmptyAmendReason(legalDeclaration, isCancellation)
          }
      )
    } else Future.successful(Redirect(RootController.displayPage))
  }

  private def toPageWithErrorsForEmptyAmendReason(legalDeclaration: LegalDeclaration, isCancellation: Boolean)(
    implicit request: JourneyRequest[_]
  ): Future[Result] = {
    val messages = messagesApi.preferred(request).messages
    val formError = FormError(amendReasonKey, messages("legal.declaration.amendReason.empty"))
    val formWithErrors = form.fill(legalDeclaration).copy(errors = List(formError))
    Future.successful(BadRequest(legal_declaration(formWithErrors, amend = true, isCancellation)))
  }

  private def session(submission: Submission)(implicit request: JourneyRequest[_]): Session =
    removeValue(declarationUuid) +
      (submissionUuid -> submission.uuid) +
      (submissionDucr -> submission.ducr.fold("")(identity)) +
      (submissionLrn -> submission.lrn)
}
