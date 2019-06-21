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

package forms.declaration

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class AdditionalFiscalReferenceSpec extends WordSpec with MustMatchers {
  import AdditionalFiscalReferenceSpec._

  "Bound form with AdditionalFiscalReference mapping" should {

    "not contain errors" when {
      "form is correct" in {
        val form = AdditionalFiscalReference.form().bind(correctForm)

        form.errors must be(empty)
      }
    }
    "contain errors" when {
      "country is incorrect" in {
        val form = AdditionalFiscalReference.form().bind(incorrectCountry)

        form.errors.length must be(1)
        form.errors.head.message must be("declaration.additionalFiscalReferences.country.error")
      }

      "reference is incorrect" in {
        val form = AdditionalFiscalReference.form().bind(incorrectReference)

        form.errors.length must be(1)
        form.errors.head.message must be("declaration.additionalFiscalReferences.reference.error")
      }
      "both country and reference are incorrect" in {
        val form = AdditionalFiscalReference.form().bind(incorrectCountryAndRef)

        form.errors.length must be(2)
        form.errors(0).message must be("declaration.additionalFiscalReferences.country.error")
        form.errors(1).message must be("declaration.additionalFiscalReferences.reference.error")
      }

      "country is empty" in {
        val form = AdditionalFiscalReference.form().bind(emptyCountry)

        form.errors.length must be(1)
        form.errors.head.message must be("declaration.additionalFiscalReferences.country.empty")
      }

      "reference is empty" in {
        val form = AdditionalFiscalReference.form().bind(emptyReference)

        form.errors.length must be(1)
        form.errors.head.message must be("declaration.additionalFiscalReferences.reference.empty")
      }

      "both country and reference are empty" in {
        val form = AdditionalFiscalReference.form().bind(emptyCountryAndRef)

        form.errors.map(_.message) must be(
          Seq(
            "declaration.additionalFiscalReferences.country.empty",
            "declaration.additionalFiscalReferences.reference.empty"
          )
        )
      }
    }
  }

  "AdditionalFiscalReferencesData" should {

    "have correct formId" in {
      AdditionalFiscalReferencesData.formId must be("AdditionalFiscalReferences")
    }

    "have correct limit" in {
      AdditionalFiscalReferencesData.limit must be(99)
    }

  }

}

object AdditionalFiscalReferenceSpec {
  val correctForm: Map[String, String] = Map("country" -> "FR", "reference" -> "12345")

  val incorrectCountry: Map[String, String] = Map("country" -> "incorrect", "reference" -> "12345")

  val incorrectReference: Map[String, String] = Map("country" -> "FR", "reference" -> "!@£abc123")

  val incorrectCountryAndRef: Map[String, String] = Map("country" -> "incorrect", "reference" -> "!@£abc123")

  val emptyCountry: Map[String, String] = Map("country" -> "", "reference" -> "12345")

  val emptyReference: Map[String, String] = Map("country" -> "FR", "reference" -> "")

  val emptyCountryAndRef: Map[String, String] = Map("country" -> "", "reference" -> "")
}
