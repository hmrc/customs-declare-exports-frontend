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

import base.{Injector, TestHelper}
import config.AppConfig
import models.declaration.SupplementaryDeclarationTestData.{allRecordsXmlMarshallingTest, cancellationDeclarationTest}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsObject, Json}
import services.audit.EventData.{EORI, SubmissionResult}
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends AuditTestSupport {

  override def beforeEach(): Unit = {
    super.beforeEach()
    mockSendEvent()
    mockSendCompletePayload(extendedDataEvent = extendedSubmissionEvent)
  }

  "AuditService" should {

    "audit an event" in {

      auditService.audit(AuditTypes.Submission, auditData)(hc)
      verify(mockAuditConnector).sendEvent(ArgumentMatchers.refEq(event, "eventId", "generatedAt"))(any(), any())
    }

    "audit full payload" in {

      auditService.auditAllPagesUserInput(AuditTypes.SubmissionPayload, allRecordsXmlMarshallingTest)(hc)
      verify(mockAuditConnector).sendExtendedEvent(
        ArgumentMatchers.refEq(extendedSubmissionEvent, "eventId", "generatedAt")
      )(any(), any())
    }

    "audit Cancellation payload" in {
      mockSendCompletePayload(extendedDataEvent = extendedCancellationEvent)
      auditService.auditAllPagesDeclarationCancellation(cancellationDeclarationTest)(hc)
      verify(mockAuditConnector).sendExtendedEvent(
        ArgumentMatchers.refEq(extendedSubmissionEvent, "eventId", "generatedAt")
      )(any(), any())
    }

    "audit full payload success" in {

      val res =
        auditService
          .auditAllPagesUserInput(AuditTypes.SubmissionPayload, allRecordsXmlMarshallingTest)(hc)
          .futureValue

      res mustBe AuditResult.Success
    }

    "audit with a success" in {
      val res = auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue

      res mustBe AuditResult.Success
    }

    "handle audit failure" in {

      mockSendEvent(result = auditFailure)

      val res = auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue

      res mustBe auditFailure
    }

    "handled audit disabled" in {

      mockSendEvent(result = Disabled)

      val res = auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue

      res mustBe AuditResult.Disabled
    }
  }
}

trait AuditTestSupport
    extends UnitSpec with ExportsDeclarationBuilder with ScalaFutures with BeforeAndAfterEach with Injector {
  val mockAuditConnector = mock[AuditConnector]

  val auditData = Map(
    EORI.toString -> "eori1",
    LRN.toString -> "lrn1", // FIXME refere to bad thing
    DUCR.toString -> "ducr1",
    SubmissionResult.toString -> "Success"
  )

  val appConfig = instanceOf[AppConfig]
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestHelper.createRandomString(255))))

  private val auditCarrierDetails: Map[String, String] = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()

  val event = DataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Submission.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"Export-Declaration-${AuditTypes.Submission.toString}-request",
        path = s"customs-declare-exports/${AuditTypes.Submission.toString}"
      ),
    detail = auditCarrierDetails ++ auditData
  )

  private val declarationAsJson: JsObject = Json.toJson(allRecordsXmlMarshallingTest).as[JsObject]
  val extendedSubmissionEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.SubmissionPayload.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"Export-Declaration-${AuditTypes.SubmissionPayload.toString}-payload-request",
        path = s"customs-declare-exports/${AuditTypes.SubmissionPayload.toString}/full-payload"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(declarationAsJson)
  )

  private val cancelDeclarationAsJson: JsObject = Json.toJson(cancellationDeclarationTest).as[JsObject]
  val extendedCancellationEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Cancellation.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"Export-Declaration-${AuditTypes.Cancellation.toString}-payload-request",
        path = s"customs-declare-exports/${AuditTypes.Cancellation.toString}/full-payload"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(cancelDeclarationAsJson)
  )

  val auditFailure = Failure("Event sending failed")

  val auditService = new AuditService(mockAuditConnector, appConfig)(global)

  def mockSendEvent(
    eventToAudit: DataEvent = event,
    result: AuditResult = Success
  ): OngoingStubbing[Future[AuditResult]] =
    when(
      mockAuditConnector.sendEvent(ArgumentMatchers.refEq(eventToAudit, "eventId", "generatedAt"))(
        ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext]
      )
    ).thenReturn(Future.successful(result))

  def mockSendCompletePayload(
    result: AuditResult = Success,
    extendedDataEvent: ExtendedDataEvent
  ): OngoingStubbing[Future[AuditResult]] =
    when(
      mockAuditConnector.sendExtendedEvent(ArgumentMatchers.refEq(extendedDataEvent, "eventId", "generatedAt"))(
        ArgumentMatchers.any[HeaderCarrier],
        ArgumentMatchers.any[ExecutionContext]
      )
    ).thenReturn(Future.successful(result))
}
