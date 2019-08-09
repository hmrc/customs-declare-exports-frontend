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
import connectors.request.ExportsDeclarationRequest
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import services.WcoMetadataMapper
import services.cache.ExportsDeclarationBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class CustomsDeclareExportsConnectorIntegrationSpec extends ConnectorSpec with BeforeAndAfterEach with ExportsDeclarationBuilder with ScalaFutures {

  private val mapper = mock[WcoMetadataMapper]
  private val connector = new CustomsDeclareExportsConnector(config, httpClient, mapper)

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.submitDeclarationV2).willReturn("/v2/declaration")
  }

  "Submit" should {
    "return Accepted" in {
      stubFor(
        post("/v2/declaration")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )
      val declaration = aDeclaration()

      val response = await(connector.submit(declaration))

      response.status shouldBe Status.ACCEPTED
      verify(
        postRequestedFor(urlEqualTo("/v2/declaration"))
          .withRequestBody(containing(Json.toJson(ExportsDeclarationRequest(declaration)).toString()))
      )
    }
  }

}
