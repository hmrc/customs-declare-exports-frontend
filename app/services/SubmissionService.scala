/*
 * Copyright 2019 HM Revenue & Customs
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
import config.AppConfig
import connectors.CustomsDeclareExportsConnector
import forms.declaration.LegalDeclaration
import javax.inject.Singleton
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.ExportsDeclaration
import play.api.Logger
import services.audit.EventData._
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject()(
  appConfig: AppConfig,
  exportsConnector: CustomsDeclareExportsConnector,
  auditService: AuditService,
  exportsMetrics: ExportsMetrics
) {

  private val logger = Logger(this.getClass)

  def submit(eori: String, exportsDeclaration: ExportsDeclaration, legalDeclaration: LegalDeclaration)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[String]] = {
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
            auditData(eori, exportsDeclaration.choice, exportsDeclaration.lrn, exportsDeclaration.ducr, legalDeclaration, Success.toString)
          )
          exportsMetrics.incrementCounter(submissionMetric)
          timerContext.stop()
        case Failure(exception) =>
          logProgress(exportsDeclaration, "Submission Failed")
          logger.error(s"Error response from backend $exception")
          auditService.audit(
            AuditTypes.Submission,
            auditData(eori, exportsDeclaration.choice, exportsDeclaration.lrn, exportsDeclaration.ducr, legalDeclaration, Failure.toString)
          )
      }
      .map(_ => exportsDeclaration.lrn)
  }

  private def logProgress(declaration: ExportsDeclaration, message: String): Unit =
    logger.info(s"Declaration [${declaration.id}]: $message")

  private def auditData(eori: String, choice: String, lrn: Option[String], ducr: Option[String], legalDeclaration: LegalDeclaration, result: String) =
    Map(
      EORI.toString -> eori,
      DecType.toString -> choice,
      LRN.toString -> lrn.getOrElse(""),
      DUCR.toString -> ducr.getOrElse(""),
      FullName.toString -> legalDeclaration.fullName,
      JobRole.toString -> legalDeclaration.jobRole,
      Email.toString -> legalDeclaration.email,
      Confirmed.toString -> legalDeclaration.confirmation.toString,
      SubmissionResult.toString -> result
    )

}
