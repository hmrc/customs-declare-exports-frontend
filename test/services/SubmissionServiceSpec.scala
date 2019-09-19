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

import base.{Injector, MockConnectors, MockExportCacheService, TestHelper}
import com.kenshoo.play.metrics.Metrics
import config.AppConfig
import forms.Choice.AllowedChoiceValues
import forms.declaration.LegalDeclaration
import metrics.{ExportsMetrics, MetricIdentifiers}
import models.{DeclarationStatus, ExportsDeclaration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.test.FakeRequest
import services.audit.{AuditService, AuditTypes, EventData}
import services.cache.SubmissionBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class SubmissionServiceSpec
    extends UnitSpec with MockExportCacheService with MockConnectors with ScalaFutures with OptionValues with Injector with SubmissionBuilder {

  val mockAuditService = mock[AuditService]

  val appConfig = instanceOf[AppConfig]

  val exportMetrics = instanceOf[ExportsMetrics]

  val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(TestHelper.createRandomString(255))))

  val request = TestHelper.journeyRequest(FakeRequest("", ""), AllowedChoiceValues.SupplementaryDec)

  val legal = LegalDeclaration("Name", "Role", "email@test.com", true)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockExportsCacheService, mockCustomsDeclareExportsConnector, mockAuditService)
  }

  val auditData = Map(
    EventData.EORI.toString -> request.authenticatedRequest.user.eori,
    EventData.LRN.toString -> "123LRN",
    EventData.DUCR.toString -> "ducr",
    EventData.DecType.toString -> "SMP",
    EventData.FullName.toString -> legal.fullName,
    EventData.JobRole.toString -> legal.jobRole,
    EventData.Email.toString -> legal.email,
    EventData.Confirmed.toString -> legal.confirmation.toString,
    EventData.SubmissionResult.toString -> "Success"
  )
  val submissionService = new SubmissionService(
    appConfig,
    mockExportsCacheService,
    mockCustomsDeclareExportsConnector,
    mockAuditService,
    exportMetrics
  )

  def theExportsDeclarationSubmitted: ExportsDeclaration = {
    val captor: ArgumentCaptor[ExportsDeclaration] = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
    verify(mockCustomsDeclareExportsConnector)
      .updateDeclaration(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
    captor.getValue
  }

  "SubmissionService" should {

    "submit cached data to backend" in {

      val declaration = aDeclaration(withId(), withStatus(DeclarationStatus.DRAFT), withConsignmentReferences(ducr = "ducr", lrn = "123LRN"))

      val completed = declaration.copy(status = DeclarationStatus.COMPLETE)

      val submission = emptySubmission(completed, "12345")

      when(mockCustomsDeclareExportsConnector.updateDeclaration(any[ExportsDeclaration])(any(), any()))
        .thenReturn(Future.successful(completed))

      when(mockCustomsDeclareExportsConnector.submitDeclaration(any[String])(any(), any()))
        .thenReturn(Future.successful(submission))

      val registry = instanceOf[Metrics].defaultRegistry
      val metric = MetricIdentifiers.submissionMetric
      val timerBefore = registry.getTimers.get(exportMetrics.timerName(metric)).getCount
      val counterBefore = registry.getCounters.get(exportMetrics.counterName(metric)).getCount
      val model = aDeclaration(withConsignmentReferences(ducr = "ducr", lrn = "123LRN"))

      val result = submissionService.submit(declaration, legal)(request, hc, global).futureValue

      result.value mustBe "123LRN"

      theExportsDeclarationSubmitted.status mustBe DeclarationStatus.COMPLETE
      verify(mockAuditService, times(1)).audit(any(), any())(any())
      verify(mockAuditService, times(1)).auditAllPagesUserInput(any())(any())
      verify(mockAuditService)
        .audit(ArgumentMatchers.eq(AuditTypes.Submission), ArgumentMatchers.eq[Map[String, String]](auditData))(any())
      registry.getTimers.get(exportMetrics.timerName(metric)).getCount mustBe >(timerBefore)
      registry.getCounters.get(exportMetrics.counterName(metric)).getCount mustBe >(counterBefore)
    }

    "propagate errors from exports connector" in {

      val error = new RuntimeException("some error")
      when(mockCustomsDeclareExportsConnector.updateDeclaration(any[ExportsDeclaration])(any(), any())).thenThrow(error)

      val model = aDeclaration(withStatus(DeclarationStatus.DRAFT), withConsignmentReferences(ducr = "ducr", lrn = "123LRN"))

      intercept[Exception](submissionService.submit(model, legal)(request, hc, global)) mustBe error
    }
  }
}
