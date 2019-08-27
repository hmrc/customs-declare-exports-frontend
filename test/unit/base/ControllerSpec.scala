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

import base.{MockAuthAction, MockConnectors, MockExportCacheService, MockNavigator}
import config.AppConfig
import controllers.util.{Add, SaveAndContinue}
import models.ExportsDeclaration
import models.requests.{ExportsSessionKeys, JourneyRequest}
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, AnyContentAsJson, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import play.twirl.api.Html
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import unit.mock.JourneyActionMocks
import unit.tools.Stubs
import play.api.test.Helpers._
import utils.FakeRequestCSRFSupport._

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
    extends UnitSpec with Stubs with MockAuthAction with MockConnectors with MockExportCacheService with MockNavigator
    with ExportsDeclarationBuilder with ExportsItemBuilder with JourneyActionMocks {

  implicit val ec: ExecutionContext = ExecutionContext.global

  protected val config: AppConfig = mock[AppConfig]

  protected val addActionUrlEncoded: (String, String) = (Add.toString, "")

  protected val saveAndContinueActionUrlEncoded: (String, String) = (SaveAndContinue.toString, "")

  protected def viewOf(result: Future[Result]) = Html(contentAsString(result))

  protected def getRequest(declaration: ExportsDeclaration): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(getAuthenticatedRequest(), declaration)

  protected def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withSession(ExportsSessionKeys.declarationId -> "declaration-id")
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequest(body: JsValue, declaration: ExportsDeclaration): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withSession(ExportsSessionKeys.declarationId -> declaration.id.getOrElse(""))
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequestAsFormUrlEncoded(body: (String, String)*): Request[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "")
      .withSession(ExportsSessionKeys.declarationId -> "declaration-id")
      .withFormUrlEncodedBody(body: _*)
      .withCSRFToken

  protected def deleteRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("DELETE", "")
      .withSession(ExportsSessionKeys.declarationId -> "declaration-id")
      .withJsonBody(body)
      .withCSRFToken
}
