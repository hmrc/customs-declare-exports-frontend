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

import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import org.mockito.BDDMockito.given
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext

class ConnectorSpec
    extends WordSpec with GuiceOneAppPerSuite with WiremockTestServer with MockitoSugar with BeforeAndAfterEach {

  /**
    * @see [[base.Injector]]
    */
  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    super.fakeApplication()
  }

  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  protected val httpClient: DefaultHttpClient = app.injector.instanceOf[DefaultHttpClient]
  protected val config: AppConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    given(config.customsDeclareExports).willReturn(host)
  }
}
