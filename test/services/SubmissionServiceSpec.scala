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
import com.kenshoo.play.metrics.Metrics
import forms.Choice.AllowedChoiceValues
import metrics.MetricIdentifiers
import models.ExportsDeclaration
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.test.FakeRequest
import reactivemongo.play.json.collection.JSONBatchCommands
import services.audit.{AuditService, AuditTypes, EventData}
import services.cache.ExportsDeclarationBuilder

import scala.concurrent.Future

class SubmissionServiceSpec extends CustomExportsBaseSpec with OptionValues with ExportsDeclarationBuilder {

  val mockAuditService = mock[AuditService]
  val sessionId = "123456"

  override def beforeEach() {
    reset(mockExportsCacheService, mockCustomsDeclareExportsConnector, mockAuditService)
    successfulCustomsDeclareExportsResponse()

    val mockResult = mock[JSONBatchCommands.FindAndModifyCommand.FindAndModifyResult]

    when(mockExportsCacheService.remove(any[String])).thenReturn(Future.successful(mockResult))
  }

  implicit val request = TestHelper.journeyRequest(FakeRequest("", ""), AllowedChoiceValues.SupplementaryDec)

  val auditData = Map(
    EventData.EORI.toString -> request.authenticatedRequest.user.eori,
    EventData.LRN.toString -> "123LRN",
    EventData.DUCR.toString -> "8GB123456789012-1234567890QWERTYUIO",
    EventData.DecType.toString -> "SMP",
    EventData.SubmissionResult.toString -> "Success"
  )
  val submissionService = new SubmissionService(
    appConfig,
    mockExportsCacheService,
    mockCustomsDeclareExportsConnector,
    mockAuditService,
    exportsMetricsMock
  )

  "SubmissionService" should {

    "submit cached data to backend" in {
      val registry = app.injector.instanceOf[Metrics].defaultRegistry

      val metric = MetricIdentifiers.submissionMetric
      val timerBefore = registry.getTimers.get(exportsMetricsMock.timerName(metric)).getCount
      val counterBefore = registry.getCounters.get(exportsMetricsMock.counterName(metric)).getCount

      val model = createFullModel()
      val result = submissionService
        .submit(sessionId, model)
        .futureValue
      result.value mustBe "123LRN"

      verify(mockExportsCacheService, times(1)).remove(any[String])
      verify(mockCustomsDeclareExportsConnector, times(1)).createDeclaration(refEq(model))(any(), any())

      verify(mockAuditService, times(1)).audit(any(), any())(any())
      verify(mockAuditService, times(1)).auditAllPagesUserInput(any())(any())
      verify(mockAuditService)
        .audit(ArgumentMatchers.eq(AuditTypes.Submission), ArgumentMatchers.eq[Map[String, String]](auditData))(any())

      registry.getTimers.get(exportsMetricsMock.timerName(metric)).getCount mustBe >(timerBefore)
      registry.getCounters.get(exportsMetricsMock.counterName(metric)).getCount mustBe >(counterBefore)
    }

  }

  private def createFullModel(): ExportsDeclaration =
    aDeclaration(withConsignmentReferences(ducr = Some("8GB123456789012-1234567890QWERTYUIO"), lrn = "123LRN"))
}
