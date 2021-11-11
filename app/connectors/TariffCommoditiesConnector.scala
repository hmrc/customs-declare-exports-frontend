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
import play.mvc.Http.Status.OK
import uk.gov.hmrc.http.{HttpClient, _}

import java.net.URL
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TariffCommoditiesConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, metrics: ExportsMetrics)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def getCommodity(commodityCode: String)(implicit hc: HeaderCarrier): Future[String] = {
    val timer = metrics.startTimer(MetricIdentifiers.tariffCommoditiesMetric)
    get(commodityCode) map { res =>
      logger.debug(s"TARIFF_COMMODITIES response is  --> ${res.toString}")
      res match {
        case TariffCommoditiesResponse(OK, Some(commodityJson)) =>
          timer.stop()
          commodityJson
        case TariffCommoditiesResponse(status, _) =>
          timer.stop()
          throw new InternalServerException(s"TARIFF_COMMODITIES returned [$status]")
      }
    }
  }

  private[connectors] def get(commodityCode: String)(implicit hc: HeaderCarrier): Future[TariffCommoditiesResponse] = {
    logger.debug(s"TARIFF_COMMODITIES request code is -> $commodityCode")
    httpClient
      .GET(commoditiesUrl(commodityCode), headers = headers())
      .recover {
        case error: Throwable =>
          logger.error(s"Error while fetching commodities: ${error.getMessage}")
          TariffCommoditiesResponse(Status.INTERNAL_SERVER_ERROR, None)
      }
  }

  private def commoditiesUrl(commodityCode: String): URL = new URL(s"${appConfig.tariffCommoditiesUri}/id/$commodityCode")

  private def headers(): Seq[(String, String)] = Seq(HeaderNames.ACCEPT -> MimeTypes.JSON, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[TariffCommoditiesResponse] =
    new HttpReads[TariffCommoditiesResponse] {
      override def read(method: String, url: String, response: HttpResponse): TariffCommoditiesResponse = {
        logger.debug(s"Response: ${response.status} => ${response.body}")
        response.status match {
          case OK =>
            TariffCommoditiesResponse(response.status, Some(response.body))
          case status if HttpErrorFunctions.is4xx(status) =>
            throw UpstreamErrorResponse(
              message = "Invalid request made to Tariff Commodities API",
              statusCode = status,
              reportAs = Status.INTERNAL_SERVER_ERROR,
              headers = response.headers
            )
          case status if HttpErrorFunctions.is5xx(status) =>
            throw UpstreamErrorResponse(
              message = "Tariff Commodities API unable to service request",
              statusCode = status,
              reportAs = Status.INTERNAL_SERVER_ERROR
            )
          case _ =>
            throw UpstreamErrorResponse(
              message = "Unexpected response from Tariff Commodities API response",
              statusCode = response.status,
              reportAs = Status.INTERNAL_SERVER_ERROR
            )
        }
      }
    }

}

trait TariffCommoditiesResponseReader extends HttpErrorFunctions {

  //noinspection ConvertExpressionToSAM
  private implicit val responseReader: HttpReads[TariffCommoditiesResponse] =
    new HttpReads[TariffCommoditiesResponse] {
      override def read(method: String, url: String, response: HttpResponse): TariffCommoditiesResponse =
        response.status match {
          case OK =>
            TariffCommoditiesResponse(response.status, Some(response.body))
          case status if is4xx(status) =>
            throw UpstreamErrorResponse(
              message = "Invalid request made to Tariff Commodities API",
              statusCode = status,
              reportAs = Status.INTERNAL_SERVER_ERROR,
              headers = response.headers
            )
          case status if is5xx(status) =>
            throw UpstreamErrorResponse(
              message = "Tariff Commodities API unable to service request",
              statusCode = status,
              reportAs = Status.INTERNAL_SERVER_ERROR
            )
          case _ =>
            throw UpstreamErrorResponse(
              message = "Unexpected response from Tariff Commodities API response",
              statusCode = response.status,
              reportAs = Status.INTERNAL_SERVER_ERROR
            )
        }
    }

}
