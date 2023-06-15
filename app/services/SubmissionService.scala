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
import forms.declaration.LegalDeclaration
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.{submissionAmendmentMetric, submissionMetric}
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration
import models.declaration.submissions.{Submission, SubmissionAmendment}
import play.api.Logging
import services.DiffTools.ExportsDeclarationDiff
import services.audit.AuditTypes.{AmendmentCancellation, AmendmentPayload, Audit, SubmissionPayload}
import services.audit.{AuditService, AuditTypes, EventData}
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
        case Success(_) =>
          logProgress(declaration, "Submitted Successfully")
          auditSubmission(eori, declaration, None, legalDeclaration, Success.toString, AuditTypes.Submission)
          metrics.incrementCounter(submissionMetric)
          timerContext.stop()

        case Failure(exception) =>
          logProgress(declaration, "Submission Failed")
          logger.error(s"Error response from backend $exception")
          auditSubmission(eori, declaration, None, legalDeclaration, Failure.toString, AuditTypes.Submission)
      }
      .map(Some(_))
  }

  def submitAmendment(
    eori: String,
    declaration: ExportsDeclaration,
    legalDeclaration: LegalDeclaration,
    submissionId: String,
    isCancellation: Boolean
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] =
    declaration.declarationMeta.parentDeclarationId.map { parentDeclarationId =>
      connector.findDeclaration(parentDeclarationId).flatMap {
        case Some(parentDeclaration) =>
          val timerContext = metrics.startTimer(submissionAmendmentMetric)
          val auditType =
            if (isCancellation) AmendmentCancellation
            else {
              auditService.auditAllPagesUserInput(AmendmentPayload, declaration)
              AuditTypes.Amendment
            }

          val declarationDiff = declaration.createDiff(parentDeclaration)
          val fieldPointers = declarationDiff.map(_.fieldPointer)

          val submissionAmendment = SubmissionAmendment(submissionId, declaration.id, fieldPointers)
          connector
            .submitAmendment(submissionAmendment)
            .andThen {
              case Success(_) =>
                auditSubmission(eori, declaration, Some(declarationDiff), legalDeclaration, Success.toString, auditType)
                metrics.incrementCounter(submissionAmendmentMetric)
                timerContext.stop()

              case Failure(exception) =>
                logProgress(declaration, "Amendment Submission Failed")
                logger.error(s"Error response from backend $exception")
                auditSubmission(eori, declaration, Some(declarationDiff), legalDeclaration, Failure.toString, auditType)
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
    diff: Option[ExportsDeclarationDiff],
    legalDeclaration: LegalDeclaration,
    opResult: String,
    auditType: Audit
  )(implicit hc: HeaderCarrier): Unit =
    auditService.audit(
      auditType,
      auditData(eori, declaration.`type`, declaration.lrn, declaration.ducr.map(_.ducr), diff, legalDeclaration, opResult)
    )

  private def auditData(
    eori: String,
    `type`: DeclarationType,
    lrn: Option[String],
    ducr: Option[String],
    declarationDiff: Option[ExportsDeclarationDiff],
    legalDeclaration: LegalDeclaration,
    result: String
  ): Map[String, String] =
    Map(
      EventData.eori.toString -> eori,
      EventData.decType.toString -> `type`.toString,
      EventData.lrn.toString -> lrn.getOrElse(""),
      EventData.ducr.toString -> ducr.getOrElse(""),
      EventData.AmendedFields.toString -> declarationDiff.fold("n/a")(DiffTools.toStringForAudit),
      EventData.fullName.toString -> legalDeclaration.fullName,
      EventData.jobRole.toString -> legalDeclaration.jobRole,
      EventData.email.toString -> legalDeclaration.email,
      EventData.confirmed.toString -> legalDeclaration.confirmation.toString,
      EventData.submissionResult.toString -> result
    )

  private def logProgress(declaration: ExportsDeclaration, message: String): Unit =
    logger.info(s"Declaration [${declaration.id}]: $message")
}
