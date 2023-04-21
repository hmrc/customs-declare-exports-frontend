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

package services

import base.{Injector, MockConnectors, MockExportCacheService, UnitWithMocksSpec}
import com.kenshoo.play.metrics.Metrics
import connectors.CustomsDeclareExportsConnector
import forms.declaration.LegalDeclaration
import forms.declaration.countries.Country
import metrics.{ExportsMetrics, MetricIdentifiers}
import models.declaration.submissions.{Action, Submission, SubmissionAmendment}
import models.DeclarationType
import models.declaration.DeclarationStatus
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
    extends UnitWithMocksSpec with Injector with MockExportCacheService with MockConnectors with OptionValues with ScalaFutures
    with SubmissionBuilder {

  private val auditService = mock[AuditService]
  private val connector = mock[CustomsDeclareExportsConnector]
  private val exportMetrics = instanceOf[ExportsMetrics]
  private val hc: HeaderCarrier = mock[HeaderCarrier]
  private val legal = LegalDeclaration("Name", "Role", "email@test.com", None, confirmation = true)
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

  val eori = "eori"
  val lrn = "123LRN"

  "SubmissionService.submitDeclaration" should {
    val registry = instanceOf[Metrics].defaultRegistry
    val metric = MetricIdentifiers.submissionMetric
    val timerBefore = registry.getTimers.get(exportMetrics.timerName(metric)).getCount
    val counterBefore = registry.getCounters.get(exportMetrics.counterName(metric)).getCount

    "successfully submit to the back end a valid declaration" in {
      // Given
      val declaration = aDeclaration(
        withId("id"),
        withStatus(DeclarationStatus.DRAFT),
        withType(DeclarationType.STANDARD),
        withConsignmentReferences(ducr = "ducr", lrn = lrn)
      )

      declaration.locations.originationCountry.value mustBe Country.GB

      val expectedSubmission = Submission(uuid = "id", eori = eori, lrn = lrn, actions = Seq.empty[Action], latestDecId = Some("id"))
      when(connector.submitDeclaration(any[String])(any(), any())).thenReturn(Future.successful(expectedSubmission))

      // When
      val actualSubmission = submissionService.submitDeclaration("eori", declaration, legal)(hc, global).futureValue.value
      actualSubmission.eori mustBe eori
      actualSubmission.lrn mustBe lrn

      // Then
      verify(connector).submitDeclaration(equalTo("id"))(equalTo(hc), any())
      verify(auditService).auditAllPagesUserInput(equalTo(AuditTypes.SubmissionPayload), equalTo(declaration))(equalTo(hc))
      verify(auditService).audit(equalTo(AuditTypes.Submission), equalTo[Map[String, String]](auditData))(equalTo(hc))
      registry.getTimers.get(exportMetrics.timerName(metric)).getCount mustBe >(timerBefore)
      registry.getCounters.get(exportMetrics.counterName(metric)).getCount mustBe >(counterBefore)
    }
  }

  "SubmissionService.submitAmendment" should {
    val submissionId = "submissionId"

    "return None" when {
      "the declaration's parentDeclarationId is not defined" in {
        submissionService
          .submitAmendment(eori, aDeclaration(), legal, submissionId, false)(hc, global)
          .futureValue mustBe None
      }
      "the declaration matching the parentDeclarationId is not found" in {

        when(connector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(None))

        val amendedDecl = aDeclaration(
          withId("id2"),
          withStatus(DeclarationStatus.AMENDMENT_DRAFT),
          withParentDeclarationId("id1"),
          withConsignmentReferences(ducr = "ducr", lrn = lrn),
          withDestinationCountry(Country(Some("IT"))),
          withTotalNumberOfItems(Some("654321"), Some("94.1"), Some("GBP"), Some("no"))
        )

        submissionService
          .submitAmendment(eori, amendedDecl, legal, submissionId, false)(hc, global)
          .futureValue mustBe None
      }
    }

    val registry = instanceOf[Metrics].defaultRegistry
    val metric = MetricIdentifiers.submissionAmendmentMetric
    val timerBefore = registry.getTimers.get(exportMetrics.timerName(metric)).getCount
    val counterBefore = registry.getCounters.get(exportMetrics.counterName(metric)).getCount

    "successfully submit to the back end a valid amendment" in {
      // Given
      val parentDeclaration = aDeclaration(
        withId("id1"),
        withStatus(DeclarationStatus.COMPLETE),
        withConsignmentReferences(ducr = "ducr", lrn = lrn),
        withDestinationCountry(),
        withTotalNumberOfItems(Some("123456"), Some("1.49"), Some("GBP"), Some("yes"))
      )
      when(connector.findDeclaration(any())(any(), any())).thenReturn(Future.successful(Some(parentDeclaration)))

      val amendedDecl = aDeclaration(
        withId("id2"),
        withStatus(DeclarationStatus.AMENDMENT_DRAFT),
        withParentDeclarationId("id1"),
        withConsignmentReferences(ducr = "ducr", lrn = lrn),
        withDestinationCountry(Country(Some("IT"))),
        withTotalNumberOfItems(Some("654321"), Some("94.1"), Some("GBP"), Some("no"))
      )

      val expectedActionId = "actionId"
      when(connector.submitAmendment(any())(any(), any())).thenReturn(Future.successful(expectedActionId))

      // When
      submissionService.submitAmendment(eori, amendedDecl, legal, submissionId, false)(hc, global).futureValue mustBe Some(expectedActionId)

      // Then
      val expectedFieldPointers = List(
        "declaration.locations.destinationCountry.code",
        "declaration.totalNumberOfItems.totalAmountInvoiced",
        "declaration.totalNumberOfItems.exchangeRate"
      )
      val expectedSubmissionAmendment = SubmissionAmendment(submissionId, "id2", expectedFieldPointers)
      verify(connector).submitAmendment(equalTo(expectedSubmissionAmendment))(any(), any())
      verify(auditService).auditAllPagesUserInput(equalTo(AuditTypes.AmendmentPayload), equalTo(amendedDecl))(any())
      verify(auditService).audit(equalTo(AuditTypes.Amendment), equalTo[Map[String, String]](auditData))(any)

      registry.getTimers.get(exportMetrics.timerName(metric)).getCount mustBe >(timerBefore)
      registry.getCounters.get(exportMetrics.counterName(metric)).getCount mustBe >(counterBefore)
    }
  }
}
