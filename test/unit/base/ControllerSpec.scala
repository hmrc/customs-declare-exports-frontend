/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.util.{Add, AddField, SaveAndContinue}
import models.DeclarationType.DeclarationType
import models.requests.{ExportsSessionKeys, JourneyRequest}
import models.{DeclarationType, ExportsDeclaration}
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import play.twirl.api.Html
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import unit.mock.JourneyActionMocks
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
    extends UnitSpec with Stubs with MockAuthAction with MockConnectors with MockExportCacheService with MockNavigator with ExportsDeclarationBuilder
    with ExportsItemBuilder with JourneyActionMocks {

  implicit val ec: ExecutionContext = ExecutionContext.global

  protected val config: AppConfig = mock[AppConfig]

  protected def addActionUrlEncoded(field: String = ""): (String, String) = if (field.isEmpty) (Add.toString, field) else (AddField.toString, field)

  protected val saveAndContinueActionUrlEncoded: (String, String) = (SaveAndContinue.toString, "")

  protected def viewOf(result: Future[Result]) = Html(contentAsString(result))

  protected def getRequest(declaration: ExportsDeclaration): JourneyRequest[AnyContentAsEmpty.type] =
    new JourneyRequest(getAuthenticatedRequest(), declaration)

  protected def postRequest(body: JsValue): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withSession(ExportsSessionKeys.declarationId -> "declaration-id")
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequest(body: JsValue, declaration: ExportsDeclaration): Request[AnyContentAsJson] =
    FakeRequest("POST", "")
      .withSession(ExportsSessionKeys.declarationId -> declaration.id)
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

  def onEveryDeclarationJourney(modifiers: ExportsDeclarationModifier*)(f: ExportsDeclaration => Unit): Unit =
    onJourney(DeclarationType.values.toSeq: _*)(modifiers: _*)(f)

  def onJourney(types: DeclarationType*)(modifiers: ExportsDeclarationModifier*)(f: ExportsDeclaration => Unit): Unit = {
    if (types.isEmpty) {
      throw new RuntimeException("Attempt to test against no types - please provide at least one declaration type")
    }
    val declaration = aDeclaration(modifiers: _*)
    types.foreach {
      case kind @ DeclarationType.STANDARD      => onStandard(aDeclarationAfter(declaration, withType(kind)))(f)
      case kind @ DeclarationType.SUPPLEMENTARY => onSupplementary(aDeclarationAfter(declaration, withType(kind)))(f)
      case kind @ DeclarationType.SIMPLIFIED    => onSimplified(aDeclarationAfter(declaration, withType(kind)))(f)
      case kind @ DeclarationType.OCCASIONAL    => onOccasional(aDeclarationAfter(declaration, withType(kind)))(f)
      case kind @ DeclarationType.CLEARANCE     => onClearance(aDeclarationAfter(declaration, withType(kind)))(f)
      case _                                    => throw new RuntimeException("Unrecognized declaration type - you could have to implement helper methods")
    }
  }

  def onStandard(f: ExportsDeclaration => Unit): Unit =
    onStandard(ControllerSpec.simpleStandardDeclaration)(f)

  def onStandard(declaration: ExportsDeclaration)(f: ExportsDeclaration => Unit): Unit =
    "on Standard journey handle request" that {
      f(declaration)
    }

  def onSimplified(f: ExportsDeclaration => Unit): Unit =
    onSimplified(ControllerSpec.simpleSimplifiedDeclaration)(f)

  def onSimplified(declaration: ExportsDeclaration)(f: ExportsDeclaration => Unit): Unit =
    "on Simplified journey handle request" that {
      f(declaration)
    }

  def onSupplementary(f: ExportsDeclaration => Unit): Unit =
    onSupplementary(ControllerSpec.simpleSupplementaryDeclaration)(f)

  def onSupplementary(declaration: ExportsDeclaration)(f: ExportsDeclaration => Unit): Unit =
    "on Supplementary journey handle request" that {
      f(declaration)
    }

  def onOccasional(f: ExportsDeclaration => Unit): Unit =
    onOccasional(ControllerSpec.simpleOccasionalDeclaration)(f)

  def onOccasional(declaration: ExportsDeclaration)(f: ExportsDeclaration => Unit): Unit =
    "on Occasional journey handle request" that {
      f(declaration)
    }

  def onClearance(f: ExportsDeclaration => Unit): Unit =
    onClearance(ControllerSpec.simpleClearanceDeclaration)(f)

  def onClearance(declaration: ExportsDeclaration)(f: ExportsDeclaration => Unit): Unit =
    "on Clearance journey handle request" that {
      f(declaration)
    }
}

object ControllerSpec extends ExportsDeclarationBuilder {
  val simpleStandardDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.STANDARD))
  val simpleSupplementaryDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.SUPPLEMENTARY))
  val simpleSimplifiedDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.SIMPLIFIED))
  val simpleOccasionalDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.OCCASIONAL))
  val simpleClearanceDeclaration: ExportsDeclaration = aDeclaration(withType(DeclarationType.CLEARANCE))
}
