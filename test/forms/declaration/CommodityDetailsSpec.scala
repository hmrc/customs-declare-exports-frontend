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

import forms.common.DeclarationPageBaseSpec
import forms.declaration.CommodityDetails._
import models.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class CommodityDetailsSpec extends DeclarationPageBaseSpec {
  import CommodityDetailsSpec._

  "CommodityDetails mapping used for declarations where code required" should {

    for (decType <- Set(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY)) {

      s"return form without errors for $decType" when {

        "provided with a commodity code 8 digits long" in {
          val form = CommodityDetails.form(decType).bind(formData("12345678", "description"), JsonBindMaxChars)
          form.errors mustBe empty
        }

        "provided with a commodity code 8 digits long prefixed and suffixed with spaces" in {
          val form = CommodityDetails.form(decType).bind(formData("  12345678  ", "description"), JsonBindMaxChars)
          form.errors mustBe empty
        }

        "provided with a commodity code 10 digits long" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567890", "description"), JsonBindMaxChars)
          form.errors mustBe empty
        }

        "provided with a commodity code 10 digits long prefixed and suffixed with spaces" in {
          val form = CommodityDetails.form(decType).bind(formData("  1234567890  ", "description"), JsonBindMaxChars)
          form.errors mustBe empty
        }
      }

      s"return form with errors for $decType" when {

        "provided with invalid commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("A123456789", "description"), JsonBindMaxChars)
          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with a commodity code not 8 or 10 digits long" in {
          val form1 = CommodityDetails.form(decType).bind(formData("12345678901", "description"), JsonBindMaxChars)
          form1.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))

          val form2 = CommodityDetails.form(decType).bind(formData("1234", "description"), JsonBindMaxChars)
          form2.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
        }

        "provided with invalid commodity code that is too long" in {
          val form = CommodityDetails.form(decType).bind(formData("ABCDE123456789", "description"), JsonBindMaxChars)

          form.errors mustBe Seq(
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"),
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length")
          )
        }

        "provided with missing commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("", "some text"), JsonBindMaxChars)

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.empty"))
        }

        "provided with missing description" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567890", ""), JsonBindMaxChars)

          form.errors mustBe Seq(FormError(descriptionOfGoodsKey, "declaration.commodityDetails.description.error.empty"))
        }
      }

      s"return form without errors for $decType" when {
        "provided with valid input" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567809", "description"), JsonBindMaxChars)

          form.hasErrors must be(false)
        }
      }
    }
  }

  "CommodityDetails mapping used for declarations where code and declaration is optional" should {

    for (decType <- Set(DeclarationType.CLEARANCE)) {

      s"return form with errors for $decType" when {
        "provided with invalid commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("#1234", "description"), JsonBindMaxChars)

          form.errors mustBe Seq(
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"),
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length")
          )
        }

        "provided with commodity code too short" in {
          val form = CommodityDetails.form(decType).bind(formData("12345", "description"), JsonBindMaxChars)

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
        }

        "provided with commodity code too long" in {
          val form = CommodityDetails.form(decType).bind(formData("12345678901", "description"), JsonBindMaxChars)

          form.errors mustBe Seq(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
        }

        "provided with missing description" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567890", ""), JsonBindMaxChars)

          form.hasErrors must be(false)
        }
      }

      s"return form without errors for $decType" when {
        "provided with valid input" in {
          val form = CommodityDetails.form(decType).bind(formData("1234567890", "description"), JsonBindMaxChars)

          form.hasErrors must be(false)
        }

        "provided with missing commodity code" in {
          val form = CommodityDetails.form(decType).bind(formData("", "description"), JsonBindMaxChars)

          form.hasErrors must be(false)
        }

        "provided with both missing commodity code and commodity description" in {
          val form = CommodityDetails.form(decType).bind(formData("", ""), JsonBindMaxChars)

          form.hasErrors must be(false)
        }
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(
      TariffContentKey(s"${messageKey}.1.clearance"),
      TariffContentKey(s"${messageKey}.2.clearance"),
      TariffContentKey(s"${messageKey}.3.clearance")
    )

  "CommodityDetails" when {
    testTariffContentKeys(CommodityDetails, "tariff.declaration.item.commodityDetails")
  }
}

object CommodityDetailsSpec {
  def formData(code: String, description: String) =
    JsObject(Map(combinedNomenclatureCodeKey -> JsString(code), descriptionOfGoodsKey -> JsString(description)))
}
