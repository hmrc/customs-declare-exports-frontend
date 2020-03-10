/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors.ead

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ConnectorSpec
import models.dis.MrnStatus
import models.dis.parsers.MrnStatusParserTestData
import play.api.http.Status
import play.api.test.Helpers._

class CustomsDeclarationsInformationConnectorSpec extends ConnectorSpec {

  private val connector = app.injector.instanceOf[CustomsDeclarationsInformationConnector]
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    disWireMockServer.start()
    WireMock.configureFor(wireHost, disWirePort)
  }

  override protected def afterAll(): Unit = {
    disWireMockServer.stop()
    super.afterAll()
  }

  "Customs Declarations Information Connector" should {

    "GET to Customs Declarations Information service" when {
      "MRN is valid" in {
        val mrn = "18GB9JLC3CU1LFGVR2"
        stubForDis(
          get(s"/mrn/${mrn}/status")
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(MrnStatusParserTestData.mrnStatusWithSelectedFields(mrn).toString())
            )
        )

        val mrnStatus: MrnStatus = await(connector.fetchMrnStatus(mrn))
        mrnStatus.mrn mustBe mrn
        mrnStatus.eori mustBe "GB7172755049242"
      }
    }
  }
}
