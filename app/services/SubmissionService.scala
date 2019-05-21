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
import controllers.util.CacheIdGenerator.cacheId
import forms.Choice
import javax.inject.Singleton
import metrics.ExportsMetrics
import metrics.MetricIdentifiers.submissionMetric
import models.requests.JourneyRequest
import play.api.Logger
import play.api.http.Status.ACCEPTED
import services.audit.EventData._
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  appConfig: AppConfig,
  cacheService: CustomsCacheService,
  exportsConnector: CustomsDeclareExportsConnector,
  auditService: AuditService,
  exportsMetrics: ExportsMetrics
) {

  private val logger = Logger(this.getClass())

  def submit(
    cacheMap: CacheMap
  )(implicit request: JourneyRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {

    val timerContext = exportsMetrics.startTimer(submissionMetric)
    val data = format(cacheMap, request.choice)
    auditService.auditAllPagesUserInput(cacheMap)
    exportsConnector.submitExportDeclaration(data.ducr, data.lrn, data.payload).flatMap {
      case HttpResponse(ACCEPTED, _, _, _) =>
        cacheService.remove(cacheId).map { _ =>
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

  private def format(cacheMap: CacheMap, choice: Choice): FormattedData = {
    val mapper = appConfig.wcoMetadataMapper
    val metaData = mapper.getMetaData(cacheMap, choice)

    val lrn = mapper.getDeclarationLrn(metaData)
    val ducr = mapper.getDeclarationDucr(metaData)
    val payload = mapper.serialise(metaData)

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

  protected case class FormattedData(lrn: Option[String], ducr: Option[String], payload: String)

}
