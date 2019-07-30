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
import controllers.actions.JourneyAction
import controllers.util.{Add, SaveAndContinue}
import handlers.ErrorHandler
import metrics.ExportsMetrics
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.libs.json.JsValue
import play.api.mvc.Results.BadRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.cache.{ExportsCacheItemBuilder, ExportsCacheModelBuilder}
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._

import scala.concurrent.{ExecutionContext, Future}

trait ErrorHandlerMocks extends BeforeAndAfterEach { self: MockitoSugar with Suite =>

  val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

  def setupErrorHandler(): Unit = {
    when(mockErrorHandler.standardErrorTemplate(anyString, anyString, anyString)(any()))
      .thenReturn(Html.apply(""))

    when(mockErrorHandler.displayErrorPage()(any())).thenReturn(Future.successful(BadRequest(Html.apply(""))))
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(mockErrorHandler)
    super.afterEach()
  }
}

trait ExportsMetricsMocks extends BeforeAndAfterEach { self: MockitoSugar with Suite =>
  val mockExportsMetrics: ExportsMetrics = mock[ExportsMetrics]

  override protected def afterEach(): Unit = {
    Mockito.reset(mockExportsMetrics)
    super.afterEach()
  }
}

trait JourneyActionMocks extends MockExportsCacheService with BeforeAndAfterEach { self: MockitoSugar with Suite with Stubs =>

  val mockJourneyAction: JourneyAction = JourneyAction(mockExportsCacheService)(ExecutionContext.global)
}

trait ControllerSpec
  extends UnitSpec with Stubs with MockAuthAction with MockConnectors with MockCustomsCacheService
    with MockExportsCacheService with ExportsCacheModelBuilder with ExportsCacheItemBuilder {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val addActionUrlEncoded: (String, String) = (Add.toString, "")

  val saveAndContinueActionUrlEncoded: (String, String) = (SaveAndContinue.toString, "")

  def getRequest(): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "").withSession(("sessionId", "sessionId")).withCSRFToken

  def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "").withSession(("sessionId", "sessionId")).withJsonBody(body).withCSRFToken

  def postRequestAsFormUrlEncoded(body: (String, String)*): Request[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "").withSession(("sessionId", "sessionId")).withFormUrlEncodedBody(body: _*).withCSRFToken
}
