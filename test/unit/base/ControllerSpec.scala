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
import controllers.actions.JourneyAction
import controllers.util.{Add, SaveAndContinue}
import handlers.ErrorHandler
import metrics.ExportsMetrics
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.json.JsValue
import play.api.mvc.Results.BadRequest
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import services.cache.ExportsCacheModel
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
    extends UnitSpec with Stubs with MockAuthAction with MockConnectors with MockCustomsCacheService
    with MockExportsCacheService {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val mockErrorHandler = mock[ErrorHandler]

  when(mockErrorHandler.standardErrorTemplate(anyString, anyString, anyString)(any()))
    .thenReturn(Html.apply(""))

  when(mockErrorHandler.displayErrorPage()(any())).thenReturn(Future.successful(BadRequest(Html.apply(""))))

  val mockExportsMetrics = new ExportsMetrics(new MetricsImpl(new DefaultApplicationLifecycle(), minimalConfiguration))

  val mockJourneyAction = JourneyAction(mockExportsCacheService, stubMessagesControllerComponents())
  val addActionUrlEncoded = (Add.toString, "")
  val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")

  def getRequest(cacheModel: ExportsCacheModel): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(getAuthenticatedRequest(sessionId = cacheModel.sessionId), cacheModel)

  def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "").withSession(("sessionId", "sessionId")).withJsonBody(body).withCSRFToken

  def postRequestAsFormUrlEncoded(body: (String, String)*): Request[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "").withSession(("sessionId", "sessionId")).withFormUrlEncodedBody(body: _*).withCSRFToken
}
