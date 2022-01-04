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

import scala.concurrent.duration._

import base.UnitWithMocksSpec
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import play.api.http.Status._

trait WiremockTestServer extends UnitWithMocksSpec with BeforeAndAfterAll with Eventually {

  val wireHost = "localhost"

  val auditingPort = 20001
  val auditingWireMockServer = new WireMockServer(auditingPort)

  val disWirePort = 20002
  val disWireMockServer = new WireMockServer(disWirePort)

  val exportsWirePort = 20003
  val exportsWireMockServer = new WireMockServer(exportsWirePort)

  val secureMessagingWirePort = 20004
  val secureMessagingWireMockServer = new WireMockServer(secureMessagingWirePort)

  val tariffCommoditiesWirePort = 20005
  val tariffCommoditiesWireMockServer = new WireMockServer(tariffCommoditiesWirePort)

  protected def stubForAuditing(): StubMapping = {
    val url = "/write/audit"
    auditingWireMockServer.stubFor(post(url).willReturn(aResponse.withStatus(NO_CONTENT)))
  }

  protected def stubForExports(mappingBuilder: MappingBuilder): StubMapping =
    exportsWireMockServer.stubFor(mappingBuilder)

  protected def stubForSecureMessaging(mappingBuilder: MappingBuilder): StubMapping =
    secureMessagingWireMockServer.stubFor(mappingBuilder)

  protected def stubForTariffCommodities(mappingBuilder: MappingBuilder): StubMapping =
    tariffCommoditiesWireMockServer.stubFor(mappingBuilder)

  protected def verifyForAuditing(count: Int = 1): Unit =
    eventually(timeout(1 seconds), interval(50 millis)) {
      auditingWireMockServer.verify(count, postRequestedFor(urlEqualTo("/write/audit")))
    }
}
