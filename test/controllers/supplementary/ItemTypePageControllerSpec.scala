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

package controllers.supplementary

import base.{CustomExportsBaseSpec, TestHelper}
import controllers.util.{FormAction, SaveAndContinue}
import forms.supplementary.ItemType
import forms.supplementary.ItemType.{nationalAdditionalCodesKey, taricAdditionalCodesKey}
import forms.supplementary.ItemTypeSpec._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class ItemTypePageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  import ItemTypePageControllerSpec._

  private val uri = uriWithContextPath("/declaration/supplementary/item-type")

  before {
    authorizedUser()
    withCaching[ItemType](None, ItemType.id)
  }

  "Item Type Page Controller on display page" should {

    "display the whole content" in {
      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("supplementary.itemType.title"))
      resultAsString must include(messages("supplementary.itemType.combinedNomenclatureCode.header"))
      resultAsString must include(messages("supplementary.itemType.combinedNomenclatureCode.header.hint"))
      resultAsString must include(messages("supplementary.itemType.taricAdditionalCodes.header"))
      resultAsString must include(messages("supplementary.itemType.taricAdditionalCodes.header.hint"))
      resultAsString must include(messages("supplementary.itemType.nationalAdditionalCode.header"))
      resultAsString must include(messages("supplementary.itemType.nationalAdditionalCode.header.hint"))
      resultAsString must include(messages("supplementary.itemType.description.header"))
      resultAsString must include(messages("supplementary.itemType.description.header.hint"))
      resultAsString must include(messages("supplementary.itemType.cusCode.header"))
      resultAsString must include(messages("supplementary.itemType.cusCode.header.hint"))
      resultAsString must include(messages("supplementary.itemType.statisticalValue.header"))
      resultAsString must include(messages("supplementary.itemType.statisticalValue.header.hint"))
      resultAsString must include(messages("supplementary.itemType.description.header"))
      resultAsString must include(messages("supplementary.itemType.description.header.hint"))
    }

    "display \"Back\" button that links to \"procedure codes\" page" in {
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/supplementary/procedure-codes")
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }
  }

  "Item Type Page Controller on submit item page" should {

    "display the form page with error" when {

      "Combined Nomenclature Code is empty" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)()
        val expectedErrorMessage = messages("supplementary.itemType.combinedNomenclatureCode.error.empty")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Combined Nomenclature Code is longer than 8 characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(combinedNomenclatureCode = "123456789")
        val expectedErrorMessage = messages("supplementary.itemType.combinedNomenclatureCode.error.length")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Combined Nomenclature Code contains special characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(combinedNomenclatureCode = "123$%^")
        val expectedErrorMessage = messages("supplementary.itemType.combinedNomenclatureCode.error.specialCharacters")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "TARIC additional code is not 4 characters long" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(taricAdditionalCodes = Seq("123"))
        val expectedErrorMessage = messages("supplementary.itemType.taricAdditionalCodes.error.length")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "TARIC additional code contains special characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(taricAdditionalCodes = Seq("123%"))
        val expectedErrorMessage = messages("supplementary.itemType.taricAdditionalCodes.error.specialCharacters")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "National additional code is longer than 4 characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(nationalAdditionalCodes = Seq("12345"))
        val expectedErrorMessage = messages("supplementary.itemType.nationalAdditionalCode.error.length")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "National additional code contains special characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(nationalAdditionalCodes = Seq("12#%"))
        val expectedErrorMessage = messages("supplementary.itemType.nationalAdditionalCode.error.specialCharacters")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Description of goods is empty" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)()
        val expectedErrorMessage = messages("supplementary.itemType.description.error.empty")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Description of goods is longer than 280 characters" in {
        val descriptionMaxLength = 280
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          descriptionOfGoods = TestHelper.createRandomString(descriptionMaxLength + 1)
        )
        val expectedErrorMessage = messages("supplementary.itemType.description.error.length")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "CUS code is not 8 characters long" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(cusCode = "1234567")
        val expectedErrorMessage = messages("supplementary.itemType.cusCode.error.length")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "CUS code contains special characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(cusCode = "1234@#$%")
        val expectedErrorMessage = messages("supplementary.itemType.cusCode.error.specialCharacters")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Statistical value is empty" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)()
        val expectedErrorMessage = messages("supplementary.itemType.statisticalValue.error.empty")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Statistical value contains more than 15 digits" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(statisticalValue = "12345678901234.56")
        val expectedErrorMessage = messages("supplementary.itemType.statisticalValue.error.length")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      "Statistical value contains non-digit characters" in {
        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(statisticalValue = "123456Q.78")
        val expectedErrorMessage = messages("supplementary.itemType.statisticalValue.error.wrongFormat")
        testErrorInFormScenario(form, expectedErrorMessage)
      }

      def testErrorInFormScenario(form: Map[String, String], expectedErrorMessage: String): Unit = {
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        contentAsString(result) must include(expectedErrorMessage)
      }
    }

    "save data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[ItemType](None, ItemType.id)

      route(app, postRequestFormUrlEncoded(uri, correctItemTypeFormUrlEncoded.toSeq: _*)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[ItemType](any(), ArgumentMatchers.eq(ItemType.id), any())(any(), any(), any())
    }

    "return 303 code" in {
      val result = route(app, postRequestFormUrlEncoded(uri, correctItemTypeFormUrlEncoded.toSeq: _*)).get
      status(result) must be(SEE_OTHER)
    }

    "redirect to \"Add Package Information\" page" in {
      val result = route(app, postRequestFormUrlEncoded(uri, correctItemTypeFormUrlEncoded.toSeq: _*)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/package-information")
      )
    }

    "redirect to \"Add Package Information\" page with mandatory fields only" in {
      val result = route(app, postRequestFormUrlEncoded(uri, mandatoryItemTypeFormUrlEncoded.toSeq: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/package-information")
      )
    }
  }
}

