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

import forms.declaration.CommodityDetails._
import models.DeclarationType
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class CommodityDetailsSpec extends WordSpec with MustMatchers {
  import CommodityDetailsSpec._

  "CommodityDetails mapping used for standard declaration" should {

    "return form with errors" when {
      "provided with invalid commodity code" in {
        val form = CommodityDetails.form(DeclarationType.STANDARD).bind(formData("A1234", "description"))

        form.errors mustBe Seq(
          FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.specialCharacters")
        )
      }

      "provided with commodity code too long" in {
        val form = CommodityDetails.form(DeclarationType.STANDARD).bind(formData("1234567890", "description"))

        form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
      }

      "provided with missing commodity code" in {
        val form = CommodityDetails.form(DeclarationType.STANDARD).bind(formData("", "some text"))

        form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.empty"))
      }

      "provided with missing description" in {
        val form = CommodityDetails.form(DeclarationType.STANDARD).bind(formData("12345678", ""))

        form.errors mustBe Seq(FormError(descriptionOfGoodsKey, "declaration.commodityDetails.description.error.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = CommodityDetails.form(DeclarationType.STANDARD).bind(formData("12345678", "description"))

        form.hasErrors must be(false)
      }

    }
  }

  "CommodityDetails mapping used for simplified declaration" should {

    "return form with errors" when {
      "provided with invalid commodity code" in {
        val form = CommodityDetails.form(DeclarationType.SIMPLIFIED).bind(formData("#1234", "description"))

        form.errors mustBe Seq(
          FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.specialCharacters")
        )
      }

      "provided with commodity code too long" in {
        val form = CommodityDetails.form(DeclarationType.SIMPLIFIED).bind(formData("1234567890", "description"))

        form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
      }

      "provided with missing description" in {
        val form = CommodityDetails.form(DeclarationType.SIMPLIFIED).bind(formData("12345678", ""))

        form.errors mustBe Seq(FormError(descriptionOfGoodsKey, "declaration.commodityDetails.description.error.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = CommodityDetails.form(DeclarationType.SIMPLIFIED).bind(formData("12345678", "description"))

        form.hasErrors must be(false)
      }

      "provided with missing commodity code" in {
        val form = CommodityDetails.form(DeclarationType.SIMPLIFIED).bind(formData("", "description"))

        form.hasErrors must be(false)
      }
    }
  }
}

object CommodityDetailsSpec {
  def formData(code: String, description: String) =
    JsObject(Map(combinedNomenclatureCodeKey -> JsString(code), descriptionOfGoodsKey -> JsString(description)))
}
