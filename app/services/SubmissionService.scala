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
import models.requests.JourneyRequest
import models.{DeclarationStatus, ExportsDeclaration}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import services.audit.EventData._
import services.audit.{AuditService, AuditTypes}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject()(
  appConfig: AppConfig,
  cacheService: ExportsCacheService,
  exportsConnector: CustomsDeclareExportsConnector,
  auditService: AuditService,
  exportsMetrics: ExportsMetrics
) {

  private val logger = Logger(this.getClass)

  def submit(
    exportsDeclaration: ExportsDeclaration,
    legalDeclaration: LegalDeclaration
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    val timerContext = exportsMetrics.startTimer(submissionMetric)
    auditService.auditAllPagesUserInput(getCachedData(exportsDeclaration))

    val completedDeclaration = if(exportsDeclaration.isComplete) {
      Future.successful(exportsDeclaration)
    } else {
      exportsConnector.updateDeclaration(exportsDeclaration.copy(status = DeclarationStatus.COMPLETE))
    }

    completedDeclaration.flatMap { declaration =>
        exportsConnector.submitDeclaration(declaration.id.get).andThen {
          case Success(_) =>
            auditService.audit(
              AuditTypes.Submission,
              auditData(exportsDeclaration.lrn, exportsDeclaration.ducr, legalDeclaration, Success.toString)
            )
            exportsMetrics.incrementCounter(submissionMetric)
            timerContext.stop()
          case Failure(exception) =>
            logger.error(s"Error response from backend $exception")
            auditService.audit(
              AuditTypes.Submission,
              auditData(exportsDeclaration.lrn, exportsDeclaration.ducr, legalDeclaration, Failure.toString)
            )
        }.map(_ => declaration.lrn)
    }
  }

  private def auditData(lrn: Option[String], ducr: Option[String], legalDeclaration: LegalDeclaration, result: String)(
    implicit request: JourneyRequest[_]
  ) =
    Map(
      EORI.toString -> request.authenticatedRequest.user.eori,
      DecType.toString -> request.choice.value,
      LRN.toString -> lrn.getOrElse(""),
      DUCR.toString -> ducr.getOrElse(""),
      FullName.toString -> legalDeclaration.fullName,
      JobRole.toString -> legalDeclaration.jobRole,
      Email.toString -> legalDeclaration.email,
      Confirmed.toString -> legalDeclaration.confirmation.toString,
      SubmissionResult.toString -> result
    )

  def getCachedData(exportsCacheModel: ExportsDeclaration)(implicit request: JourneyRequest[_]): JsObject =
    Json.toJson(exportsCacheModel).as[JsObject]

  protected case class FormattedData(lrn: Option[String], ducr: Option[String], payload: String)

}
