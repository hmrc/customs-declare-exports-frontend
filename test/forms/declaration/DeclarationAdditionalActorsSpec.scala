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

package forms.declaration

import forms.declaration.DeclarationAdditionalActors.PartyType.{Consolidator, FreightForwarder}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DeclarationAdditionalActorsSpec extends WordSpec with MustMatchers {
  import DeclarationAdditionalActorsSpec._

  "DeclarationAdditionalActors mapping used for binding data" should {

    "return form with errors" when {
      "provided with empty input for party type" in {
        val form = DeclarationAdditionalActors.form().bind(correctEORIPartyNotSelectedJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("supplementary.partyType.empty")
      }

      "provided with unknown value for party type" in {
        val declarationAdditionalActorsInputData =
          JsObject(Map("eori" -> JsString("eori1"), "partyType" -> JsString("Incorrect")))
        val form = DeclarationAdditionalActors.form().bind(declarationAdditionalActorsInputData)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("supplementary.partyType.error")
      }
    }

    "return form without errors" when {
      "provided with valie input" in {
        val form = DeclarationAdditionalActors.form().bind(correctAdditionalActorsJSON)

        form.hasErrors must be(false)
      }
    }
  }

}

object DeclarationAdditionalActorsSpec {
  val correctAdditionalActors1 = DeclarationAdditionalActors(eori = Some("eori1"), partyType = Some(Consolidator))
  val correctAdditionalActors2 = DeclarationAdditionalActors(eori = Some("eori99"), partyType = Some(FreightForwarder))

  val emptyAdditionalActors = DeclarationAdditionalActors(eori = None, partyType = None)
  val correctEORIPartyNotSelected = DeclarationAdditionalActors(eori = Some("1234567890123456"), partyType = None)
  val incorrectAdditionalActors =
    DeclarationAdditionalActors(eori = Some("123456789123456789"), partyType = Some("Incorrect"))

  val correctAdditionalActorsJSON: JsValue = JsObject(Map("eori" -> JsString("eori1"), "partyType" -> JsString(Consolidator)))
  val emptyAdditionalActorsJSON: JsValue = JsObject(Map("eori" -> JsString(""), "partyType" -> JsString("")))

  val correctEORIPartyNotSelectedJSON: JsValue = JsObject(Map("eori" -> JsString("1234567890123456")))
  val incorrectAdditionalActorsJSON: JsValue = JsObject(Map("eori" -> JsString("123456789123456789"), "partyType" -> JsString("Incorrect")))

  val correctAdditionalActorsMap: Map[String, String] = Map("eori" -> "eori1", "partyType" -> "CS")
}
