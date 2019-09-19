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

package connectors

import java.time.{Instant, LocalDateTime}
import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.exchange.ExportsDeclarationExchange
import forms.CancelDeclaration
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, RequestType, Submission, SubmissionStatus}
import models.{Page, Paginated}
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers._
import services.cache.ExportsDeclarationBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import base.TestHelper._

class CustomsDeclareExportsConnectorIntegrationSpec
    extends ConnectorSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with ScalaFutures {

  private val id = "id"
  private val newDeclaration = aDeclaration(withoutId())
  private val existingDeclaration = aDeclaration(withId(id))
  private val newDeclarationExchange = ExportsDeclarationExchange(newDeclaration)
  private val existingDeclarationExchange = ExportsDeclarationExchange(existingDeclaration)
  private val action = Action(RequestType.SubmissionRequest, UUID.randomUUID().toString)
  private val submission = Submission(id, "eori", "lrn", Some("mrn"), None, Seq(action))
  private val notification =
    Notification("action-id", "mrn", LocalDateTime.now, SubmissionStatus.UNKNOWN, Seq.empty, "payload")
  private val connector = new CustomsDeclareExportsConnector(config, httpClient)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.declarationsV2).willReturn("/declarations")
    given(config.cancelDeclaration).willReturn("/cancellations")
  }

  "Create Declaration" should {
    "return payload" in {
      stubFor(
        post("/declarations")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.createDeclaration(newDeclaration))

      response mustBe existingDeclaration
      verify(
        postRequestedFor(urlEqualTo("/declarations"))
          .withRequestBody(containing(json(newDeclarationExchange)))
      )
    }
  }

  "Update Declaration" should {
    "return payload" in {
      stubFor(
        put(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.updateDeclaration(existingDeclaration))

      response mustBe existingDeclaration
      verify(
        putRequestedFor(urlEqualTo(s"/declarations/id"))
          .withRequestBody(containing(json(existingDeclarationExchange)))
      )
    }

    "throw IllegalArgument for missing ID" in {
      intercept[IllegalArgumentException] {
        await(connector.updateDeclaration(newDeclaration))
      }
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
           |      "requestType" : "SubmissionRequest",
           |      "conversationId" : "${UUID.randomUUID().toString}",
           |      "requestTimestamp" : "${Instant.now().toString}"
           |   }]
          |}
        """.stripMargin
      stubFor(
        post(s"/declarations/$id/submission")
          .willReturn(
            aResponse()
              .withStatus(Status.CREATED)
              .withBody(payload)
          )
      )
      val response = await(connector.submitDeclaration(id))

      response.uuid mustBe id
      response.actions must not be empty
    }
  }

  "Delete Declaration" should {
    "return payload" in {
      stubFor(
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
      stubFor(
        get("/declarations?page-index=1&page-size=10")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclarationExchange), pagination, 1)))
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
      stubFor(
        get("/declarations?status=DRAFT&page-index=1&page-size=10&sort-by=updatedDateTime&sort-direction=des")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclarationExchange), pagination, 1)))
          )
      )

      val response = await(connector.findSavedDeclarations(pagination))

      response mustBe Paginated(Seq(existingDeclaration), pagination, 1)
      verify(
        getRequestedFor(
          urlEqualTo("/declarations?status=DRAFT&page-index=1&page-size=10&sort-by=updatedDateTime&sort-direction=des")
        )
      )
    }
  }

  "Find Declaration" should {
    "return Ok" in {
      stubFor(
        get(s"/declarations/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.findDeclaration(id))

      response mustBe Some(existingDeclaration)
      verify(getRequestedFor(urlEqualTo(s"/declarations/$id")))
    }
  }

  "Find Submission" should {
    "return Ok" in {
      stubFor(
        get(s"/declarations/$id/submission")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(submission))
          )
      )

      val response = await(connector.findSubmission(id))

      response mustBe Some(submission)
      verify(getRequestedFor(urlEqualTo(s"/declarations/$id/submission")))
    }
  }

  "Find Notifications" should {
    "return Ok" in {
      stubFor(
        get(s"/declarations/$id/submission/notifications")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Seq(notification)))
          )
      )

      val response = await(connector.findNotifications(id))

      response mustBe Seq(notification)
      verify(getRequestedFor(urlEqualTo(s"/declarations/$id/submission/notifications")))
    }
  }

  "Create Cancellation" should {
    val cancellation = CancelDeclaration("ref", "id", "statement", "reason")

    "return payload" in {
      stubFor(
        post("/cancellations")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
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
