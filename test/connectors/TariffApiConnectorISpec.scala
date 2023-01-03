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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import play.mvc.Http.Status._

class TariffApiConnectorISpec extends ConnectorISpec with OptionValues with ScalaFutures {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
    tariffCommoditiesWireMockServer.start()
    WireMock.configureFor(wireHost, tariffCommoditiesWirePort)
  }

  override def afterAll(): Unit = {
    tariffCommoditiesWireMockServer.stop()
    super.afterAll()
  }

  private val testConnector: TariffApiConnector = app.injector.instanceOf[TariffApiConnector]

  private val commodityCode: String = "1234567890"

  private val exampleSuccessResponse: String = """{"testJson": true}"""

  "TariffAPIConnector" when {

    "getCommodityOnCondition" should {

      "respond with json" in {
        val response = aResponse.withStatus(OK).withBody(exampleSuccessResponse)
        stubForTariffCommodities(get(anyUrl()).willReturn(response))

        val result = testConnector.getCommodityOnCondition(commodityCode).futureValue
        result.value mustBe Json.parse(exampleSuccessResponse)
      }

      "server responds with something else" in {
        val response = aResponse.withStatus(NOT_FOUND)
        stubForTariffCommodities(get(anyUrl()).willReturn(response))

        whenReady(testConnector.getCommodityOnCondition(commodityCode)) { result =>
          result mustBe None
        }
      }
    }
  }
}
