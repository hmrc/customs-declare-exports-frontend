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

package services.audit

import base.{ExportsTestData, Injector, TestHelper, UnitWithMocksSpec}
import config.AppConfig
import config.featureFlags.SecureMessagingConfig
import models.AuthKey.enrolment
import models.declaration.ExportDeclarationTestData.{allRecordsXmlMarshallingTest, cancellationDeclarationTest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.libs.json.{JsObject, JsString, Json}
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends AuditTestSupport with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    reset(auditConnector)
    super.afterEach()
  }

  "AuditService" should {

    "audit an event" in {
      mockDataEvent()
      auditService.audit(AuditTypes.Submission, auditData)(hc)
      verifyDataEvent(event)
    }

    "audit full payload" in {
      mockExtendedDataEvent
      auditService.auditAllPagesUserInput(AuditTypes.SubmissionPayload, allRecordsXmlMarshallingTest)(hc)
      verifyExtendedDataEvent(extendedSubmissionEvent)
    }

    "audit Amendment payload" in {
      mockExtendedDataEvent
      auditService.auditAmendmentSent(AuditTypes.Amendment, amendmentJson)(hc)
      verifyExtendedDataEvent(extendedAmendmentEvent)
    }

    "audit Cancellation payload" in {
      mockExtendedDataEvent
      auditService.auditAllPagesDeclarationCancellation(cancellationDeclarationTest)(hc)
      verifyExtendedDataEvent(extendedCancellationEvent)
    }

    "audit the successful retrieval of the message inbox partial" in {
      mockExtendedDataEvent
      auditService.auditMessageInboxPartialRetrieved(ExportsTestData.eori, secureMessagingConfig.notificationType, secureMessagingConfig.fetchInbox)(
        hc
      )
      verifyExtendedDataEvent(eventForMessageInboxPartialRetrieved)
    }

    "audit full payload success" in {
      mockExtendedDataEvent
      val res = auditService.auditAllPagesUserInput(AuditTypes.SubmissionPayload, allRecordsXmlMarshallingTest)(hc).futureValue
      res mustBe AuditResult.Success
    }

    "audit with a success" in {
      mockDataEvent()
      auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue mustBe AuditResult.Success
    }

    "handle audit failure" in {
      mockDataEvent(auditFailure)
      auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue mustBe auditFailure
    }

    "handled audit disabled" in {
      mockDataEvent(Disabled)
      auditService.audit(AuditTypes.Submission, auditData)(hc).futureValue mustBe AuditResult.Disabled
    }
  }
}

trait AuditTestSupport extends UnitWithMocksSpec with ExportsDeclarationBuilder with ScalaFutures with Injector {

  val auditConnector = mock[AuditConnector]

  val auditData = Map(
    EventData.eori.toString -> "eori1",
    EventData.lrn.toString -> "lrn1",
    EventData.ducr.toString -> "ducr1",
    EventData.submissionResult.toString -> "Success"
  )

  val appConfig = instanceOf[AppConfig]
  val secureMessagingConfig = instanceOf[SecureMessagingConfig]
  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestHelper.createRandomString(255))))

  private val auditCarrierDetails: Map[String, String] = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails()

  val event = DataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Submission.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${AuditTypes.Submission.toString.toLowerCase}-request",
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
        transactionName = s"export-declaration-${AuditTypes.SubmissionPayload.toString.toLowerCase}-payload-request",
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
    auditType = AuditTypes.CancellationPayload.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${AuditTypes.CancellationPayload.toString.toLowerCase}-payload-request",
        path = s"customs-declare-exports/${AuditTypes.CancellationPayload.toString}/full-payload"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(cancelDeclarationAsJson)
  )

  val eventForMessageInboxPartialRetrieved = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.NavigateToMessages.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(transactionName = "callExportPartial", path = secureMessagingConfig.fetchInbox),
    detail = Json.obj(
      "enrolment" -> enrolment,
      "eoriNumber" -> ExportsTestData.eori,
      "tags" -> Json.obj("notificationType" -> secureMessagingConfig.notificationType)
    )
  )

  val amendmentJson = new JsObject(
    Map(EventData.preAmendmentDeclaration.toString -> JsString("someDec"), EventData.postAmendmentDeclaration.toString -> JsString("otherDec"))
  )

  val extendedAmendmentEvent = ExtendedDataEvent(
    auditSource = appConfig.appName,
    auditType = AuditTypes.Amendment.toString,
    tags = AuditExtensions
      .auditHeaderCarrier(hc)
      .toAuditTags(
        transactionName = s"export-declaration-${AuditTypes.Amendment.toString.toLowerCase}-request",
        path = s"customs-declare-exports/${AuditTypes.Amendment.toString}"
      ),
    detail = Json
      .toJson(auditCarrierDetails)
      .as[JsObject]
      .deepMerge(amendmentJson)
  )

  val auditFailure = Failure("Event sending failed")

  val auditService = new AuditService(auditConnector, appConfig)(global)

  def mockDataEvent(result: AuditResult = Success): OngoingStubbing[Future[AuditResult]] =
    when(auditConnector.sendEvent(any[DataEvent])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(result))

  def mockExtendedDataEvent: OngoingStubbing[Future[AuditResult]] =
    when(auditConnector.sendExtendedEvent(any[ExtendedDataEvent])(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(Success))

  def verifyDataEvent(expected: DataEvent): Assertion = {
    val captor = ArgumentCaptor.forClass(classOf[DataEvent])
    verify(auditConnector).sendEvent(captor.capture())(any(), any())

    val actual: DataEvent = captor.getValue
    actual.auditSource mustBe expected.auditSource
    actual.auditType mustBe expected.auditType
    actual.tags mustBe expected.tags
    actual.detail mustBe expected.detail
  }

  def verifyExtendedDataEvent(expected: ExtendedDataEvent): Assertion = {
    val captor = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
    verify(auditConnector).sendExtendedEvent(captor.capture())(any(), any())

    val actual: ExtendedDataEvent = captor.getValue
    actual.auditSource mustBe expected.auditSource
    actual.auditType mustBe expected.auditType
    actual.tags mustBe expected.tags
    actual.detail mustBe expected.detail
  }
}
