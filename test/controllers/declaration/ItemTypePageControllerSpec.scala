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

package controllers.declaration

import base.{CustomExportsBaseSpec, TestHelper, ViewValidator}
import controllers.util.{Add, FormAction, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.ItemType
import forms.declaration.ItemType.{nationalAdditionalCodesKey, taricAdditionalCodesKey}
import forms.declaration.ItemTypeSpec._
import helpers.views.declaration.{CommonMessages, ItemTypeMessages}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import play.api.test.Helpers._

class ItemTypePageControllerSpec
    extends CustomExportsBaseSpec with ViewValidator with ItemTypeMessages with CommonMessages {
  import ItemTypePageControllerSpec._

  private val uri = uriWithContextPath("/declaration/item-type")

  before {
    authorizedUser()
    withCaching[ItemType](None, ItemType.id)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  after {
    reset(mockCustomsCacheService)
  }

  "Item Type Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = ItemType("5555", Seq("6666"), Seq("7777"), "FaultyGoods", Some("CusCus"), "900")
      withCaching[ItemType](Some(cachedData), "ItemType")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("5555")
      page must include("6666")
      page must include("7777")
      page must include("FaultyGoods")
      page must include("CusCus")
      page must include("900")
    }

    /*
     * For some reason I cannot create the table
     * in view tests for this form using fill() - for now they are here
     */
    "display the table" when {

      "user added one TARIC" in {

        withCaching[ItemType](Some(ItemType("100", Seq("1234"), Seq(), "Description", Some(""), "100")), ItemType.id)

        val result = route(app, getRequest(uri)).get
        val page = contentAsString(result)

        getElementByCss(page, "table>tbody>tr>th:nth-child(1)").text() must be("1234")
        getElementByCss(page, "table>tbody>tr>th:nth-child(2)> button").text() must be(messages(removeCaption))
      }

      "user added one NAC" in {

        withCaching[ItemType](Some(ItemType("100", Seq(), Seq("1234"), "Description", Some(""), "100")), ItemType.id)

        val result = route(app, getRequest(uri)).get
        val page = contentAsString(result)

        getElementByCss(page, "table>tbody>tr>th:nth-child(1)").text() must be("1234")
        getElementByCss(page, "table>tbody>tr>th:nth-child(2)> button").text() must be(messages(removeCaption))
      }
    }
  }

  "Item Type Controller on POST" should {

    "display the form page with error" when {

      "Combined Nomenclature Code is empty" in {

        val form =
          buildItemTypeUrlEncodedInput(SaveAndContinue)(statisticalValue = "100", descriptionOfGoods = "Description")
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cncErrorEmpty, "#combinedNomenclatureCode")

        getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(messages(cncErrorEmpty))
      }

      "Combined Nomenclature Code is longer than 8 characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "123456789",
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cncErrorLength, "#combinedNomenclatureCode")

        getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(messages(cncErrorLength))
      }

      "Combined Nomenclature Code contains special characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "123$%^",
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cncErrorSpecialCharacters, "#combinedNomenclatureCode")

        getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(
          messages(cncErrorSpecialCharacters)
        )
      }

      "TARIC additional code is not 4 characters long" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("123"),
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, taricErrorLength, "#taricAdditionalCode_")

        getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(messages(taricErrorLength))
      }

      "TARIC additional code contains special characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1$%^"),
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, taricErrorSpecialCharacters, "#taricAdditionalCode_")

        getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(
          messages(taricErrorSpecialCharacters)
        )
      }

      "National additional code is longer than 4 characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1234"),
          nationalAdditionalCodes = Seq("12345"),
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, nacErrorLength, "#nationalAdditionalCode_")

        getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(messages(nacErrorLength))
      }

      "National additional code contains special characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1234"),
          nationalAdditionalCodes = Seq("1$%^"),
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, nacErrorSpecialCharacters, "#nationalAdditionalCode_")

        getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(
          messages(nacErrorSpecialCharacters)
        )
      }

      "Description of goods is empty" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1234"),
          nationalAdditionalCodes = Seq("1234"),
          statisticalValue = "100"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, descriptionErrorEmpty, "#descriptionOfGoods")

        getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(messages(descriptionErrorEmpty))
      }

      "Description of goods is longer than 280 characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1234"),
          nationalAdditionalCodes = Seq("1234"),
          statisticalValue = "100",
          descriptionOfGoods = TestHelper.createRandomString(281)
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, descriptionErrorLength, "#descriptionOfGoods")

        getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(
          messages(descriptionErrorLength)
        )
      }

      "CUS code is not 8 characters long" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1234"),
          nationalAdditionalCodes = Seq("1234"),
          cusCode = "12345",
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cusCodeErrorLength, "#cusCode")

        getElementByCss(page, "#error-message-cusCode-input").text() must be(messages(cusCodeErrorLength))
      }

      "CUS code contains special characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          taricAdditionalCodes = Seq("1234"),
          nationalAdditionalCodes = Seq("1234"),
          cusCode = "1234@#$%",
          statisticalValue = "100",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cusCodeErrorSpecialCharacters, "#cusCode")

        getElementByCss(page, "#error-message-cusCode-input").text() must be(messages(cusCodeErrorSpecialCharacters))
      }

      "Statistical value is empty" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, statisticalErrorEmpty, "#statisticalValue")

        getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorEmpty))
      }

      "Statistical value contains more than 15 digits" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          statisticalValue = "12345678901234.56",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, statisticalErrorLength, "#statisticalValue")

        getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorLength))
      }

      "Statistical value contains non-digit characters" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "12345",
          statisticalValue = "123456Q.78",
          descriptionOfGoods = "Description"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, statisticalErrorWrongFormat, "#statisticalValue")

        getElementByCss(page, "#error-message-statisticalValue-input").text() must be(
          messages(statisticalErrorWrongFormat)
        )
      }

      "user press \"Save and continue\" without entering anything" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)()
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cncErrorEmpty, "#combinedNomenclatureCode")
        checkErrorLink(page, 2, descriptionErrorEmpty, "#descriptionOfGoods")
        checkErrorLink(page, 3, statisticalErrorEmpty, "#statisticalValue")

        getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(messages(cncErrorEmpty))
        getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(messages(descriptionErrorEmpty))
        getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorEmpty))
      }

      "user put incorrect/empty values into all fields" in {

        val form = buildItemTypeUrlEncodedInput(SaveAndContinue)(
          combinedNomenclatureCode = "123456789",
          taricAdditionalCodes = Seq("12345"),
          nationalAdditionalCodes = Seq("12345"),
          cusCode = "%^&%6789"
        )
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, cncErrorLength, "#combinedNomenclatureCode")
        checkErrorLink(page, 2, taricErrorLength, "#taricAdditionalCode_")
        checkErrorLink(page, 3, nacErrorLength, "#nationalAdditionalCode_")
        checkErrorLink(page, 4, descriptionErrorEmpty, "#descriptionOfGoods")
        checkErrorLink(page, 5, cusCodeErrorSpecialCharacters, "#cusCode")
        checkErrorLink(page, 6, statisticalErrorEmpty, "#statisticalValue")

        getElementByCss(page, "#error-message-combinedNomenclatureCode-input").text() must be(messages(cncErrorLength))
        getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(messages(taricErrorLength))
        getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(messages(nacErrorLength))
        getElementByCss(page, "#error-message-descriptionOfGoods-input").text() must be(messages(descriptionErrorEmpty))
        getElementByCss(page, "#error-message-cusCode-input").text() must be(messages(cusCodeErrorSpecialCharacters))
        getElementByCss(page, "#error-message-statisticalValue-input").text() must be(messages(statisticalErrorEmpty))
      }

      "when user tries to add duplicated TARIC" in {
        withCaching[ItemType](
          Some(ItemType("100", fourDigitsSequence(98), Seq(), "Description", Some(""), "100")),
          ItemType.id
        )

        val form =
          buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", taricAdditionalCodes = Seq("9991"))
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, taricErrorDuplicate, "#taricAdditionalCode_")

        getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(messages(taricErrorDuplicate))
      }

      "when user tries to add more then 99 TARIC" in {

        withCaching[ItemType](
          Some(ItemType("100", fourDigitsSequence(99), Seq(), "Description", Some(""), "100")),
          ItemType.id
        )

        val form =
          buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", taricAdditionalCodes = Seq("1000"))
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, taricErrorMaxAmount, "#taricAdditionalCode_")

        getElementByCss(page, "#error-message-taricAdditionalCode_-input").text() must be(messages(taricErrorMaxAmount))
      }

      "when user tries to add duplicated NAC" in {

        withCaching[ItemType](
          Some(ItemType("100", Seq(), fourDigitsSequence(98), "Description", Some(""), "100")),
          ItemType.id
        )

        val form =
          buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", nationalAdditionalCodes = Seq("9991"))
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, nacErrorDuplicate, "#nationalAdditionalCode_")

        getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(
          messages(nacErrorDuplicate)
        )
      }

      "when user tries to add more then 99 NAC" in {

        withCaching[ItemType](
          Some(ItemType("100", Seq(), fourDigitsSequence(99), "Description", Some(""), "100")),
          ItemType.id
        )

        val form =
          buildItemTypeUrlEncodedInput(Add)(combinedNomenclatureCode = "100", nationalAdditionalCodes = Seq("1000"))
        val result = route(app, postRequestFormUrlEncoded(uri, form.toSeq: _*)).get
        val page = contentAsString(result)

        status(result) must be(BAD_REQUEST)

        checkErrorsSummary(page)
        checkErrorLink(page, 1, nacErrorMaxAmount, "#nationalAdditionalCode_")

        getElementByCss(page, "#error-message-nationalAdditionalCode_-input").text() must be(
          messages(nacErrorMaxAmount)
        )
      }
    }

    "save data to the cache" in {
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

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/package-information"))
    }

    "redirect to \"Add Package Information\" page with mandatory fields only" in {
      val result = route(app, postRequestFormUrlEncoded(uri, mandatoryItemTypeFormUrlEncoded.toSeq: _*)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/package-information"))
    }
  }
}

object ItemTypePageControllerSpec {

  def fourDigitsSequence(number: Int): Seq[String] = Seq.tabulate(number)(n => (9999 - n).toString)

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
  ): Map[String, String] =
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
