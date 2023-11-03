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
import models.CancellationStatus.CancellationStatusWrites
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.StatusGroup.ActionRequiredStatuses
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import models.{CancelDeclaration, CancellationRequestSent, PageOfSubmissions, Paginated}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers._
import services.cache.ExportsDeclarationBuilder
import views.dashboard.DashboardHelper.{Groups, Page}

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID

class CustomsDeclareExportsConnectorISpec extends ConnectorISpec with ExportsDeclarationBuilder with ScalaFutures with FeatureFlagMocks {

  private val id = "id"
  private val existingDeclaration = aDeclaration(withId(id), withParentDeclarationId("123456789"))

  private val action = Action(id = UUID.randomUUID().toString, requestType = SubmissionRequest, notifications = None, decId = Some(id), versionNo = 1)
  private val submission = Submission(id, "eori", "lrn", Some("mrn"), None, None, None, Seq(action), latestDecId = Some(id))
  private val notification = Notification("action-id", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.UNKNOWN, Seq.empty)
  private val injector = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder()
      .overrides(bind[DeclarationAmendmentsConfig].toInstance(mockDeclarationAmendmentsConfig))
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
    Mockito.reset(mockDeclarationAmendmentsConfig)
  }

  "Create Declaration" should {
    "return payload" in {
      stubForExports(
        post("/declarations")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclaration))
          )
      )

      val newDeclaration = aDeclaration().copy(id = "")
      val response = await(connector.createDeclaration(newDeclaration))

      response mustBe existingDeclaration
      WireMock.verify(
        postRequestedFor(urlEqualTo("/declarations"))
          .withRequestBody(containing(json(newDeclaration)))
      )
    }
  }

  "Update Declaration" should {
    "return payload" in {
      stubForExports(
        put(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclaration))
          )
      )

      val response = await(connector.updateDeclaration(existingDeclaration))

      response mustBe existingDeclaration
      WireMock.verify(
        putRequestedFor(urlEqualTo(s"/declarations/id"))
          .withRequestBody(containing(json(existingDeclaration)))
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

      val response = await(connector.deleteDraftDeclaration(id))

      response mustBe ((): Unit)
      WireMock.verify(deleteRequestedFor(urlEqualTo(s"/declarations/id")))
    }
  }

  "Fetch Submission Page" should {
    val queryString = s"${Groups}=action&${Page}=1"
    val pageOfSubmissions = PageOfSubmissions(ActionRequiredStatuses, 1, List(submission))

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

  "Find Declarations" should {
    val pagination = models.Page(1, 10)

    "return Ok" in {
      stubForExports(
        get("/declarations?page-index=1&page-size=10")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclaration), pagination, 1)))
          )
      )

      val response = await(connector.findDeclarations(pagination))

      response mustBe Paginated(Seq(existingDeclaration), pagination, 1)
      WireMock.verify(getRequestedFor(urlEqualTo("/declarations?page-index=1&page-size=10")))
    }
  }

  "FindDraftByParent method" should {
    val parentId = existingDeclaration.declarationMeta.parentDeclarationId.getOrElse("")

    "return Ok" in {
      stubForExports(
        get(s"/draft-declarations-by-parent/${parentId}")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(existingDeclaration))
          )
      )

      val response = await(connector.findDraftByParent(parentId))

      response mustBe Some(existingDeclaration)
      WireMock.verify(getRequestedFor(urlEqualTo(s"/draft-declarations-by-parent/${parentId}")))
    }
  }

  "Find Saved Draft Declarations when Amendment Flag is enabled" should {
    val pagination = models.Page(1, 10)

    "return Ok" in {
      when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(true)
      stubForExports(
        get("/declarations?status=DRAFT&status=AMENDMENT_DRAFT&page-index=1&page-size=10&sort-by=declarationMeta.updatedDateTime&sort-direction=des")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclaration), pagination, 1)))
          )
      )

      val response = await(connector.findSavedDeclarations(pagination))

      response mustBe Paginated(Seq(existingDeclaration), pagination, 1)
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/declarations?status=DRAFT&status=AMENDMENT_DRAFT&page-index=1&page-size=10&sort-by=declarationMeta.updatedDateTime&sort-direction=des"
          )
        )
      )
    }
  }

  "Find Saved Draft Declarations when Amendment Flag is disabled" should {
    val pagination = models.Page(1, 10)

    "return Ok" in {
      when(mockDeclarationAmendmentsConfig.isEnabled).thenReturn(false)
      stubForExports(
        get("/declarations?status=DRAFT&page-index=1&page-size=10&sort-by=declarationMeta.updatedDateTime&sort-direction=des")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclaration), pagination, 1)))
          )
      )

      val response = await(connector.findSavedDeclarations(pagination))

      response mustBe Paginated(Seq(existingDeclaration), pagination, 1)
      WireMock.verify(
        getRequestedFor(urlEqualTo("/declarations?status=DRAFT&page-index=1&page-size=10&sort-by=declarationMeta.updatedDateTime&sort-direction=des"))
      )
    }
  }

  "Find Declaration" should {
    "return Ok" in {
      stubForExports(
        get(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(existingDeclaration))
          )
      )

      val response = await(connector.findDeclaration(id))

      response mustBe Some(existingDeclaration)
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
              .withBody(json(Seq(notification)))
          )
      )

      val response = await(connector.findNotifications(id))

      response mustBe Seq(notification)
      WireMock.verify(getRequestedFor(urlEqualTo(s"/submission/notifications/$id")))
    }
  }

  "Create Cancellation" should {
    val cancellation = CancelDeclaration(id, Lrn("ref"), "id", "statement", "reason")

    "return payload" in {
      stubForExports(
        post("/cancellations")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(Json.toJson(CancellationRequestSent)(CancellationStatusWrites.writes _).toString())
          )
      )

      await(connector.createCancellation(cancellation))

      WireMock.verify(
        postRequestedFor(urlEqualTo("/cancellations"))
          .withRequestBody(containing(json(cancellation)))
      )
    }
  }

  private def json[T](t: T)(implicit wts: Writes[T]): String = Json.toJson(t).toString()

}
