/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import models.DeclarationType._
import models.requests.JourneyRequest
import models.viewmodels.TariffContentKey
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, Json}
import services.cache.ExportsTestHelper

class CommodityDetailsSpec extends DeclarationPageBaseSpec with ExportsTestHelper {

  "CommodityDetails mapping used for declarations where code is optional" should {
    for (decType <- List(CLEARANCE, OCCASIONAL, SIMPLIFIED, SUPPLEMENTARY)) {
      val maybeModifier = decType match {
        case CLEARANCE     => Some(withEntryIntoDeclarantsRecords())
        case SUPPLEMENTARY => Some(withAdditionalDeclarationType(SUPPLEMENTARY_EIDR))
        case _             => None
      }
      val declaration = aDeclaration(List(Some(withType(decType)), maybeModifier).flatten: _*)
      implicit val request: JourneyRequest[_] = journeyRequest(declaration)
      val form = CommodityDetails.form

      s"return a form with errors for $decType" when {
        "provided with invalid commodity code" in {
          val expectedError = List(
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"),
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length")
          )
          form.bind(formData("#1234", "description"), JsonBindMaxChars).errors mustBe expectedError
        }

        "provided with commodity code too short" in {
          val expectedError = List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
          form.bind(formData("12345", "description"), JsonBindMaxChars).errors mustBe expectedError
        }

        "provided with commodity code too long" in {
          val expectedError = List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
          form.bind(formData("123456789", "description"), JsonBindMaxChars).errors mustBe expectedError
        }

        "provided with missing description" in {
          val expectedError = List(FormError(descriptionOfGoodsKey, "declaration.commodityDetails.description.error.empty"))
          form.bind(formData("12345678", ""), JsonBindMaxChars).errors mustBe expectedError
        }
      }

      s"return a form without errors for $decType" when {
        "provided with valid input" in {
          form.bind(formData("12345678", "description"), JsonBindMaxChars).hasErrors mustBe false
        }

        "provided with missing commodity code" in {
          form.bind(formData("", "description"), JsonBindMaxChars).hasErrors mustBe false
        }
      }
    }
  }

  "CommodityDetails mapping used for declarations where code required" should {
    for (decType <- List(STANDARD, SUPPLEMENTARY)) {
      implicit val request: JourneyRequest[_] = journeyRequest(decType)

      s"return a form without errors for $decType" when {

        "provided with a commodity code 8 digits long" in {
          val form = CommodityDetails.form.bind(formData("12345678", "description"), JsonBindMaxChars)
          form.errors mustBe empty
        }

        "provided with a commodity code 8 digits long prefixed and suffixed with spaces" in {
          val form = CommodityDetails.form.bind(formData("  12345678  ", "description"), JsonBindMaxChars)
          form.errors mustBe empty
        }
      }

      s"return a form with errors for $decType" when {

        "provided with invalid commodity code" in {
          val form = CommodityDetails.form.bind(formData("A1234567", "description"), JsonBindMaxChars)
          form.errors mustBe List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"))
        }

        "provided with a commodity code not 8 digits long" in {
          val form1 = CommodityDetails.form.bind(formData("123456789", "description"), JsonBindMaxChars)
          form1.errors mustBe List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))

          val form2 = CommodityDetails.form.bind(formData("1234", "description"), JsonBindMaxChars)
          form2.errors mustBe List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
        }

        "provided with invalid commodity code that is too long" in {
          val form = CommodityDetails.form.bind(formData("ABCDE1234", "description"), JsonBindMaxChars)

          form.errors mustBe List(
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"),
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length")
          )
        }

        "provided with spaces in the commodity code" in {
          val form = CommodityDetails.form.bind(formData("1234 5678", "description"), JsonBindMaxChars)

          form.errors mustBe List(
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"),
            FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length")
          )
        }

        "provided with missing commodity code" in {
          val form = CommodityDetails.form.bind(formData("", "some text"), JsonBindMaxChars)
          form.errors mustBe List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.empty"))
        }

        "provided with missing description" in {
          val form = CommodityDetails.form.bind(formData("12345678", ""), JsonBindMaxChars)
          form.errors mustBe List(FormError(descriptionOfGoodsKey, "declaration.commodityDetails.description.error.empty"))
        }
      }

      s"return a form without errors for $decType" when {
        "provided with valid input" in {
          CommodityDetails.form.bind(formData("12345678", "description"), JsonBindMaxChars).hasErrors mustBe false
        }
      }
    }
  }

  "CommodityDetails mapping used for CLEARANCE declarations where code and declaration is optional" should {
    implicit val request: JourneyRequest[_] = journeyRequest(CLEARANCE)
    val form = CommodityDetails.form

    "return a form with errors" when {
      "provided with invalid commodity code" in {
        val expectedError = List(
          FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.invalid"),
          FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length")
        )
        form.bind(formData("#1234", "description"), JsonBindMaxChars).errors mustBe expectedError
      }

      "provided with commodity code too short" in {
        val expectedError = List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
        form.bind(formData("12345", "description"), JsonBindMaxChars).errors mustBe expectedError
      }

      "provided with commodity code too long" in {
        val expectedError = List(FormError(combinedNomenclatureCodeKey, "declaration.commodityDetails.combinedNomenclatureCode.error.length"))
        form.bind(formData("123456789", "description"), JsonBindMaxChars).errors mustBe expectedError
      }

      "provided with missing description" in {
        form.bind(formData("12345678", ""), JsonBindMaxChars).hasErrors mustBe false
      }
    }

    "return a form without errors" when {
      "provided with valid input" in {
        form.bind(formData("12345678", "description"), JsonBindMaxChars).hasErrors mustBe false
      }

      "provided with missing commodity code" in {
        form.bind(formData("", "description"), JsonBindMaxChars).hasErrors mustBe false
      }

      "provided with both missing commodity code and commodity description" in {
        form.bind(formData("", ""), JsonBindMaxChars).hasErrors mustBe false
      }
    }
  }

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.1.common"), TariffContentKey(s"${messageKey}.2.common"), TariffContentKey(s"${messageKey}.3.common"))

  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.clearance"))

  "CommodityDetails" when {
    testTariffContentKeys(CommodityDetails, "tariff.declaration.item.commodityDetails")
  }

  def formData(code: String, description: String): JsObject =
    Json.obj(combinedNomenclatureCodeKey -> JsString(code), descriptionOfGoodsKey -> JsString(description))
}
