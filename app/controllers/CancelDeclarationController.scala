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

package controllers

import connectors.CustomsDeclareExportsConnector
import controllers.actions.{AuthAction, VerifiedEmailAction}
import forms.CancelDeclarationDescription._
import forms.{CancelDeclarationDescription, Lrn}
import handlers.ErrorHandler
import metrics.ExportsMetrics
import metrics.MetricIdentifiers._
import models._
import models.declaration.submissions.Submission
import models.requests.SessionHelper._
import models.requests.AuthenticatedRequest
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.{AuditService, AuditTypes, EventData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.cancel_declaration

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

  def displayPage(additionalDecType: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getSessionData() match {
      case Some((submissionsId, mrn, lrn, ducr)) =>
        Future.successful(Ok(cancelDeclarationPage(CancelDeclarationDescription.form, additionalDecType, submissionsId, lrn, ducr, mrn)))
      case _ => errorHandler.redirectToErrorPage
    }
  }

  def onSubmit(additionalDecType: String): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    getSessionData() match {
      case Some((submissionId, mrn, lrn, ducr)) =>
        form
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[CancelDeclarationDescription]) =>
              Future.successful(BadRequest(cancelDeclarationPage(formWithErrors, additionalDecType, submissionId, lrn, ducr, mrn))),
            userInput => {
              val context = exportsMetrics.startTimer(cancellationMetric)
              sendAuditedCancellationRequest(userInput, additionalDecType, submissionId, lrn, ducr, mrn).map { either =>
                exportsMetrics.incrementCounter(cancellationMetric)
                context.stop()

                either match {
                  case Right(models.CancellationRequestSent) => Redirect(routes.CancellationResultController.displayHoldingPage)
                  case Right(models.CancellationAlreadyRequested) =>
                    Ok(
                      cancelDeclarationPage(
                        createFormWithErrors(userInput, "cancellation.duplicateRequest.error"),
                        additionalDecType,
                        submissionId,
                        lrn,
                        ducr,
                        mrn
                      )
                    )
                  case Left(error) => errorHandler.internalServerError(error)
                }
              }
            }
          )
      case _ => errorHandler.redirectToErrorPage
    }
  }

  private def sendAuditedCancellationRequest(
    userInput: CancelDeclarationDescription,
    additionalDecType: String,
    submissionId: String,
    lrn: Lrn,
    ducr: String,
    mrn: String
  )(implicit request: AuthenticatedRequest[_]): Future[Either[String, CancellationStatus]] = {

    val cancelDeclaration = CancelDeclaration(submissionId, lrn, mrn, userInput.statementDescription, userInput.changeReason)

    customsDeclareExportsConnector.findSubmission(submissionId).flatMap {
      case Some(submission) =>
        val maybeStatus = for {
          _ <- auditService.auditAllPagesDeclarationCancellation(cancelDeclaration)
          cancelStatus <- customsDeclareExportsConnector.createCancellation(cancelDeclaration)
        } yield cancelStatus

        maybeStatus.map { status =>
          val result = if (status == CancellationAlreadyRequested) Failure.toString else Success.toString
          auditEventGenerator(submission, additionalDecType, userInput, result, ducr, mrn, lrn)
          Right(status)
        }.recoverWith { case exception =>
          logger.error(s"Error response from backend $exception")
          auditEventGenerator(submission, additionalDecType, userInput, Failure.toString, ducr, mrn, lrn)
          Future.successful(Left("Problem sending cancellation request"))
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
    additionalDecType: String,
    userInput: CancelDeclarationDescription,
    result: String,
    ducr: String,
    mrn: String,
    lrn: Lrn
  )(implicit request: AuthenticatedRequest[_]): Option[Future[AuditResult]] = {
    (submission.latestDecId, submission.actions.headOption) match {
      case (Some(latestDecId), Some(action)) =>
        Some(
          Map(
            EventData.eori.toString -> request.user.eori,
            EventData.lrn.toString -> lrn.lrn,
            EventData.mrn.toString -> mrn,
            EventData.changeReason.toString -> userInput.changeReason,
            EventData.changeDescription.toString -> userInput.statementDescription,
            EventData.submissionResult.toString -> result,
            EventData.ducr.toString -> ducr,
            EventData.declarationId.toString -> latestDecId,
            EventData.conversationId.toString -> action.id,
            EventData.decType.toString -> additionalDecType
          )
        )
      case _ => None
    }
  }.map(eventData => auditService.audit(AuditTypes.Cancellation, eventData))

  private def getSessionData()(implicit request: AuthenticatedRequest[_]): Option[(String, String, Lrn, String)] =
    for {
      submissionId <- getValue(submissionUuid)
      mrn <- getValue(submissionMrn)
      lrn <- getValue(submissionLrn).map(Lrn(_))
      ducr <- getValue(submissionDucr)
    } yield (submissionId, mrn, lrn, ducr)
}
