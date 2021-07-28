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

import base.TestHelper
import forms.common.DeclarationPageBaseSpec
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}

class GoodsLocationFormSpec extends DeclarationPageBaseSpec {

  private val validCode = "GBAUFXTFXTFXT"

  "GoodsLocation form" should {

    "return form with errors" when {

      "provided with a Code" which {

        "is missing" in {
          val form = GoodsLocationForm.form().bind(JsObject(Map("unexpected" -> JsString(""))), Form.FromJsonMaxChars)

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("error.required")
        }

        "is empty" in {
          val form = getBoundedForm("")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.empty")
        }

        "is longer than 39 characters" in {
          val form = getBoundedForm(TestHelper.createRandomAlphanumericString(40))

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error")
        }

        "is shorter than 10 characters" in {
          val form = getBoundedForm(TestHelper.createRandomAlphanumericString(9))

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error")
        }

        "is alphanumeric" in {
          val form = getBoundedForm(s"${validCode}*")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error")
        }

        "does not contain a valid country" in {
          val form = getBoundedForm(s"XX${validCode}")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error")
        }

        "does not contain a valid location type" in {
          val form = getBoundedForm(s"GBX${validCode}")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error")
        }

        "does not contain a valid qualifier code" in {
          val form = getBoundedForm(s"GBAX${validCode}")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error")
        }
      }
    }

    "convert to upper case" in {
      val form = getBoundedForm(validCode.toLowerCase)

      form.value.map(_.code) must be(Some(validCode))
    }

    "trim white spaces" in {
      val form = getBoundedForm(s"\n \t${validCode}\t \n")

      form.value.map(_.code) must be(Some(validCode))
    }
  }

  "GoodsLocationForm" when {
    testTariffContentKeys(GoodsLocationForm, "tariff.declaration.locationOfGoods")
  }

  private def getBoundedForm(code: String) = GoodsLocationForm.form().bind(JsObject(Map("code" -> JsString(code))), Form.FromJsonMaxChars)
}
