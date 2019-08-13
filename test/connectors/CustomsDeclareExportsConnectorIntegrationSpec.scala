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

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.exchange.ExportsDeclarationExchange
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import services.WcoMetadataMapper
import services.cache.ExportsDeclarationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class CustomsDeclareExportsConnectorIntegrationSpec extends ConnectorSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with ScalaFutures {

  private val id = "id"
  private val sessionId = "session-id"
  private val newDeclaration = aDeclaration(withoutId(), withSessionId(sessionId))
  private val existingDeclaration = aDeclaration(withId(id), withSessionId(sessionId))
  private val newDeclarationExchange = ExportsDeclarationExchange(newDeclaration)
  private val existingDeclarationExchange = ExportsDeclarationExchange(existingDeclaration)
  private val mapper = mock[WcoMetadataMapper]
  private val connector = new CustomsDeclareExportsConnector(config, httpClient, mapper)

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.submitDeclarationV2).willReturn("/v2/declaration")
  }

  "Create" should {
    "return payload" in {
      stubFor(
        post("/v2/declaration")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.create(newDeclaration))

      response shouldBe existingDeclaration
      verify(
        postRequestedFor(urlEqualTo("/v2/declaration"))
          .withRequestBody(containing(json(newDeclarationExchange)))
      )
    }
  }

  "Update" should {
    "return payload" in {
      stubFor(
        put(s"/v2/declaration/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.update(existingDeclaration))

      response shouldBe existingDeclaration
      verify(
        putRequestedFor(urlEqualTo(s"/v2/declaration/id"))
          .withRequestBody(containing(json(existingDeclarationExchange)))
      )
    }

    "throw IllegalArgument for missing ID" in {
      intercept[IllegalArgumentException] {
        await(connector.update(newDeclaration))
      }
    }
  }

  "Find" should {
    "return Ok" in {
      stubFor(
        get("/v2/declaration")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(Seq(existingDeclarationExchange)))
          )
      )

      val response = await(connector.find(sessionId))

      response.toList shouldBe List(existingDeclaration)
      verify(getRequestedFor(urlEqualTo("/v2/declaration")))
    }
  }

  "Find by ID" should {
    "return Ok" in {
      stubFor(
        get(s"/v2/declaration/$id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(json(existingDeclarationExchange))
          )
      )

      val response = await(connector.find(sessionId, id))

      response shouldBe Some(existingDeclaration)
      verify(getRequestedFor(urlEqualTo(s"/v2/declaration/$id")))
    }
  }

  private def json[T](t: T)(implicit wts: Writes[T]): String = Json.toJson(t).toString()

}
