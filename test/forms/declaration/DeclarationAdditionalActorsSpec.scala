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

package forms.declaration

import forms.common.{DeclarationPageBaseSpec, Eori}
import forms.declaration.DeclarationAdditionalActors.PartyType.{Consolidator, FreightForwarder}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DeclarationAdditionalActorsSpec extends DeclarationPageBaseSpec {
  import DeclarationAdditionalActorsSpec._

  "DeclarationAdditionalActors mapping used for binding data" should {

    "return form with errors" when {
      "provided with empty input for party type CS" in {
        val form = DeclarationAdditionalActors.form.bind(emptyAdditionalActorsJSON("CS"), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.empty")
      }

      "provided with empty input for party type MF" in {
        val form = DeclarationAdditionalActors.form.bind(emptyAdditionalActorsJSON("MF"), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.empty")
      }

      "provided with empty input for party type FW" in {
        val form = DeclarationAdditionalActors.form.bind(emptyAdditionalActorsJSON("FW"), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.empty")
      }

      "provided with empty input for party type WH" in {
        val form = DeclarationAdditionalActors.form.bind(emptyAdditionalActorsJSON("WH"), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.empty")
      }

      "provided with unknown value for party type" in {
        val declarationAdditionalActorsInputData =
          JsObject(Map("eori" -> JsString("GB12345678912345"), "partyType" -> JsString("Incorrect")))
        val form = DeclarationAdditionalActors.form.bind(declarationAdditionalActorsInputData, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.partyType.error")
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = DeclarationAdditionalActors.form.bind(correctAdditionalActorsJSON, JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

  "DeclarationAdditionalActors" when {
    testTariffContentKeysNoSpecialisation(DeclarationAdditionalActors, "tariff.declaration.otherPartiesInvolved")
  }
}

object DeclarationAdditionalActorsSpec {
  val correctAdditionalActors1 = DeclarationAdditionalActors(eori = Some(Eori("eori1")), partyType = Some(Consolidator))
  val correctAdditionalActors2 = DeclarationAdditionalActors(eori = Some(Eori("eori99")), partyType = Some(FreightForwarder))

  val emptyAdditionalActors = DeclarationAdditionalActors(eori = None, partyType = None)
  val correctEORIPartyNotSelected = DeclarationAdditionalActors(eori = Some(Eori("1234567890123456")), partyType = None)
  val incorrectAdditionalActors =
    DeclarationAdditionalActors(eori = Some(Eori("123456789123456789")), partyType = Some("Incorrect"))

  val correctAdditionalActorsJSON: JsValue = JsObject(Map("eoriCS" -> JsString("GB12345678912345"), "partyType" -> JsString(Consolidator)))
  val emptyAdditionalActorsJSON: JsValue = JsObject(Map("eori" -> JsString(""), "partyType" -> JsString("")))

  val incorrectAdditionalActorsJSON: JsValue = JsObject(Map("eori" -> JsString("123456789123456789"), "partyType" -> JsString("Incorrect")))
  def emptyAdditionalActorsJSON(partyType: String): JsValue = JsObject(Map(s"eori$partyType" -> JsString(""), "partyType" -> JsString(partyType)))
  def incorrectAdditionalActorsJSON(partyType: String): JsValue =
    JsObject(Map("eori" -> JsString("123456789123456789"), "partyType" -> JsString(partyType)))

  val correctAdditionalActorsMap: Map[String, String] = Map("eori" -> "eori1", "partyType" -> "CS")
}
