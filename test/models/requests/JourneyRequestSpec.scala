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

package models.requests

import base.MockAuthAction
import models.DeclarationType
import play.api.mvc.AnyContentAsEmpty
import services.cache.ExportsDeclarationBuilder
import unit.base.UnitSpec

class JourneyRequestSpec extends UnitSpec with ExportsDeclarationBuilder with MockAuthAction {

  val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL))
  val authenticatedRequest = getAuthenticatedRequest()
  val request = new JourneyRequest(authenticatedRequest, declaration)

  "Journey request" should {

    "have a correct declaration type" in {

      request.declarationType mustBe DeclarationType.OCCASIONAL
    }

    "check if type is contained in allowed values" in {

      request.isType(DeclarationType.CLEARANCE) mustBe false
      request.isType(DeclarationType.OCCASIONAL, DeclarationType.CLEARANCE) mustBe true
    }

    "check if eori is correct" in {
      request.eori mustBe "12345"
    }
  }
}
