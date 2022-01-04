/*
 * Copyright 2022 HM Revenue & Customs
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

import java.util.UUID
import base.{MockAuthAction, UnitWithMocksSpec}
import models.DeclarationType
import services.cache.ExportsDeclarationBuilder

class JourneyRequestSpec extends UnitWithMocksSpec with ExportsDeclarationBuilder with MockAuthAction {

  val sourceId = UUID.randomUUID().toString
  val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL), withSourceId(sourceId))
  val authenticatedRequest = getAuthenticatedRequest()
  val request = new JourneyRequest(authenticatedRequest, declaration)

  "Journey request" should {

    "have a correct declaration type" in {

      request.declarationType mustBe DeclarationType.OCCASIONAL
    }

    "have correct source dec Id" in {

      request.sourceDecId mustBe Some(sourceId)
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
