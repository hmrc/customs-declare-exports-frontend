/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.Inject
import config.AppConfig
import metrics.{ExportsMetrics, MetricIdentifiers}
import models.responses.TariffCommoditiesResponse
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, MimeTypes, Status}
import play.api.libs.json.JsValue
import play.mvc.Http.Status.OK
import uk.gov.hmrc.http.{HttpClient, _}

import java.net.URL
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TariffCommoditiesConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, metrics: ExportsMetrics)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def getCommodity(commodityCode: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    logger.debug(s"TARIFF_COMMODITIES request code is -> $commodityCode")
    val timer = metrics.startTimer(MetricIdentifiers.tariffCommoditiesMetric)

    httpClient.GET(commoditiesUrl(commodityCode)) map {
      case TariffCommoditiesResponse(status, json @ Some(commodityJson)) =>
        timer.stop()
        logger.debug(s"TARIFF_COMMODITIES returned [$status] with body --> ${commodityJson}")
        json
      case TariffCommoditiesResponse(status, _) =>
        timer.stop()
        logger.error(s"TARIFF_COMMODITIES returned [$status]")
        None
    }

  }

  private def commoditiesUrl(commodityCode: String): URL = new URL(s"${appConfig.tariffCommoditiesUri}/id/$commodityCode")

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[TariffCommoditiesResponse] =
    new HttpReads[TariffCommoditiesResponse] {
      override def read(method: String, url: String, response: HttpResponse): TariffCommoditiesResponse = {
        logger.debug(s"Response: ${response.status} => ${response.body}")
        response.status match {
          case OK =>
            TariffCommoditiesResponse(response.status, Some(response.json))
          case status if HttpErrorFunctions.is4xx(status) =>
            logger.error(s"Invalid request made to Tariff Commodities API")
            TariffCommoditiesResponse(response.status, None)
          case status if HttpErrorFunctions.is5xx(status) =>
            logger.error(s"Tariff Commodities API unable to service request")
            TariffCommoditiesResponse(response.status, None)
          case status =>
            logger.error(s"Unexpected response from Tariff Commodities API response")
            TariffCommoditiesResponse(response.status, None)
        }
      }
    }

}
