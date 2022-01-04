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
import forms.CancelDeclaration
import forms.CancelDeclaration._
import metrics.ExportsMetrics
import metrics.MetricIdentifiers._
import models.{CancellationAlreadyRequested, CancellationRequestSent, CancellationStatus, MrnNotFound}
import models.requests.AuthenticatedRequest
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.audit.{AuditService, AuditTypes}
import services.audit.EventData._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{cancel_declaration, cancellation_confirmation_page}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class CancelDeclarationController @Inject()(
  authenticate: AuthAction,
  verifyEmail: VerifiedEmailAction,
  customsDeclareExportsConnector: CustomsDeclareExportsConnector,
  exportsMetrics: ExportsMetrics,
  mcc: MessagesControllerComponents,
  auditService: AuditService,
  cancelDeclarationPage: cancel_declaration,
  cancelConfirmationPage: cancellation_confirmation_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with Logging {

  def displayPage(): Action[AnyContent] = (authenticate andThen verifyEmail) { implicit request =>
    Ok(cancelDeclarationPage(CancelDeclaration.form))
  }

  def onSubmit(): Action[AnyContent] = (authenticate andThen verifyEmail).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[CancelDeclaration]) => Future.successful(BadRequest(cancelDeclarationPage(formWithErrors))),
        userInput => {
          sendAuditedCancellationRequest(userInput).map { response =>
            response match {
              case CancellationRequestSent      => Ok(cancelConfirmationPage())
              case MrnNotFound                  => Ok(cancelDeclarationPage(createFormWithErrors(userInput, "cancellation.mrn.error.denied")))
              case CancellationAlreadyRequested => Ok(cancelDeclarationPage(createFormWithErrors(userInput, "cancellation.duplicateRequest.error")))
            }
          }
        }
      )
  }

  private def sendAuditedCancellationRequest(userInput: CancelDeclaration)(implicit request: AuthenticatedRequest[_]): Future[CancellationStatus] = {
    auditService.auditAllPagesDeclarationCancellation(userInput)
    val context = exportsMetrics.startTimer(cancelMetric)

    customsDeclareExportsConnector.createCancellation(userInput) andThen {
      case Failure(exception) =>
        logger.error(s"Error response from backend $exception")
        auditService.audit(AuditTypes.Cancellation, auditData(userInput, Failure.toString))
      case Success(_) =>
        auditService.audit(AuditTypes.Cancellation, auditData(userInput, Success.toString))
        exportsMetrics.incrementCounter(cancelMetric)
        context.stop()
    }
  }

  private def createFormWithErrors(userInput: CancelDeclaration, errorMessageKey: String)(
    implicit request: AuthenticatedRequest[_]
  ): Form[CancelDeclaration] = {
    val messages = messagesApi.preferred(request).messages

    CancelDeclaration.form
      .fill(userInput)
      .copy(errors = List(FormError.apply(mrnKey, messages(errorMessageKey))))
  }

  private def auditData(form: CancelDeclaration, result: String)(implicit request: AuthenticatedRequest[_]): Map[String, String] =
    Map(
      eori.toString -> request.user.eori,
      lrn.toString -> form.functionalReferenceId.value,
      mrn.toString -> form.mrn,
      changeReason.toString -> form.changeReason,
      changeDescription.toString -> form.statementDescription,
      submissionResult.toString -> result
    )
}
