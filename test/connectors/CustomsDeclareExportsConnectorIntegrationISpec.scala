/*
 * Copyright 2022 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import forms.Lrn
import models.CancellationStatus.CancellationStatusWrites
import models.declaration.notifications.Notification
import models.declaration.submissions.RequestType.SubmissionRequest
import models.declaration.submissions.{Action, Submission, SubmissionStatus}
import models.{CancelDeclaration, CancellationRequestSent, Page, Paginated}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers._
import services.cache.ExportsDeclarationBuilder

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID

class CustomsDeclareExportsConnectorIntegrationISpec extends ConnectorISpec with ExportsDeclarationBuilder with ScalaFutures {

  private val id = "id"
  private val existingDeclaration = aDeclaration(withId(id))

  private val action = Action(id = UUID.randomUUID().toString, requestType = SubmissionRequest, notifications = None)
  private val submission = Submission(id, "eori", "lrn", Some("mrn"), None, None, None, Seq(action))
  private val notification = Notification("action-id", "mrn", ZonedDateTime.now(ZoneOffset.UTC), SubmissionStatus.UNKNOWN, Seq.empty)
  private val connector = app.injector.instanceOf[CustomsDeclareExportsConnector]

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
      verify(
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
      verify(
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
           |      "requestTimestamp" : "${ZonedDateTime.now(ZoneOffset.UTC).toString}"
           |   }]
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

      verify(
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
      verify(deleteRequestedFor(urlEqualTo(s"/declarations/id")))
    }
  }

  "Find Declarations" should {
    val pagination = Page(1, 10)

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
      verify(getRequestedFor(urlEqualTo("/declarations?page-index=1&page-size=10")))
    }
  }

  "Find Saved Draft Declarations" should {
    val pagination = Page(1, 10)

    "return Ok" in {
      stubForExports(
        get("/declarations?status=DRAFT&page-index=1&page-size=10&sort-by=updatedDateTime&sort-direction=des")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclaration), pagination, 1)))
          )
      )

      val response = await(connector.findSavedDeclarations(pagination))

      response mustBe Paginated(Seq(existingDeclaration), pagination, 1)
      verify(getRequestedFor(urlEqualTo("/declarations?status=DRAFT&page-index=1&page-size=10&sort-by=updatedDateTime&sort-direction=des")))
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
      verify(getRequestedFor(urlEqualTo(s"/declarations/$id")))
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
      verify(getRequestedFor(urlEqualTo(s"/submission/${submission.uuid}")))
    }
  }

  "Find if Lrn already used" should {
    "return Ok" in {
      val lrn = Lrn(submission.lrn)
      stubForExports(
        get(s"/lrn-already-used/${lrn.value}")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(true))
          )
      )

      val response = await(connector.isLrnAlreadyUsed(lrn))

      response mustBe true
      verify(getRequestedFor(urlEqualTo(s"/lrn-already-used/${lrn.value}")))
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
      verify(getRequestedFor(urlEqualTo(s"/submission/notifications/$id")))
    }
  }

  "Find last received Notification" should {

    "return Ok when a notification is found" in {
      val result = aResponse().withStatus(Status.OK).withBody(json(Option(notification)))
      stubForExports(get(s"/submission/latest-notification/$id").willReturn(result))

      val response = await(connector.findLatestNotification(id))

      response.head mustBe notification
      verify(getRequestedFor(urlEqualTo(s"/submission/latest-notification/$id")))
    }

    "return None when a notification is not found" in {
      val result = aResponse().withStatus(Status.NOT_FOUND)
      stubForExports(get(s"/submission/latest-notification/$id").willReturn(result))

      val response = await(connector.findLatestNotification(id))

      response mustBe None
      verify(getRequestedFor(urlEqualTo(s"/submission/latest-notification/$id")))
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
              .withBody(Json.toJson(CancellationRequestSent)(CancellationStatusWrites.writes).toString())
          )
      )

      await(connector.createCancellation(cancellation))

      verify(
        postRequestedFor(urlEqualTo("/cancellations"))
          .withRequestBody(containing(json(cancellation)))
      )
    }
  }

  private def json[T](t: T)(implicit wts: Writes[T]): String = Json.toJson(t).toString()

}
