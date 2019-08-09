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
import javax.inject.Singleton
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.Logger
import play.api.http.Status.ACCEPTED
import play.api.libs.json.{JsObject, Json}
import services.audit.EventData._
import services.audit.{AuditService, AuditTypes}
import services.cache.ExportsCacheService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  appConfig: AppConfig,
  cacheService: ExportsCacheService,
  exportsConnector: CustomsDeclareExportsConnector,
  auditService: AuditService,
  exportsMetrics: ExportsMetrics,
  mapper: WcoMetadataMapper
) {

  private val logger = Logger(this.getClass())

  def submit(
    sessionId: String,
    exportsDeclaration: ExportsDeclaration
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {

    val timerContext = exportsMetrics.startTimer(submissionMetric)
    val data = format(exportsDeclaration)
    auditService.auditAllPagesUserInput(getCachedData(exportsDeclaration))
    (for {
      _ <- exportsConnector.submit(exportsDeclaration)
      response <- exportsConnector.submitExportDeclaration(data.ducr, data.lrn, data.payload)
    } yield response) flatMap  {
        case HttpResponse(ACCEPTED, _, _, _) =>
          cacheService
            .remove(sessionId)
            .map { _ =>
              auditService.audit(AuditTypes.Submission, auditData(data.lrn, data.ducr, Success.toString))
              exportsMetrics.incrementCounter(submissionMetric)
              timerContext.stop()
              data.lrn
            }

        case error =>
          logger.error(s"Error response from backend ${error.body}")
          auditService.audit(AuditTypes.Submission, auditData(data.lrn, data.ducr, Failure.toString))
          Future.successful(None)
      }
  }

  private def format(exportsCacheModel: ExportsDeclaration): FormattedData = {
    val metaData = mapper.produceMetaData(exportsCacheModel)

    val lrn = mapper.declarationLrn(metaData)
    val ducr = mapper.declarationUcr(metaData)
    val payload = mapper.toXml(metaData)

    FormattedData(lrn, ducr, payload)
  }

  private def auditData(lrn: Option[String], ducr: Option[String], result: String)(
    implicit request: JourneyRequest[_]
  ) =
    Map(
      EORI.toString -> request.authenticatedRequest.user.eori,
      DecType.toString -> request.choice.value,
      LRN.toString -> lrn.getOrElse(""),
      DUCR.toString -> ducr.getOrElse(""),
      SubmissionResult.toString -> result
    )

  def getCachedData(exportsCacheModel: ExportsDeclaration)(implicit request: JourneyRequest[_]): JsObject =
    Json.toJson(exportsCacheModel).as[JsObject]

  protected case class FormattedData(lrn: Option[String], ducr: Option[String], payload: String)
}
