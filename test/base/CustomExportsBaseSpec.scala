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
import connectors.{CustomsDeclareExportsConnector, NrsConnector}
import metrics.ExportsMetrics
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import services._
import services.cache.{ExportItemIdGeneratorService, ExportsCacheService, ExportsDeclarationBuilder}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

trait CustomExportsBaseSpec
    extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with ScalaFutures with MockAuthAction
    with MockConnectors with BeforeAndAfterEach with ExportsDeclarationBuilder with MockExportCacheService {

  val mockSubmissionService: SubmissionService = mock[SubmissionService]
  val mockItemGeneratorService: ExportItemIdGeneratorService = mock[ExportItemIdGeneratorService]
  val mockNrsService: NRSService = mock[NRSService]

  implicit val hc: HeaderCarrier =
    HeaderCarrier(
      authorization = Some(Authorization(TestHelper.createRandomString(255))),
      nsStamp = DateTime.now().getMillis
    )

  SharedMetricRegistries.clear()

  override lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[AuthConnector].to(mockAuthConnector),
      bind[ExportsCacheService].to(mockExportsCacheService),
      bind[ExportItemIdGeneratorService].to(mockItemGeneratorService),
      bind[CustomsDeclareExportsConnector].to(mockCustomsDeclareExportsConnector),
      bind[NrsConnector].to(mockNrsConnector),
      bind[SubmissionService].to(mockSubmissionService),
      bind[NRSService].to(mockNrsService)
    )
    .build()

  implicit val mat: Materializer = app.materializer

  implicit val ec: ExecutionContext = global

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val exportsMetricsMock = app.injector.instanceOf[ExportsMetrics]

  implicit val messages: Messages = messagesApi.preferred(FakeRequest("", ""))

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val mockWSClient = mock[WSClient]
}
