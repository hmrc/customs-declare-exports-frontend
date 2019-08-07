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

import base.{MockAuthAction, MockConnectors, MockExportsCacheService}
import controllers.util.{Add, SaveAndContinue}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, AnyContentAsJson, Request}
import play.api.test.FakeRequest
import services.cache.{ExportsCacheModelBuilder, ExportsItemBuilder}
import unit.mock.JourneyActionMocks
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._

import scala.concurrent.ExecutionContext

trait ControllerSpec
    extends UnitSpec with Stubs with MockAuthAction with MockConnectors with MockExportsCacheService
    with ExportsCacheModelBuilder with ExportsItemBuilder with JourneyActionMocks {

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
