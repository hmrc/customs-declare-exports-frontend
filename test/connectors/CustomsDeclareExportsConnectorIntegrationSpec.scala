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

import java.time.LocalDateTime
import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.exchange.ExportsDeclarationExchange
import models.declaration.notifications.Notification
import models.declaration.submissions.{Action, RequestType, Submission}
import models.{DeclarationStatus, Page, Paginated}
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import services.cache.ExportsDeclarationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class CustomsDeclareExportsConnectorIntegrationSpec
    extends ConnectorSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with ScalaFutures {

  private val id = "id"
  private val newDeclaration = aDeclaration(withoutId())
  private val existingDeclaration = aDeclaration(withId(id))
  private val newDeclarationExchange = ExportsDeclarationExchange(newDeclaration)
  private val existingDeclarationExchange = ExportsDeclarationExchange(existingDeclaration)
  private val action = Action(RequestType.SubmissionRequest, UUID.randomUUID().toString)
  private val submission = Submission(id, "eori", "lrn", Some("mrn"), None, Seq(action))
  private val notification = Notification("conv-id", "mrn", LocalDateTime.now, "f-code", None, Seq.empty, "payload")
  private val connector = new CustomsDeclareExportsConnector(config, httpClient)

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.declarationsV2).willReturn("/v2/declaration")
  }

  "Create Declaration" should {
    "return payload" in {
      stubFor(
        post("/v2/declaration")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.createDeclaration(newDeclaration))

      response shouldBe existingDeclaration
      verify(
        postRequestedFor(urlEqualTo("/v2/declaration"))
          .withRequestBody(containing(json(newDeclarationExchange)))
      )
    }
  }

  "Update Declaration" should {
    "return payload" in {
      stubFor(
        put(s"/v2/declaration/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.updateDeclaration(existingDeclaration))

      response shouldBe existingDeclaration
      verify(
        putRequestedFor(urlEqualTo(s"/v2/declaration/id"))
          .withRequestBody(containing(json(existingDeclarationExchange)))
      )
    }

    "throw IllegalArgument for missing ID" in {
      intercept[IllegalArgumentException] {
        await(connector.updateDeclaration(newDeclaration))
      }
    }
  }

  "Find Declarations" should {
    val pagination = Page(1, 10)

    "return Ok" in {
      stubFor(
        get("/v2/declaration?status=DRAFT&page-index=1&page-size=10")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Paginated(Seq(existingDeclarationExchange), pagination, 1)))
          )
      )

      val response = await(connector.findDeclarations(pagination))

      response shouldBe Paginated(Seq(existingDeclaration), pagination, 1)
      verify(getRequestedFor(urlEqualTo("/v2/declaration?status=DRAFT&page-index=1&page-size=10")))
    }
  }

  "Find Declaration" should {
    "return Ok" in {
      stubFor(
        get(s"/v2/declaration/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.findDeclaration(id))

      response shouldBe Some(existingDeclaration)
      verify(getRequestedFor(urlEqualTo(s"/v2/declaration/$id")))
    }
  }

  "Find Submission" should {
    "return Ok" in {
      stubFor(
        get(s"/v2/declaration/$id/submission")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(submission))
          )
      )

      val response = await(connector.findSubmission(id))

      response shouldBe Some(submission)
      verify(getRequestedFor(urlEqualTo(s"/v2/declaration/$id/submission")))
    }
  }

  "Find Notifications" should {
    "return Ok" in {
      stubFor(
        get(s"/v2/declaration/$id/submission/notifications")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Seq(notification)))
          )
      )

      val response = await(connector.findNotifications(id))

      response shouldBe Seq(notification)
      verify(getRequestedFor(urlEqualTo(s"/v2/declaration/$id/submission/notifications")))
    }
  }

  private def json[T](t: T)(implicit wts: Writes[T]): String = Json.toJson(t).toString()

}
