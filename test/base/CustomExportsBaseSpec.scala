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

package base

import akka.stream.Materializer
import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import metrics.ExportsMetrics
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

trait CustomExportsBaseSpec
    extends PlaySpec with MockitoSugar with ScalaFutures with MockAuthAction with MockConnectors with BeforeAndAfterEach
    with ExportsDeclarationBuilder with MockExportCacheService {

  implicit val hc: HeaderCarrier =
    HeaderCarrier(
      authorization = Some(Authorization(TestHelper.createRandomString(255))),
      nsStamp = DateTime.now().getMillis
    )

  SharedMetricRegistries.clear()

  val injector = GuiceApplicationBuilder().injector()

  implicit val mat: Materializer = injector.instanceOf[Materializer]

  implicit val ec: ExecutionContext = global

  val appConfig: AppConfig = injector.instanceOf[AppConfig]

  val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  val exportsMetricsMock = injector.instanceOf[ExportsMetrics]

  implicit val messages: Messages = messagesApi.preferred(FakeRequest("", ""))

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))
}
