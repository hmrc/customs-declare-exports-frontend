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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import com.google.inject.Inject
import connectors.CustomsDeclareExportsConnector
import forms.declaration.LegalDeclaration
import javax.inject.Singleton
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.DeclarationType.DeclarationType
import models.ExportsDeclaration
import models.declaration.submissions.Submission
import play.api.Logging
import services.audit.{AuditService, AuditTypes, EventData}
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class SubmissionService @Inject() (exportsConnector: CustomsDeclareExportsConnector, auditService: AuditService, exportsMetrics: ExportsMetrics)
    extends Logging {

  def submit(eori: String, exportsDeclaration: ExportsDeclaration, legalDeclaration: LegalDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[Submission]] = {
    val timerContext = exportsMetrics.startTimer(submissionMetric)
    auditService.auditAllPagesUserInput(AuditTypes.SubmissionPayload, exportsDeclaration)

    logProgress(exportsDeclaration, "Beginning Submission")
    exportsConnector
      .submitDeclaration(exportsDeclaration.id)
      .andThen {
        case Success(_) =>
          logProgress(exportsDeclaration, "Submitted Successfully")
          auditService.audit(
            AuditTypes.Submission,
            auditData(
              eori,
              exportsDeclaration.`type`,
              exportsDeclaration.lrn,
              exportsDeclaration.ducr.map(_.ducr),
              legalDeclaration,
              Success.toString
            )
          )
          exportsMetrics.incrementCounter(submissionMetric)
          timerContext.stop()

        case Failure(exception) =>
          logProgress(exportsDeclaration, "Submission Failed")
          logger.error(s"Error response from backend $exception")
          auditService.audit(
            AuditTypes.Submission,
            auditData(
              eori,
              exportsDeclaration.`type`,
              exportsDeclaration.lrn,
              exportsDeclaration.ducr.map(_.ducr),
              legalDeclaration,
              Failure.toString
            )
          )
      }
      .map(Some(_))
  }

  def amend(eori: String, exportsDeclaration: ExportsDeclaration, legalDeclaration: LegalDeclaration): Future[Option[Submission]] = Future.successful(None)

  private def logProgress(declaration: ExportsDeclaration, message: String): Unit =
    logger.info(s"Declaration [${declaration.id}]: $message")

  private def auditData(
    eori: String,
    `type`: DeclarationType,
    lrn: Option[String],
    ducr: Option[String],
    legalDeclaration: LegalDeclaration,
    result: String
  ) =
    Map(
      EventData.eori.toString -> eori,
      EventData.decType.toString -> `type`.toString,
      EventData.lrn.toString -> lrn.getOrElse(""),
      EventData.ducr.toString -> ducr.getOrElse(""),
      EventData.fullName.toString -> legalDeclaration.fullName,
      EventData.jobRole.toString -> legalDeclaration.jobRole,
      EventData.email.toString -> legalDeclaration.email,
      EventData.confirmed.toString -> legalDeclaration.confirmation.toString,
      EventData.submissionResult.toString -> result
    )

}
