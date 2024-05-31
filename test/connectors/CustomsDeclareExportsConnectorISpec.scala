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

package connectors

import base.TestHelper._
import com.codahale.metrics.SharedMetricRegistries
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import config.featureFlags.DeclarationAmendmentsConfig
import forms.Lrn
import mock.FeatureFlagMocks
import models.CancellationStatus.CancellationResult
import models.declaration.DeclarationStatus.DRAFT
import models.declaration.notifications.Notification
import models.declaration.submissions.EnhancedStatus.GOODS_ARRIVED
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.StatusGroup.ActionRequiredStatuses
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{verify, verifyNoInteractions}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers._
import services.audit.AuditService
import services.cache.ExportsDeclarationBuilder
import views.dashboard.DashboardHelper.{Groups, Page}

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

class CustomsDeclareExportsConnectorISpec extends ConnectorISpec with ExportsDeclarationBuilder with ScalaFutures with FeatureFlagMocks {

  private val id = "id"
  private val eori = "eori"
  private val declaration = aDeclaration(withId(id), withParentDeclarationId("123456789"))

  private val action = Action(id = UUID.randomUUID().toString, requestType = SubmissionRequest, notifications = None, decId = Some(id), versionNo = 1)
  private val submission = Submission(id, eori, "lrn", Some("mrn"), None, None, None, List(action), latestDecId = Some(id))
  private val notification = Notification("action-id", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.UNKNOWN, Seq.empty)

  private val mockAuditService = mock[AuditService]

