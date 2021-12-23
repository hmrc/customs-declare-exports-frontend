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
import connectors.CodeListConnector
import forms.common.DeclarationPageBaseSpec
import models.codes.Country
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.Lang
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers.stubMessagesApi

import java.util.Locale
import scala.collection.immutable.ListMap

class GoodsLocationFormSpec extends DeclarationPageBaseSpec with MockitoSugar with BeforeAndAfterEach {

  implicit val mockCodeListConnector = mock[CodeListConnector]
  implicit val messages = stubMessagesApi().preferred(Seq(Lang(Locale.ENGLISH)))

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

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
          val form = getBoundedForm(s"GBAU${TestHelper.createRandomAlphanumericString(40)}")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error.length")
        }

        "is shorter than 10 characters" in {
          val form = getBoundedForm(s"GBAU")

          form.hasErrors must be(true)
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.goodsLocation.code.error.length")
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
