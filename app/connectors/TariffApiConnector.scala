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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import com.google.inject.Inject
import config.featureFlags.TariffApiConfig
import javax.inject.Singleton
import metrics.{ExportsMetrics, MetricIdentifiers}
import play.api.Logger
import play.api.libs.json.JsValue
import play.mvc.Http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HttpClient

@Singleton
class TariffApiConnector @Inject() (config: TariffApiConfig, httpClient: HttpClient, metrics: ExportsMetrics)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def getCommodityOnCondition(commodityCode: String): Future[Option[JsValue]] =
    if (commodityCode.length == 10) getCommodity(commodityCode)
    else Future.successful(None)

  private def getCommodity(commodityCode: String): Future[Option[JsValue]] = {
    logger.debug(s"Request's Commodity code to Tariff API is [$commodityCode]")
    val timer = metrics.startTimer(MetricIdentifiers.tariffCommoditiesMetric)

    httpClient.doGet(s"${config.tariffCommoditiesUri}/$commodityCode") map { response =>
      timer.stop()
      response.status match {
        case OK =>
          logger.debug(s"Commodity for code [$commodityCode] via Tariff API found")
          Try(response.json) match {
            case Success(json) => Some(json)

            case Failure(exc) =>
              logger.warn("While parsing the (json) payload returned by the Tariff API:", exc)
              None
          }

        case NOT_FOUND =>
          logger.debug(s"Commodity for code [$commodityCode] via Tariff API NOT found")
          None

        case status =>
          logger.warn(s"Response from Tariff API for Commodity code [$commodityCode] is ${status}")
          None
      }
    }
  }
}