  private val injector = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .overrides(bind[DeclarationAmendmentsConfig].toInstance(mockDeclarationAmendmentsConfig))
      .overrides(bind[AuditService].toInstance(mockAuditService))
      .configure(overrideConfig)
      .injector()
  }
  private val connector = injector.instanceOf[CustomsDeclareExportsConnector]

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    exportsWireMockServer.start()
    WireMock.configureFor(wireHost, exportsWirePort)
  }

  override protected def afterAll(): Unit = {
    exportsWireMockServer.stop()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockDeclarationAmendmentsConfig, mockAuditService)
  }

  "Create Declaration" should {
    "return payload" in {
      stubForExports(
        post("/declarations")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(declaration))
          )
      )

      val newDeclaration = aDeclaration(withConsignmentReferences("DUCR", "LRN")).copy(id = "")
      val response = await(connector.createDeclaration(newDeclaration, eori))

      response mustBe declaration
      WireMock.verify(
        postRequestedFor(urlEqualTo("/declarations"))
          .withRequestBody(containing(json(newDeclaration)))
      )
    }

    "send audit event if DUCR set" in {
      stubForExports(
        post("/declarations")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(declaration))
          )
      )

      val newDeclaration = aDeclaration(withConsignmentReferences("DUCR", "LRN")).copy(id = "")
      val response = await(connector.createDeclaration(newDeclaration, eori))

      response mustBe declaration

      verify(mockAuditService).auditDraftDecCreated(any(), any())(any())
    }

    "don NOT send audit event if DUCR is not set" in {
      stubForExports(
        post("/declarations")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(declaration))
          )
      )

      val newDeclaration = aDeclaration().copy(id = "")
      val response = await(connector.createDeclaration(newDeclaration, eori))

      response mustBe declaration

      verifyNoInteractions(mockAuditService)
    }
  }

  "Update Declaration" should {
    "return payload" in {
      stubForExports(
        put(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(declaration))
          )
      )

      val response = await(connector.updateDeclaration(declaration, eori))

      response mustBe declaration
      WireMock.verify(
        putRequestedFor(urlEqualTo(s"/declarations/id"))
          .withRequestBody(containing(json(declaration)))
      )
    }
  }

  "Submit declaration" should {
    "return submission object" in {
      val payload =
        s"""
           |{
           |  "uuid": "$id",
           |  "eori": "${createRandomAlphanumericString(11)}",
           |  "lrn":  "${createRandomAlphanumericString(8)}",
           |  "actions" : [{
           |      "id" : "${UUID.randomUUID().toString}",
           |      "requestType" : "SubmissionRequest",
           |      "requestTimestamp" : "${ZonedDateTime.now(ZoneOffset.UTC).toString}",
           |      "decId" : "$id",
           |      "versionNo" : 1
           |   }],
           |  "latestDecId" : "$id",
           |  "latestVersionNo" : 1,
           |  "blockAmendments" : false
          |}
        """.stripMargin
      stubForExports(
        post(s"/submission/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.CREATED)
              .withBody(payload)
          )
      )
      val response = await(connector.submitDeclaration(id))

      response.uuid mustBe id
      response.actions must not be empty

      WireMock.verify(
        postRequestedFor(urlEqualTo(s"/submission/$id"))
          .withRequestBody(absent())
      )
    }
  }

  "Delete Declaration" should {
    "return payload" in {
      stubForExports(
        delete(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.NO_CONTENT)
          )
      )

      await(connector.deleteDraftDeclaration(id)) mustBe ((): Unit)
      WireMock.verify(deleteRequestedFor(urlEqualTo(s"/declarations/id")))
    }
  }

  "Fetch Submission Page" should {
    val queryString = s"${Groups}=action&${Page}=1&reverse"
    val pageOfSubmissions = PageOfSubmissions(ActionRequiredStatuses, 1, List(submission), true)

    "return Ok" in {
      stubForExports(
        get(s"/paginated-submissions?$queryString")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(pageOfSubmissions))
          )
      )

      val response = await(connector.fetchSubmissionPage(queryString))

      response mustBe pageOfSubmissions
      WireMock.verify(getRequestedFor(urlEqualTo(s"/paginated-submissions?$queryString")))
    }
  }

  "FindDraftByParent method" should {
    val parentId = declaration.declarationMeta.parentDeclarationId.getOrElse("")

    "return Ok" in {
      stubForExports(
        get(s"/draft-declarations-by-parent/${parentId}")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(declaration))
          )
      )

      val response = await(connector.findDraftByParent(parentId))

      response mustBe Some(declaration)
      WireMock.verify(getRequestedFor(urlEqualTo(s"/draft-declarations-by-parent/${parentId}")))
    }
  }

  "Fetch Draft Declarations" should {
    "return a page of draft declarations" in {
      val page = models.Page(1, 10)
      val draftDeclarationData = DraftDeclarationData("id", Some("ducrId"), DRAFT, Instant.now)
      val pageOfDraftDeclarationData = Paginated(List(draftDeclarationData), page, 1)

      val query = "page-index=1&page-size=10&sort-by=declarationMeta.updatedDateTime&sort-direction=desc"

      val response = aResponse().withStatus(Status.OK).withBody(json(pageOfDraftDeclarationData))
      stubForExports(get(s"/draft-declarations?$query").willReturn(response))

      await(connector.fetchDraftDeclarations(page)) mustBe pageOfDraftDeclarationData
      WireMock.verify(getRequestedFor(urlEqualTo(s"/draft-declarations?$query")))
    }
  }

  "Find or Create Draft for Amendment" should {
    val draftId = "sd8f7sdf76s87f6s8d7f6s7"
    val parentId = "345232fer23423"
    val enhancedStatus = GOODS_ARRIVED

    "not audit draft dec creation if an existing draft has been returned" in {
      stubForExports(
        get(s"/amendment-draft/$parentId/${enhancedStatus.toString}")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(s""""$draftId"""")
          )
      )

      val response = await(connector.findOrCreateDraftForAmendment(parentId, enhancedStatus, "eori", declaration))

      response mustBe draftId
      WireMock.verify(getRequestedFor(urlEqualTo(s"/amendment-draft/$parentId/${enhancedStatus.toString}")))
      verifyNoInteractions(mockAuditService)
    }

    "audit draft dec creation if a new draft has been returned" in {
      stubForExports(
        get(s"/amendment-draft/$parentId/${enhancedStatus.toString}")
          .willReturn(
            aResponse()
              .withStatus(Status.CREATED)
              .withBody(s""""$draftId"""")
          )
      )

      val response =
        await(connector.findOrCreateDraftForAmendment(parentId, enhancedStatus, "eori", aDeclaration(withConsignmentReferences("DUCR", "LRN"))))

      response mustBe draftId

      verify(mockAuditService).auditDraftDecCreated(any(), any())(any())
    }
  }

  "Find or Create Draft for Rejection" should {
    val draftId = "sd8f7sdf76s87f6s8d7f6s7"
    val parentId = "345232fer23423"

    "not audit draft dec creation if an existing draft has been returned" in {
      stubForExports(
        get(s"/rejected-submission-draft/$parentId")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(s""""$draftId"""")
          )
      )

      val response = await(connector.findOrCreateDraftForRejection(parentId, "eori", declaration))

      response mustBe draftId
      WireMock.verify(getRequestedFor(urlEqualTo(s"/rejected-submission-draft/$parentId")))
      verifyNoInteractions(mockAuditService)
    }

    "audit draft dec creation if a new draft has been returned" in {
      stubForExports(
        get(s"/rejected-submission-draft/$parentId")
          .willReturn(
            aResponse()
              .withStatus(Status.CREATED)
              .withBody(s""""$draftId"""")
          )
      )

      val response = await(connector.findOrCreateDraftForRejection(parentId, "eori", aDeclaration(withConsignmentReferences("DUCR", "LRN"))))

      response mustBe draftId

      verify(mockAuditService).auditDraftDecCreated(any(), any())(any())
    }
  }

  "Find Declaration" should {
    "return Ok" in {
      stubForExports(
        get(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(declaration))
          )
      )

      val response = await(connector.findDeclaration(id))

      response mustBe Some(declaration)
      WireMock.verify(getRequestedFor(urlEqualTo(s"/declarations/$id")))
    }
  }

  "Find Submission" should {
    "return Ok" in {
      stubForExports(
        get(s"/submission/${submission.uuid}")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Some(submission)))
          )
      )

      val response = await(connector.findSubmission(id))

      response mustBe Some(submission)
      WireMock.verify(getRequestedFor(urlEqualTo(s"/submission/${submission.uuid}")))
    }
  }

  "Find if Lrn already used" should {
    "return Ok" in {
      val lrn = Lrn(submission.lrn)
      stubForExports(
        get(s"/lrn-already-used/${lrn.lrn}")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(true))
          )
      )

      val response = await(connector.isLrnAlreadyUsed(lrn))

      response mustBe true
      WireMock.verify(getRequestedFor(urlEqualTo(s"/lrn-already-used/${lrn.lrn}")))
    }
  }

  "Find Notifications" should {
    "return Ok" in {
      stubForExports(
        get(s"/submission/notifications/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(List(notification)))
          )
      )

      val response = await(connector.findNotifications(id))

      response mustBe List(notification)
      WireMock.verify(getRequestedFor(urlEqualTo(s"/submission/notifications/$id")))
    }
  }

  "Create Cancellation" should {
    val cancellation = CancelDeclaration(id, Lrn("ref"), "id", "statement", "reason")

    "return payload" in {
      stubForExports(
        post("/cancellation-request")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(
                Json
                  .toJson(CancellationResult(CancellationRequestSent, Some("conversationId")))(CancellationResult.cancellationResultFormat.writes _)
                  .toString()
              )
          )
      )

      await(connector.createCancellation(cancellation))

      WireMock.verify(
        postRequestedFor(urlEqualTo("/cancellation-request"))
          .withRequestBody(containing(json(cancellation)))
      )
    }
  }

  private def json[T](t: T)(implicit wts: Writes[T]): String = Json.toJson(t).toString()

}
