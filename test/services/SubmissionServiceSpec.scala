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

import base.{CustomExportsBaseSpec, TestHelper}
import forms.Choice
import forms.Choice.AllowedChoiceValues
import metrics.MetricIdentifiers
import models.declaration.SupplementaryDeclarationDataSpec._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import services.audit.EventData._
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SubmissionServiceSpec extends CustomExportsBaseSpec with OptionValues {

  val mockAuditService = mock[AuditService]
  before {
    reset(mockCustomsCacheService)
    reset(mockCustomsDeclareExportsConnector)
    reset(mockAuditService)
    successfulCustomsDeclareExportsResponse()
    when(mockCustomsCacheService.remove(anyString())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK)))
  }

  implicit val request = TestHelper.journeyRequest(FakeRequest("", ""), AllowedChoiceValues.SupplementaryDec)

  val auditData = Map(
    EORI.toString -> request.authenticatedRequest.user.eori,
    LRN.toString -> "123LRN",
    DUCR.toString -> "8GB123456789012-1234567890QWERTYUIO",
    DecType.toString -> "SMP",
    SubmissionResult.toString -> "Success"
  )
  val submissionService = new SubmissionService(
    appConfig,
    mockCustomsCacheService,
    mockCustomsDeclareExportsConnector,
    mockAuditService,
    metrics
  )

  "SubmissionService" should {

    "submit cached data to backend" in {
      val result = submissionService.submit(cacheMapAllRecords).futureValue
      result.value mustBe "123LRN"
      verify(mockCustomsCacheService, times(1)).remove(any())(any(), any())

    }
    "handle success response" in {
      val result = submissionService.submit(cacheMapAllRecords).futureValue
      result.value mustBe "123LRN"
      verify(mockCustomsDeclareExportsConnector, times(1)).submitExportDeclaration(any(), any(), any())(any(), any())
    }

    "handle failure response" in {
      customsDeclaration400Response()
      val result = submissionService.submit(cacheMapAllRecords).futureValue
      result mustBe None
      verify(mockCustomsDeclareExportsConnector, times(1)).submitExportDeclaration(any(), any(), any())(any(), any())
    }

    "audit a submission" in {
      val result = submissionService.submit(cacheMapAllRecords).futureValue
      result.value mustBe "123LRN"
      verify(mockCustomsDeclareExportsConnector, times(1)).submitExportDeclaration(any(), any(), any())(any(), any())
      verify(mockAuditService, times(1)).audit(any(), any())(any())
      verify(mockAuditService, times(1)).auditAllPagesUserInput(any())(any())
      verify(mockAuditService)
        .audit(ArgumentMatchers.eq(AuditTypes.Submission), ArgumentMatchers.eq[Map[String, String]](auditData))(any())
    }

    "record submission timing and increase the Success Counter when response is OK" in {
      val timer = metrics.timers(MetricIdentifiers.submissionMetric).getCount
      val counter = metrics.counters(MetricIdentifiers.submissionMetric).getCount

      val result = submissionService.submit(cacheMapAllRecords).futureValue
      result.value mustBe "123LRN"

      metrics.timers(MetricIdentifiers.submissionMetric).getCount mustBe timer + 1
      metrics.counters(MetricIdentifiers.submissionMetric).getCount mustBe counter + 1
    }
  }
}
