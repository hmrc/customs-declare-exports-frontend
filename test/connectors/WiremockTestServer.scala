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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.BeforeAndAfterAll
import uk.gov.hmrc.play.test.UnitSpec

trait WiremockTestServer extends UnitSpec with BeforeAndAfterAll {

  private val wireHost = "localhost"
  private val wirePort = 20001
  private val wireMockServer = new WireMockServer(wirePort)

  lazy val host: String = s"http://$wireHost:$wirePort"

  protected def stubFor(mappingBuilder: MappingBuilder): StubMapping = {
    wireMockServer.stubFor(mappingBuilder)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(wireHost, wirePort)
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

}
