/*
 * Copyright 2021 HM Revenue & Customs
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

import base.{Injector, MockConnectors, MockExportCacheService, UnitWithMocksSpec}
import com.kenshoo.play.metrics.Metrics
import connectors.CustomsDeclareExportsConnector
import forms.declaration.LegalDeclaration
import metrics.{ExportsMetrics, MetricIdentifiers}
import models.{DeclarationStatus, DeclarationType}
import models.declaration.submissions.{Action, Submission}
import org.mockito.ArgumentMatchers.{any, eq => equalTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import services.audit.{AuditService, AuditTypes, EventData}
import services.cache.SubmissionBuilder
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class SubmissionServiceSpec
    extends UnitWithMocksSpec with MockExportCacheService with MockConnectors with ScalaFutures with OptionValues with Injector
    with SubmissionBuilder {

  private val auditService = mock[AuditService]
  private val connector = mock[CustomsDeclareExportsConnector]
  private val exportMetrics = instanceOf[ExportsMetrics]
  private val hc: HeaderCarrier = mock[HeaderCarrier]
  private val legal = LegalDeclaration("Name", "Role", "email@test.com", confirmation = true)
  private val auditData = Map(
    EventData.eori.toString -> "eori",
    EventData.lrn.toString -> "123LRN",
    EventData.ducr.toString -> "ducr",
    EventData.decType.toString -> "STANDARD",
    EventData.fullName.toString -> legal.fullName,
    EventData.jobRole.toString -> legal.jobRole,
    EventData.email.toString -> legal.email,
    EventData.confirmed.toString -> legal.confirmation.toString,
    EventData.submissionResult.toString -> "Success"
  )
  private val submissionService = new SubmissionService(connector, auditService, exportMetrics)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector, auditService)
  }

  "SubmissionService" should {
    val registry = instanceOf[Metrics].defaultRegistry
    val metric = MetricIdentifiers.submissionMetric
    val timerBefore = registry.getTimers.get(exportMetrics.timerName(metric)).getCount
    val counterBefore = registry.getCounters.get(exportMetrics.counterName(metric)).getCount

    "submit to the back end" when {
      "valid declaration" in {
        // Given
        val declaration =
          aDeclaration(
            withId("id"),
            withStatus(DeclarationStatus.DRAFT),
            withType(DeclarationType.STANDARD),
            withConsignmentReferences(ducr = "ducr", lrn = "123LRN")
          )
        val submission = Submission(uuid = "id", eori = "eori", lrn = "lrn", actions = Seq.empty[Action])
        when(connector.submitDeclaration(any[String])(any(), any())).thenReturn(Future.successful(submission))

        // When
        submissionService.submit("eori", declaration, legal)(hc, global).futureValue.value mustBe "123LRN"

        // Then
        verify(connector).submitDeclaration(equalTo("id"))(equalTo(hc), any())
        verify(auditService).auditAllPagesUserInput(equalTo(AuditTypes.SubmissionPayload), equalTo(declaration))(equalTo(hc))
        verify(auditService).audit(equalTo(AuditTypes.Submission), equalTo[Map[String, String]](auditData))(equalTo(hc))
        registry.getTimers.get(exportMetrics.timerName(metric)).getCount mustBe >(timerBefore)
        registry.getCounters.get(exportMetrics.counterName(metric)).getCount mustBe >(counterBefore)
      }
    }
  }
}
