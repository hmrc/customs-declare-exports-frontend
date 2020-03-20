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

import forms.declaration.CommodityDetails._
import models.DeclarationType
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class CommodityDetailsSpec extends WordSpec with MustMatchers {
  import CommodityDetailsSpec._

  "CommodityDetails mapping used for declarations where code required" should {

    for (decType <- Set(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)) {

      s"return form with errors for $decType" when {
        "provided with invalid commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("A1234", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with commodity code too long" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567890", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with commodity code too short" in {
          val form = CommodityDetails.form(decType).bind(formData("1234", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with invalid commodity code that is too long" in {
          val form = CommodityDetails.form(decType).bind(formData("ABCDE", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with missing commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("", "some text"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.empty"))
        }

        "provided with missing description" in {
          val form = CommodityDetails.form(decType).bind(formData("12345678", ""))

          form.errors mustBe Seq(FormError(descriptionOfGoodsKey, "declaration.commodityDetails.description.error.empty"))
        }
      }

      s"return form without errors for $decType" when {
        "provided with valid input" in {
          val form = CommodityDetails.form(decType).bind(formData("12345678", "description"))

          form.hasErrors must be(false)
        }
      }
    }
  }

  "CommodityDetails mapping used for declarations where code and declaration is optional" should {

    for (decType <- Set(DeclarationType.CLEARANCE)) {

      s"return form with errors for $decType" when {
        "provided with invalid commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("#1234", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with commodity code too short" in {
          val form = CommodityDetails.form(decType).bind(formData("12345", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with commodity code too long" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567890", "description"))

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with missing description" in {
          val form = CommodityDetails.form(decType).bind(formData("12345678", ""))

          form.hasErrors must be(false)
        }
      }

      s"return form without errors for $decType" when {
        "provided with valid input" in {
          val form = CommodityDetails.form(decType).bind(formData("12345678", "description"))

          form.hasErrors must be(false)
        }

        "provided with missing commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("", "description"))

          form.hasErrors must be(false)
        }

        "provided with both missing commodity code and commodity description" in {
          val form = CommodityDetails.form(decType).bind(formData("", ""))

          form.hasErrors must be(false)
        }
      }
    }
  }
}

object CommodityDetailsSpec {
  def formData(code: String, description: String) =
    JsObject(Map(combinedNomenclatureCodeKey -> JsString(code), descriptionOfGoodsKey -> JsString(description)))
}
