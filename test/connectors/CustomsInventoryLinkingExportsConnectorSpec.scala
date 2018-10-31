/*
 * Copyright 2018 HM Revenue & Customs
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

import base.{CustomExportsBaseSpec, MockInventoryHttpClient}
import models.Arrival
import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.Future

class CustomsInventoryLinkingExportsConnectorSpec extends CustomExportsBaseSpec {

  val arrival = Arrival("eori1","Ducr", "Arrival")

  val headers: Seq[(String, String)] = Seq(
    "Accept" -> "application/vnd.hmrc.1.0+xml",
    "Content-Type" -> "application/xml;charset=utf-8",
    "X-Client-ID" -> "5c68d3b5-d8a7-4212-8688-6b67f18bbce7",
    "X-EORI-Identfier" -> "eori1"
  )

  "CustomsInventoryLinkingExportsConnector" should {
    "POST arrival to Customs Inventory Linking Exports" in sendArrival(arrival) { response =>
      response.futureValue.status must be (ACCEPTED)
    }
  }

  def sendArrival(
    arrival: Arrival,
    hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(randomString(255))))
  )(test: Future[HttpResponse] => Unit): Unit = {
    val expectedUrl: String = s"${appConfig.customsInventoryLinkingExports}${appConfig.sendArrival}"
    val falseServerError: Boolean = false
    val expectedHeaders: Seq[(String, String)] = headers
    val http = new MockInventoryHttpClient(expectedUrl, arrival, expectedHeaders, falseServerError)
    val client = new CustomsInventoryLinkingExportsConnector(appConfig, http)
    test(client.sendArrival(arrival)(hc, ec))
  }
}