object ItemTypePageControllerSpec {

  val correctItemTypeFormUrlEncoded: Map[String, String] =
    buildItemTypeUrlEncodedInput(SaveAndContinue)(
      combinedNomenclatureCode = correctItemType.combinedNomenclatureCode,
      taricAdditionalCodes = correctItemType.taricAdditionalCodes,
      nationalAdditionalCodes = correctItemType.nationalAdditionalCodes,
      descriptionOfGoods = correctItemType.descriptionOfGoods,
      cusCode = correctItemType.cusCode.get,
      statisticalValue = correctItemType.statisticalValue
    )

  val mandatoryItemTypeFormUrlEncoded: Map[String, String] =
    buildItemTypeUrlEncodedInput(SaveAndContinue)(
      combinedNomenclatureCode = correctItemType.combinedNomenclatureCode,
      taricAdditionalCodes = Nil,
      nationalAdditionalCodes = Nil,
      descriptionOfGoods = correctItemType.descriptionOfGoods,
      cusCode = correctItemType.cusCode.get,
      statisticalValue = correctItemType.statisticalValue
    )

  def buildItemTypeUrlEncodedInput(formAction: FormAction)(
    combinedNomenclatureCode: String = "",
    taricAdditionalCodes: Seq[String] = Seq.empty,
    nationalAdditionalCodes: Seq[String] = Seq.empty,
    descriptionOfGoods: String = "",
    cusCode: String = "",
    statisticalValue: String = ""
  ) =
    Map(formAction.toString -> "") ++
      Map(
        "combinedNomenclatureCode" -> combinedNomenclatureCode,
        "descriptionOfGoods" -> descriptionOfGoods,
        "cusCode" -> cusCode,
        "statisticalValue" -> statisticalValue
      ) ++ taricAdditionalCodes.zipWithIndex.map {
      case (code, idx) => taricAdditionalCodesKey + "[" + idx + "]" -> code
    } ++ nationalAdditionalCodes.zipWithIndex.map {
      case (code, idx) => nationalAdditionalCodesKey + "[" + idx + "]" -> code
    }

}