/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.{Ducr, Lrn}
import forms.common.DeclarationPageBaseSpec
import models.viewmodels.TariffContentKey
import play.api.libs.json.{JsObject, JsString, JsValue}

class ConsignmentReferencesSpec extends DeclarationPageBaseSpec {

  import ConsignmentReferencesSpec._

  "ConsignmentReferences mapping used for binding data" should {

    "return form with errors" when {

      "provided with empty input" in {

        val form = ConsignmentReferences.form().bind(emptyJSON)

        form.hasErrors mustBe true
        form.errors.length must equal(2)
        form.errors(0).message must equal("error.required")
        form.errors(1).message must equal("error.required")
      }

      "provided with invalid input" in {

        val form = ConsignmentReferences.form().bind(correctConsignmentReferencesNoDucrJSON)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("error.ducr")
      }

      "provided with valid input" in {

        val form = ConsignmentReferences.form().bind(correctConsignmentReferencesJSON)

        form.hasErrors mustBe false
      }

      "provided with valid input lowercase input" in {

        val form = ConsignmentReferences.form().bind(correctConsignmentReferencesLowercaseDucrJSON)

        form.hasErrors mustBe false
        form.value.map(_.ducr.ducr) mustBe Some(exemplaryDucr)
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"), TariffContentKey(s"${messageKey}.3.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(
      TariffContentKey(s"${messageKey}.1.clearance"),
      TariffContentKey(s"${messageKey}.2.clearance"),
      TariffContentKey(s"${messageKey}.3.clearance")
    )

  "ConsignmentReferences" when {
    testTariffContentKeys(ConsignmentReferences, "tariff.declaration.consignmentReferences")
  }
}

object ConsignmentReferencesSpec {
  val exemplaryDucr = "8GB123456789012-1234567890QWERTYUIO"

  val emptyJSON: JsValue = JsObject(Map("" -> JsString("")))

  val correctConsignmentReferences = ConsignmentReferences(ducr = Ducr(ducr = exemplaryDucr), lrn = Lrn("123LRN"))
  val correctConsignmentReferencesNoDucr = ConsignmentReferences(ducr = Ducr(""), lrn = Lrn("123LRN"))
  val emptyConsignmentReferences = ConsignmentReferences(ducr = Ducr(""), lrn = Lrn(""))

  val correctConsignmentReferencesJSON: JsValue = JsObject(
    Map("ducr" -> JsObject(Map("ducr" -> JsString(exemplaryDucr))), "lrn" -> JsString("123LRN"))
  )
  val correctConsignmentReferencesLowercaseDucrJSON: JsValue = JsObject(
    Map("ducr" -> JsObject(Map("ducr" -> JsString(exemplaryDucr.toLowerCase))), "lrn" -> JsString("123LRN"))
  )
  val correctConsignmentReferencesNoDucrJSON: JsValue = JsObject(Map("ducr" -> JsObject(Map("ducr" -> JsString(""))), "lrn" -> JsString("123LRN")))
  val emptyConsignmentReferencesJSON: JsValue = JsObject(Map("ducr" -> JsObject(Map("ducr" -> JsString(""))), "lrn" -> JsString("")))
}
