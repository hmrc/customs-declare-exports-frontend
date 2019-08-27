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
import models.{DeclarationStatus, ExportsDeclaration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import play.api.test.FakeRequest
import services.audit.{AuditService, AuditTypes, EventData}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec extends CustomExportsBaseSpec {

  val mockAuditService = mock[AuditService]

  override def beforeEach(): Unit =
    reset(mockExportsCacheService, mockCustomsDeclareExportsConnector, mockAuditService)

  implicit val request = TestHelper.journeyRequest(FakeRequest("", ""), AllowedChoiceValues.SupplementaryDec)

  val auditData = Map(
    EventData.EORI.toString -> request.authenticatedRequest.user.eori,
    EventData.LRN.toString -> "123LRN",
    EventData.DUCR.toString -> "ducr",
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

  def theExportsDeclarationSubmitted: ExportsDeclaration = {
    val captor: ArgumentCaptor[ExportsDeclaration] = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockCustomsDeclareExportsConnector)
      .updateDeclaration(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
    captor.getValue
  }

  "SubmissionService" should {
    val submittedDeclaration = mock[ExportsDeclaration]

    "submit cached data to backend" in {

      when(mockCustomsDeclareExportsConnector.updateDeclaration(any[ExportsDeclaration])(any(), any()))
        .thenReturn(Future.successful(submittedDeclaration))

      val registry = app.injector.instanceOf[Metrics].defaultRegistry
      val metric = MetricIdentifiers.submissionMetric
      val timerBefore = registry.getTimers.get(exportsMetricsMock.timerName(metric)).getCount
      val counterBefore = registry.getCounters.get(exportsMetricsMock.counterName(metric)).getCount
      val model = aDeclaration(withConsignmentReferences(ducr = "ducr", lrn = "123LRN"))

      val result = submissionService.submit(model).futureValue

      result.value mustBe "123LRN"

      theExportsDeclarationSubmitted.status mustBe DeclarationStatus.COMPLETE
      verify(mockAuditService, times(1)).audit(any(), any())(any())
      verify(mockAuditService, times(1)).auditAllPagesUserInput(any())(any())
      verify(mockAuditService)
        .audit(ArgumentMatchers.eq(AuditTypes.Submission), ArgumentMatchers.eq[Map[String, String]](auditData))(any())
      registry.getTimers.get(exportsMetricsMock.timerName(metric)).getCount mustBe >(timerBefore)
      registry.getCounters.get(exportsMetricsMock.counterName(metric)).getCount mustBe >(counterBefore)
    }

    "propagate errors from exports connector" in {

      val error = new RuntimeException("some error")
      when(mockCustomsDeclareExportsConnector.updateDeclaration(any[ExportsDeclaration])(any(), any())).thenThrow(error)

      val model = aDeclaration(withConsignmentReferences(ducr = "ducr", lrn = "123LRN"))

      intercept[Exception](submissionService.submit(model)) mustBe error
    }
  }
}
