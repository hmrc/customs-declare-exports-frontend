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

package services

import com.google.inject.Inject
import connectors.CustomsDeclareExportsConnector
import forms.declaration.{AmendmentSubmission, LegalDeclaration}
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.{submissionAmendmentMetric, submissionMetric}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.ExportsDeclaration
import models.declaration.DeclarationStatus.DeclarationStatus
import models.declaration.submissions.{Submission, SubmissionAmendment}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import services.audit.AuditTypes.{AmendmentCancellation, AmendmentPayload, Audit, SubmissionPayload}
import services.audit.{AuditService, AuditTypes, EventData}
import services.view.AmendmentAction.{AmendmentAction, Cancellation, Resubmission}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject() (connector: CustomsDeclareExportsConnector, auditService: AuditService, metrics: ExportsMetrics) extends Logging {

  def submitDeclaration(eori: String, declaration: ExportsDeclaration, legalDeclaration: LegalDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[Submission]] = {
    val timerContext = metrics.startTimer(submissionMetric)
    auditService.auditAllPagesUserInput(SubmissionPayload, declaration)

    logProgress(declaration, "Beginning Submission")
    connector
      .submitDeclaration(declaration.id)
      .andThen {
        case Success(successfulSubmission) =>
          val conversationId = successfulSubmission.actions.headOption.map(_.id).getOrElse("")
          logProgress(declaration, "Submitted Successfully")
          auditSubmission(eori, declaration, legalDeclaration, conversationId, Success.toString, AuditTypes.Submission)
          metrics.incrementCounter(submissionMetric)
          timerContext.stop()

        case Failure(exception) =>
          logProgress(declaration, "Submission Failed")
          logger.error(s"Error response from backend $exception")
          auditSubmission(eori, declaration, legalDeclaration, "N/A", Failure.toString, AuditTypes.Submission)
      }
      .map(Some(_))
  }

  def submitAmendment(
    eori: String,
    declaration: ExportsDeclaration,
    amendmentSubmission: AmendmentSubmission,
    submissionId: String,
    amendmentAction: AmendmentAction
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] =
    declaration.declarationMeta.parentDeclarationId.map { parentDeclarationId =>
      connector.findDeclaration(parentDeclarationId).flatMap {
        case Some(parentDeclaration) =>
          val timerContext = metrics.startTimer(submissionAmendmentMetric)
          val isCancellation = amendmentAction == Cancellation

          val auditTypeAndFunction =
            if (isCancellation) (AmendmentCancellation, () => auditService.auditAllPagesUserInput(AmendmentCancellation, declaration))
             else (AuditTypes.Amendment, () => auditService.auditAllPagesUserInput(AmendmentPayload, declaration))

          val fieldPointers =
            if (isCancellation) List("")
            else {
              val declarationDiff = declaration.createDiff(parentDeclaration)
              declarationDiff.map(_.fieldPointer)
            }

          val submissionAmendment = SubmissionAmendment(submissionId, declaration.id, isCancellation, fieldPointers)
          val result = auditTypeAndFunction._2().flatMap{_ =>
            if (amendmentAction == Resubmission) connector.resubmitAmendment(submissionAmendment)
            else connector.submitAmendment(submissionAmendment)
          }

          result.andThen {
            case Success(conversationId) =>
              auditAmendmentSubmission(eori, declaration, parentDeclaration, amendmentSubmission, conversationId, Success.toString, auditTypeAndFunction._1)
              metrics.incrementCounter(submissionAmendmentMetric)
              timerContext.stop()

            case Failure(exception) =>
              logProgress(declaration, "Amendment Submission Failed")
              logger.error(s"Error response from backend $exception")
              auditAmendmentSubmission(eori, declaration, parentDeclaration, amendmentSubmission, "N/A", Failure.toString, auditTypeAndFunction._1)
          }
            .map(Some(_))

        case None =>
          logger.warn(s"Amendment declaration $parentDeclarationId cannot be found")
          Future.successful(None)
      }
    } getOrElse {
      logger.warn(s"Amendment submission of a declaration(${declaration.id}) with 'parentDeclarationId' undefined")
      Future.successful(None)
    }

  private def auditSubmission(
    eori: String,
    declaration: ExportsDeclaration,
    legalDeclaration: LegalDeclaration,
    conversationId: String,
    opResult: String,
    auditType: Audit
  )(implicit hc: HeaderCarrier): Unit =
    auditService.audit(
      auditType,
      auditSubmission(
        eori,
        declaration.additionalDeclarationType,
        declaration.lrn,
        declaration.ducr.map(_.ducr),
        legalDeclaration,
        declaration.id,
        declaration.declarationMeta.status,
        conversationId,
        opResult
      )
    )

  private def auditAmendmentSubmission(
    eori: String,
    newDeclaration: ExportsDeclaration,
    oldDeclaration: ExportsDeclaration,
    amendmentSubmission: AmendmentSubmission,
    conversationId: String,
    opResult: String,
    auditType: Audit
  )(implicit hc: HeaderCarrier): Unit = {

    val auditPayload = Json
      .toJson(
        auditAmendment(
          eori,
          newDeclaration.additionalDeclarationType,
          newDeclaration.lrn,
          newDeclaration.ducr.map(_.ducr),
          amendmentSubmission,
          newDeclaration.id,
          newDeclaration.declarationMeta.status,
          conversationId,
          opResult
        )
      )
      .as[JsObject]

    val declarationVersions =
      Json.obj("preAmendDeclaration" -> Json.toJson(oldDeclaration).as[JsObject], "postAmendDeclaration" -> Json.toJson(newDeclaration).as[JsObject])

    auditService.auditAmendmentSent(auditType, declarationVersions ++ auditPayload)
  }

  private def auditAmendment(
    eori: String,
    additionalDeclarationType: Option[AdditionalDeclarationType],
    lrn: Option[String],
    ducr: Option[String],
    amendmentSubmission: AmendmentSubmission,
    id: String,
    status: DeclarationStatus,
    conversationId: String,
    result: String
  ): Map[String, String] =
    Map(
      EventData.eori.toString -> eori,
      EventData.decType.toString -> additionalDeclarationType.map(_.toString).getOrElse(""),
      EventData.lrn.toString -> lrn.getOrElse(""),
      EventData.ducr.toString -> ducr.getOrElse(""),
      EventData.changeReason.toString -> amendmentSubmission.reason,
      EventData.fullName.toString -> amendmentSubmission.fullName,
      EventData.jobRole.toString -> amendmentSubmission.jobRole,
      EventData.email.toString -> amendmentSubmission.email,
      EventData.confirmed.toString -> amendmentSubmission.confirmation.toString,
      EventData.submissionResult.toString -> result,
      EventData.declarationId.toString -> id,
      EventData.declarationStatus.toString -> status.toString,
      EventData.conversationId.toString -> conversationId
    )

  private def auditSubmission(
    eori: String,
    additionalDeclarationType: Option[AdditionalDeclarationType],
    lrn: Option[String],
    ducr: Option[String],
    legalDeclaration: LegalDeclaration,
    id: String,
    status: DeclarationStatus,
    conversationId: String,
    result: String
  ): Map[String, String] =
    Map(
      EventData.eori.toString -> eori,
      EventData.decType.toString -> additionalDeclarationType.map(_.toString).getOrElse(""),
      EventData.lrn.toString -> lrn.getOrElse(""),
      EventData.ducr.toString -> ducr.getOrElse(""),
      EventData.fullName.toString -> legalDeclaration.fullName,
      EventData.jobRole.toString -> legalDeclaration.jobRole,
      EventData.email.toString -> legalDeclaration.email,
      EventData.confirmed.toString -> legalDeclaration.confirmation.toString,
      EventData.submissionResult.toString -> result,
      EventData.declarationId.toString -> id,
      EventData.declarationStatus.toString -> status.toString,
      EventData.conversationId.toString -> conversationId
    )

  private def logProgress(declaration: ExportsDeclaration, message: String): Unit =
    logger.info(s"Declaration [${declaration.id}]: $message")
}
