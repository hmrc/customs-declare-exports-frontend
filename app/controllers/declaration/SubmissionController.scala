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
import controllers.declaration.amendments.routes.AmendmentConfirmationController
import controllers.declaration.routes.ConfirmationController
import controllers.helpers.ErrorFixModeHelper.inErrorFixMode
import controllers.routes.RootController
import forms.declaration.LegalDeclaration
import forms.declaration.LegalDeclaration.form
import handlers.ErrorHandler
import models.declaration.submissions.Submission
import models.requests.ExportsSessionKeys._
import models.requests.{ExportsSessionKeys, JourneyRequest}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result, Session}
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
  override val exportsCacheService: ExportsCacheService,
  submissionService: SubmissionService,
  legal_declaration: legal_declaration,
  declarationAmendmentsConfig: DeclarationAmendmentsConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with ModelCacheable with WithUnsafeDefaultFormBinding {

  val actions = authenticate andThen verifyEmail andThen journeyType

  def displayLegalDeclarationPage(isAmendment: Boolean, action: Option[String]): Action[AnyContent] = actions { implicit request =>
    if (isAmendment || action.contains("cancel")) {
      if (declarationAmendmentsConfig.isEnabled) {
        Ok(legal_declaration(form, amend = true, action))
      } else Redirect(RootController.displayPage)
    } else if (inErrorFixMode) handleError("Invalid mode while redirected to the 'Legal declaration' page")
    else Ok(legal_declaration(form))
  }

  def submitAmendment(action: Option[String]): Action[AnyContent] = actions.async { implicit request =>
    if (!declarationAmendmentsConfig.isEnabled) Future.successful(Redirect(RootController.displayPage))
    else
      form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legal_declaration(formWithErrors, amend = true, action))),
          _.amendReason match {
            case Some(amendReason) =>
              val declaration = exportsCacheService.update(request.cacheModel.copy(statementDescription = Some(amendReason)))
              declaration.flatMap { _ =>
                submissionService.amend.map { _ =>
                  Redirect(AmendmentConfirmationController.displayHoldingPage)
                }
              }
            case _ => errorHandler.displayErrorPage
          }
        )
  }

  def cancelAmendment(decId: Option[String]): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    decId match {
      case Some(id) if declarationAmendmentsConfig.isEnabled =>
        Redirect(routes.SubmissionController.submitAmendment(Some("cancel"))).addingToSession((ExportsSessionKeys.declarationId, id))
      case _ =>
        Redirect(controllers.routes.RootController.displayPage)
    }
  }

  val submitDeclaration: Action[AnyContent] = actions.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[LegalDeclaration]) => Future.successful(BadRequest(legal_declaration(formWithErrors))),
        submissionService.submit(request.eori, request.cacheModel, _).map {
          case Some(submission) => Redirect(ConfirmationController.displayHoldingPage).withSession(session(submission))
          case _                => handleError(s"Error from Customs Declarations API")
        }
      )
  }

  private def handleError(logMessage: String)(implicit request: Request[_]): Result = {
    logger.error(logMessage)
    InternalServerError(errorHandler.globalErrorPage)
  }

  private def session(submission: Submission)(implicit request: JourneyRequest[_]): Session =
    request.session - declarationId +
      (declarationType -> request.cacheModel.additionalDeclarationType.fold("")(_.toString)) +
      (submissionId -> submission.uuid) +
      (submissionDucr -> submission.ducr.fold("")(identity)) +
      (submissionLrn -> submission.lrn)
}
