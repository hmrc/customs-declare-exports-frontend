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

package services.audit

import base.CustomExportsBaseSpec
import models.declaration.SupplementaryDeclarationDataSpec.cacheMapAllRecords
import org.mockito.ArgumentMatchers
import org.scalatest.OptionValues
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import org.mockito.Mockito.{verify, when}
import services.audit.EventData.{DUCR, EORI, LRN, SubmissionResult}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends AuditTestSupport with OptionValues {

  before {
    mockSendEvent()
    mockSendCompletePayload()
  }
  "AuditService" should {

    "audit an event" in {
      auditService.audit(AuditTypes.Submission, auditData)
      verify(mockAuditConnector).sendEvent(ArgumentMatchers.refEq(event, "eventId", "generatedAt"))(any(), any())
    }

    "audit full payload" in {
      auditService.auditAllPagesUserInput(cacheMapAllRecords)
      verify(mockAuditConnector).sendExtendedEvent(ArgumentMatchers.refEq(extendedEvent, "eventId", "generatedAt"))(
        any(),
        any()
      )

    }
    "audit full payload success" in {
      val res = auditService.auditAllPagesUserInput(cacheMapAllRecords).futureValue
      res mustBe AuditResult.Success

    }
    "audit with a success" in {
      val res = auditService.audit(AuditTypes.Submission, auditData).futureValue
      res mustBe AuditResult.Success
    }
    "handle audit failure" in {
      mockSendEvent(result = auditFailure)
      val res = auditService.audit(AuditTypes.Submission, auditData).futureValue
      res mustBe auditFailure
    }

    "handled audit disabled" in {
      mockSendEvent(result = Disabled)
      val res = auditService.audit(AuditTypes.Submission, auditData).futureValue
      res mustBe AuditResult.Disabled
    }

  }

}

trait AuditTestSupport extends CustomExportsBaseSpec {
  val mockAuditConnector = mock[AuditConnector]

  val auditData = Map(
    EORI.toString -> "eori1",
    LRN.toString -> "lrn1",
    DUCR.toString -> "ducr1",
    SubmissionResult.toString -> "Success"
  )

  val event = DataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Submission.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"Export-Declaration-${AuditTypes.Submission.toString}-request",
        path = s"customs-declare-exports/${AuditTypes.Submission.toString}"
      ),
    detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails() ++ auditData
  )

  val extendedEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.SubmissionPayload.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"Export-Declaration-${AuditTypes.SubmissionPayload.toString}-payload-request",
        path = s"customs-declare-exports/${AuditTypes.SubmissionPayload.toString}/full-payload"
      ),
    detail = Json
      .toJson(AuditExtensions.auditHeaderCarrier(hc).toAuditDetails())
      .as[JsObject]
      .deepMerge(Json.toJson(cacheMapAllRecords.data).as[JsObject])
  )

  val auditFailure = Failure("Event sending failed")

  val auditService = new AuditService(mockAuditConnector, appConfig)

  def mockSendEvent(evenToAudit: DataEvent = event, result: AuditResult = Success) =
    when(
      mockAuditConnector.sendEvent(ArgumentMatchers.refEq(evenToAudit, "eventId", "generatedAt"))(
        ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext]
      )
    ) thenReturn Future.successful(result)

  def mockSendCompletePayload(result: AuditResult = Success) =
    when(
      mockAuditConnector.sendExtendedEvent(ArgumentMatchers.refEq(extendedEvent, "eventId", "generatedAt"))(
        ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext]
      )
    ) thenReturn Future.successful(result)
}
