/*
 * Copyright 2022 HM Revenue & Customs
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
import metrics.ExportsMetrics
import metrics.MetricIdentifiers._
import models.requests.{AuthenticatedRequest, ExportsSessionKeys}
import models.{CancelDeclaration, CancellationAlreadyRequested, CancellationRequestSent, CancellationStatus}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.{AuditService, AuditTypes, EventData}
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.cancel_declaration

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CancelDeclarationController @Inject() (
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  exportsMetrics: ExportsMetrics,
  mcc: MessagesControllerComponents,
  auditService: AuditService,
  cancelDeclarationPage: cancel_declaration
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    Ok(cancelDeclarationPage(CancelDeclarationDescription.form, lrn, ducr, mrn))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CancelDeclarationDescription]) => Future.successful(BadRequest(cancelDeclarationPage(formWithErrors, lrn, ducr, mrn))),
        userInput =>
          sendAuditedCancellationRequest(userInput).map {
            case CancellationRequestSent => Redirect(routes.CancellationResultController.displayHoldingPage())
            case CancellationAlreadyRequested =>
              Ok(cancelDeclarationPage(createFormWithErrors(userInput, "cancellation.duplicateRequest.error"), lrn, ducr, mrn))
          }
      )
  }

  private def sendAuditedCancellationRequest(
    userInput: CancelDeclarationDescription
  )(implicit request: AuthenticatedRequest[_]): Future[CancellationStatus] = {
    auditService.auditAllPagesDeclarationCancellation(CancelDeclaration(lrn, mrn, userInput.statementDescription, userInput.changeReason))
    val context = exportsMetrics.startTimer(cancelMetric)

    customsDeclareExportsConnector.createCancellation(CancelDeclaration(lrn, mrn, userInput.statementDescription, userInput.changeReason)) andThen {
      case Failure(exception) =>
        logger.error(s"Error response from backend $exception")
        auditService.audit(AuditTypes.Cancellation, auditData(userInput, Failure.toString))
      case Success(_) =>
        auditService.audit(AuditTypes.Cancellation, auditData(userInput, Success.toString))
        exportsMetrics.incrementCounter(cancelMetric)
        context.stop()
    }
  }

  private def createFormWithErrors(userInput: CancelDeclarationDescription, errorMessageKey: String)(
    implicit request: AuthenticatedRequest[_]
  ): Form[CancelDeclarationDescription] = {
    val messages = messagesApi.preferred(request).messages

    CancelDeclarationDescription.form
      .fill(userInput)
  }

  private def auditData(form: CancelDeclarationDescription, result: String)(implicit request: AuthenticatedRequest[_]): Map[String, String] =
    Map(
      EventData.eori.toString -> request.user.eori,
      EventData.lrn.toString -> lrn.value,
      EventData.mrn.toString -> mrn,
      EventData.changeReason.toString -> form.changeReason,
      EventData.changeDescription.toString -> form.statementDescription,
      EventData.submissionResult.toString -> result
    )

  private def mrn(implicit request: AuthenticatedRequest[_]): String = request.session.get(ExportsSessionKeys.submissionMrn).getOrElse("")
  private def lrn(implicit request: AuthenticatedRequest[_]): Lrn = Lrn(request.session.get(ExportsSessionKeys.submissionLrn).getOrElse(""))
  private def ducr(implicit request: AuthenticatedRequest[_]): String = request.session.get(ExportsSessionKeys.submissionDucr).getOrElse("")
}
