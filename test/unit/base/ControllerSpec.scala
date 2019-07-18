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

package unit.base

import base.{MockAuthAction, MockConnectors, MockCustomsCacheService, MockExportsCacheService}
import com.kenshoo.play.metrics.MetricsImpl
import handlers.ErrorHandler
import metrics.ExportsMetrics
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._

import scala.concurrent.ExecutionContext

trait ControllerSpec
    extends UnitSpec with Stubs with MockAuthAction with MockConnectors with MockCustomsCacheService
    with MockExportsCacheService {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val mockErrorHandler = mock[ErrorHandler]

  when(mockErrorHandler.standardErrorTemplate(anyString, anyString, anyString)(any()))
    .thenReturn(Html.apply(""))

  val mockExportsMetrics = new ExportsMetrics(new MetricsImpl(new DefaultApplicationLifecycle(), minimalConfiguration))

  def getRequest(): Request[AnyContentAsEmpty.type] =
    FakeRequest("", "").withSession(("sessionId", "sessionId")).withCSRFToken

  def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("", "").withSession(("sessionId", "sessionId")).withJsonBody(body).withCSRFToken
}
