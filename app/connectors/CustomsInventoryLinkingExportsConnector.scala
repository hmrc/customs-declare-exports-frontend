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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsInventoryLinkingExportsConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient) {

  def sendMovementRequest(eori: String, body: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    postMovementRequest(eori, body).map { response =>
      Logger.debug(s"CUSTOMS_INVENTORY_LINKING_EXPORTS response is --> ${response.toString}")
      response
    }

  private[connectors] def postMovementRequest(eori: String, body: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val headers: Seq[(String, String)] = Seq(
      "Accept" -> "application/vnd.hmrc.1.0+xml",
      "Content-Type" -> "application/xml;charset=utf-8",
      "X-Client-ID" -> appConfig.clientIdInventory,
      "X-EORI-Identfier" -> eori
    )

    httpClient.POSTString(
      s"${appConfig.customsInventoryLinkingExports}${appConfig.sendArrival}", body, headers
    )
  }
}
