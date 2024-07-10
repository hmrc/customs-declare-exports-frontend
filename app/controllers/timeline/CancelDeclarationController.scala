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

package controllers.timeline

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import controllers.helpers.ErrorHandler
import controllers.timeline.routes.CancellationResultController
import forms.timeline.CancelDeclarationDescription
import forms.timeline.CancelDeclarationDescription._
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.cancellationMetric
import models._
import models.declaration.submissions.Submission
import models.requests.SessionHelper.getDataForCancelDeclaration
import models.requests.{AuthenticatedRequest, CancelDeclarationData}
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import services.audit.{AuditService, AuditTypes, EventData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.timeline.cancel_declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CancelDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  errorHandler: ErrorHandler,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  exportsMetrics: ExportsMetrics,
  mcc: MessagesControllerComponents,
  auditService: AuditService,
  cancelDeclarationPage: cancel_declaration
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with WithUnsafeDefaultFormBinding {

  def displayPage: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getDataForCancelDeclaration match {
      case Some(cancelDeclarationData) =>
        val page = cancelDeclarationPage(CancelDeclarationDescription.form, cancelDeclarationData)
        Future.successful(Ok(page))

      case _ => errorHandler.redirectToErrorPage
    }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getDataForCancelDeclaration match {
      case Some(cancelDeclarationData) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(cancelDeclarationPage(formWithErrors, cancelDeclarationData))),
            userInput => {
              val context = exportsMetrics.startTimer(cancellationMetric)
              sendAuditedCancellationRequest(userInput, cancelDeclarationData).map { either =>
                exportsMetrics.incrementCounter(cancellationMetric)
                context.stop()

                either match {
                  case Right(models.CancellationRequestSent) => Redirect(CancellationResultController.displayHoldingPage)
                  case Right(models.CancellationAlreadyRequested) =>
                    val formWithErrors = createFormWithErrors(userInput, "cancellation.duplicateRequest.error")
                    Ok(cancelDeclarationPage(formWithErrors, cancelDeclarationData))

                  case Left(error) => errorHandler.internalServerError(error)
                }
              }
            }
          )
      case _ => errorHandler.redirectToErrorPage
    }
  }

  private def sendAuditedCancellationRequest(userInput: CancelDeclarationDescription, cancelDeclarationData: CancelDeclarationData)(
    implicit request: AuthenticatedRequest[_]
  ): Future[Either[String, CancellationStatus]] = {

    val cancelDeclaration = CancelDeclaration(
      cancelDeclarationData.submissionId, cancelDeclarationData.lrn, cancelDeclarationData.mrn,
      userInput.statementDescription, userInput.changeReason
    )

    def sendCancellationAndAudit(submission: Submission, declaration: ExportsDeclaration): Future[Either[String, CancellationStatus]] = {
      val maybeResult = for {
        _ <- auditService.auditAllPagesDeclarationCancellation(cancelDeclaration)
        cancelResult <- customsDeclareExportsConnector.createCancellation(cancelDeclaration)
      } yield cancelResult

      maybeResult.map { result =>
        auditEventGenerator(submission, declaration, cancelDeclarationData, userInput, Success.toString, result.conversationId)
        Right(result.status)
      }.recoverWith { case exception =>
        logger.error(s"Error response from backend $exception")
        auditEventGenerator(submission, declaration, cancelDeclarationData, userInput, Failure.toString, None)
        Future.successful(Left("Problem sending cancellation request"))
      }
    }

    customsDeclareExportsConnector.findSubmission(cancelDeclarationData.submissionId).flatMap {
      case Some(submission) =>
        val latestDecId = submission.latestDecId.getOrElse(submission.uuid)
        customsDeclareExportsConnector.findDeclaration(latestDecId).flatMap {
          case Some(exportsDeclaration) => sendCancellationAndAudit(submission, exportsDeclaration)
          case _                        => Future.successful(Left("Problem retrieving declaration"))
        }

      case None => Future.successful(Left("Unable to find submission"))
    }
  }

  private def createFormWithErrors(userInput: CancelDeclarationDescription, errorMessageKey: String)(
    implicit request: AuthenticatedRequest[_]
  ): Form[CancelDeclarationDescription] = {
    val messages = messagesApi.preferred(request).messages

    CancelDeclarationDescription.form
      .fill(userInput)
      .copy(errors = List(FormError(CancelDeclarationDescription.statementDescriptionKey, messages(errorMessageKey))))
  }

  private def auditEventGenerator(
    submission: Submission,
    declaration: ExportsDeclaration,
    cancelDeclarationData: CancelDeclarationData,
    userInput: CancelDeclarationDescription,
    result: String,
    conversationId: Option[String]
  )(implicit request: Request[_]): Option[Future[AuditResult]] = {
    submission.latestDecId match {
      case Some(latestDecId) =>
        Some(
          Map(
            EventData.eori.toString -> declaration.eori,
            EventData.lrn.toString -> cancelDeclarationData.lrn.lrn,
            EventData.mrn.toString -> cancelDeclarationData.mrn,
            EventData.changeReason.toString -> userInput.changeReason,
            EventData.changeDescription.toString -> userInput.statementDescription,
            EventData.submissionResult.toString -> result,
            EventData.ducr.toString -> cancelDeclarationData.ducr,
            EventData.declarationId.toString -> latestDecId,
            EventData.conversationId.toString -> conversationId.getOrElse(""),
            EventData.decType.toString -> declaration.additionalDeclarationType.getOrElse("").toString
          )
        )
      case _ => None
    }
  }.map(eventData => auditService.audit(AuditTypes.Cancellation, eventData))
}
