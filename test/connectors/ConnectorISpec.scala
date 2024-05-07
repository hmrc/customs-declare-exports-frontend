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

import scala.concurrent.ExecutionContext

import base.UnitSpec
import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

class ConnectorISpec extends UnitSpec with BeforeAndAfterEach with GuiceOneAppPerSuite with WiremockTestServer {

  def overrideConfig: Map[String, Any] = Map(
    "auditing.enabled" -> true,
    "auditing.consumer.baseUri.host" -> wireHost,
    "auditing.consumer.baseUri.port" -> auditingPort,
    "microservice.services.customs-declare-exports.host" -> wireHost,
    "microservice.services.customs-declare-exports.port" -> exportsWirePort,
    "microservice.services.secure-messaging.host" -> wireHost,
    "microservice.services.secure-messaging.port" -> secureMessagingWirePort
  )

  /**
   * @see [[base.Injector]]
   */
  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().configure(overrideConfig).build()
  }

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  protected val httpClient: DefaultHttpClient = app.injector.instanceOf[DefaultHttpClient]
}
