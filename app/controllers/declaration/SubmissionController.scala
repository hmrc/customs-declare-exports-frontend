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
import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, JourneyAction, VerifiedEmailAction}
import controllers.declaration.amendments.routes.AmendmentOutcomeController
import controllers.declaration.routes.ConfirmationController
import controllers.helpers.ErrorFixModeHelper.inErrorFixMode
import controllers.routes.RootController
import forms.declaration.{AmendmentSubmission, LegalDeclaration}
import handlers.ErrorHandler
import models.declaration.submissions.Submission
import models.requests.JourneyRequest
import models.requests.SessionHelper._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import services.cache.ExportsCacheService
import services.view.AmendmentAction
import services.view.AmendmentAction.{AmendmentAction, Cancellation, Resubmission, Submission => SubmissionAmendment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.declaration.amendments.amendment_submission
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
  amendment_submission: amendment_submission,
  legal_declaration: legal_declaration,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithUnsafeDefaultFormBinding {

  val cancelAmendment: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    if (declarationAmendmentsConfig.isDisabled) Future.successful(Redirect(RootController.displayPage))
    else
      getValue(submissionUuid) match {
        case Some(submissionId) =>
          customsDeclareExportsConnector.findSubmission(submissionId) flatMap {
            case Some(submission) => cancelAmendment(submission)
            case _                => errorHandler.internalError(s"No Submission with id($submissionId) on amendment cancellation")
          }

        case _ => errorHandler.internalError("No 'submissionUuid' in Session for an amendment cancellation")
      }
  }

  private def cancelAmendment(submission: Submission)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] =
    (submission.latestDecId, submission.latestEnhancedStatus) match {
      case (Some(latestDecId), Some(latestEnhancedStatus)) =>
        customsDeclareExportsConnector.findOrCreateDraftForAmendment(latestDecId, latestEnhancedStatus) flatMap { declarationId =>
          val call = routes.SubmissionController.displayCancelAmendmentPage
          Future.successful(Redirect(call).addingToSession((declarationUuid, declarationId)))
        }

      case _ =>
        val id = if (submission.latestDecId.isEmpty) "latestDecId" else "latestEnhancedStatus"
        errorHandler.internalError(s"Undefined $id for Submission(${submission.uuid}) on amendment cancellation")
    }

  private val actions = authenticate andThen verifyEmail andThen journeyType

  val displaySubmitDeclarationPage: Action[AnyContent] = actions { implicit request =>
    if (inErrorFixMode) errorHandler.internalServerError("Invalid mode while redirected to the 'Legal declaration' page")
    else Ok(legal_declaration(LegalDeclaration.form))
  }

  val displayCancelAmendmentPage: Action[AnyContent] = actions { implicit request =>
    if (declarationAmendmentsConfig.isDisabled) Redirect(RootController.displayPage)
    else Ok(amendment_submission(AmendmentSubmission.form(true), Cancellation))
  }

  val displaySubmitAmendmentPage: Action[AnyContent] = actions { implicit request =>
    if (declarationAmendmentsConfig.isDisabled) Redirect(RootController.displayPage)
    else Ok(amendment_submission(AmendmentSubmission.form(false), SubmissionAmendment))
  }

  val displayResubmitAmendmentPage: Action[AnyContent] = actions { implicit request =>
    if (declarationAmendmentsConfig.isDisabled) Redirect(RootController.displayPage)
    else Ok(amendment_submission(AmendmentSubmission.form(false), Resubmission))
  }

  val submitDeclaration: Action[AnyContent] = actions.async { implicit request =>
    LegalDeclaration.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legal_declaration(formWithErrors))),
        submissionService.submitDeclaration(request.eori, request.cacheModel, _).map {
          case Some(submission) => Redirect(ConfirmationController.displayHoldingPage).withSession(session(submission))
          case _                => errorHandler.internalServerError("Error from Customs Declarations API")
        }
      )
  }

  def submitAmendment(amendmentActionAsString: String): Action[AnyContent] = actions.async { implicit request =>
    if (declarationAmendmentsConfig.isDisabled) Future.successful(Redirect(RootController.displayPage))
    else {
      val amendmentAction = AmendmentAction.from(amendmentActionAsString)
      val isCancellation = amendmentAction == Cancellation
      val binding = AmendmentSubmission.form(isCancellation).bindFromRequest()
      binding.fold(
        formWithErrors => Future.successful(BadRequest(amendment_submission(formWithErrors, amendmentAction))),
        amendmentSubmission =>
          getValue(submissionUuid).fold(errorHandler.internalError(submissionError(amendmentAction))) { submissionId =>
            for {
              declaration <- exportsCacheService.update(request.cacheModel.copy(statementDescription = Some(amendmentSubmission.reason)))
              maybeActionId <- submissionService.submitAmendment(request.eori, declaration, amendmentSubmission, submissionId, amendmentAction)
            } yield maybeActionId match {
              case Some(actionId) =>
                Redirect(AmendmentOutcomeController.displayHoldingPage(isCancellation)).addingToSession(submissionActionId -> actionId)

              case _ => errorHandler.badRequest
            }
          }
      )
    }
  }

  private def submissionError(amendmentAction: AmendmentAction)(implicit request: JourneyRequest[_]): String = {
    val action = amendmentAction match {
      case Cancellation => "cancellation"
      case Resubmission => "resubmission"
      case _            => "submission"
    }
    s"Cannot retrieve 'submissionUuid' from session on Amendment $action for declaration(${request.cacheModel.id})"
  }

  private def session(submission: Submission)(implicit request: JourneyRequest[_]): Session =
    removeValue(declarationUuid) +
      (submissionUuid -> submission.uuid) +
      (submissionDucr -> submission.ducr.fold("")(identity)) +
      (submissionLrn -> submission.lrn)
}
