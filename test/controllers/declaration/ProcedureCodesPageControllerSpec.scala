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

import base.{CustomExportsBaseSpec, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import helpers.views.declaration.{CommonMessages, ProcedureCodesMessages}
import models.declaration.ProcedureCodesData
import models.declaration.ProcedureCodesData.formId
import play.api.test.Helpers._

class ProcedureCodesPageControllerSpec
    extends CustomExportsBaseSpec with ViewValidator with ProcedureCodesMessages with CommonMessages {
  import ProcedureCodesPageControllerSpec._

  private val uri = uriWithContextPath("/declaration/procedure-codes")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[ProcedureCodesData](None, formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Procedure Codes Controller on GET" should {

    "return 200 status code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = ProcedureCodesData(Some("1234"), Seq("123", "234"))
      withCaching[ProcedureCodesData](Some(cachedData), formId)

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)

      getElementByCss(page, "table>tbody>tr>th:nth-child(1)").text() must be("123")
      getElementByCss(page, "table>tbody>tr>th:nth-child(2)>button").text() must be(messages(removeCaption))

      getElementByCss(page, "table>tbody>tr:nth-child(2)>th:nth-child(1)").text() must be("234")
      getElementByCss(page, "table>tbody>tr:nth-child(2)>th:nth-child(2)>button").text() must be(
        messages(removeCaption)
      )
    }
  }

  "Procedure Codes Controller on POST" can {

    "display the form page with error" when {

      "adding the data" should {

        "without any code" in {

          val body = Seq(addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorEmpty, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorEmpty)
          )
        }

        "with shorter procedure code" in {

          val body = Seq(("procedureCode", "123"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorLength, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(messages(procCodeErrorLength))
        }

        "with longer procedure code" in {

          val body = Seq(("procedureCode", "12345"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorLength, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(messages(procCodeErrorLength))
        }

        "with special characters in procedure code" in {

          val body = Seq(("procedureCode", "1@£4"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorSpecialCharacters, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(
            messages(procCodeErrorSpecialCharacters)
          )
        }

        "without additional procedure code" in {

          val body = Seq(("procedureCode", "1234"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorEmpty, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorEmpty)
          )
        }

        "with shorter additional procedure code" in {

          val body = Seq(("additionalProcedureCode", "12"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorLength, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorLength)
          )
        }

        "with too long additional procedure code" in {

          val body = Seq(("additionalProcedureCode", "2002"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorLength, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorLength)
          )
        }

        "with special characters in additional code" in {

          val body = Seq(("additionalProcedureCode", "2@2"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorSpecialCharacters, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorSpecialCharacters)
          )
        }

        "with duplicated additional procedure code" in {

          val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
          withCaching[ProcedureCodesData](Some(cachedData), formId)

          val body = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorDuplication, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorDuplication)
          )
        }

        "when more than 99 additional codes already exists" in {

          withCaching[ProcedureCodesData](Some(cacheWithMaximumAmountOfAdditionalCodes), formId)

          val body = Seq(("additionalProcedureCode", "200"), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorMaxAmount, "#")
        }
      }

      "saving the data" should {

        "without any code" in {

          val body = Seq(saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorEmpty, "#procedureCode")
          checkErrorLink(page, 2, addProcCodeErrorMandatory, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(messages(procCodeErrorEmpty))
          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorMandatory)
          )
        }

        "without procedure code" in {

          val body = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorEmpty, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(messages(procCodeErrorEmpty))
        }

        "with shorter procedure code" in {

          val body = Seq(("procedureCode", "123"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorLength, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(messages(procCodeErrorLength))
        }

        "with longer procedure code" in {

          val body = Seq(("procedureCode", "12345"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorLength, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(messages(procCodeErrorLength))
        }

        "with special characters in procedure code" in {

          val body = Seq(("procedureCode", "1@£4"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, procCodeErrorSpecialCharacters, "#procedureCode")

          getElementByCss(page, "#error-message-procedureCode-input").text() must be(
            messages(procCodeErrorSpecialCharacters)
          )
        }

        "without additional procedure code" in {

          val body = Seq(("procedureCode", "1234"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorMandatory, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorMandatory)
          )
        }

        "with shorter additional procedure code" in {

          val body = Seq(("additionalProcedureCode", "12"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorLength, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorLength)
          )
        }

        "with too long additional procedure code" in {

          val body = Seq(("additionalProcedureCode", "1234"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorLength, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorLength)
          )
        }

        "with special characters in additional procedure code" in {

          val body = Seq(("additionalProcedureCode", "1@4"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorSpecialCharacters, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorSpecialCharacters)
          )
        }

        "with duplicated additional procedure code" in {

          val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
          withCaching[ProcedureCodesData](Some(cachedData), formId)

          val body = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorDuplication, "#additionalProcedureCode")

          getElementByCss(page, "#error-message-additionalProcedureCode-input").text() must be(
            messages(addProcCodeErrorDuplication)
          )
        }

        "when more than 99 additional procedure codes already exists" in {

          withCaching[ProcedureCodesData](Some(cacheWithMaximumAmountOfAdditionalCodes), formId)
          val body = Seq(("additionalProcedureCode", "200"), saveAndContinueActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, addProcCodeErrorMaxAmount, "#")
        }
      }

      "try to remove not added additional code" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)

        val body = removeActionUrlEncoded("124")
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages(globalErrorTitle))
        stringResult must include(messages(globalErrorHeading))
        stringResult must include(messages(globalErrorMessage))
      }
    }

    "add code without errors" when {

      "user provides additional code when no codes in cache" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq())
        withCaching[ProcedureCodesData](Some(cachedData), formId)

        val body = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "user provides additional code that does not exist in cache " in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("100"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)

        val body = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove code" when {

      "code exists in cache" in {

        val cachedData = ProcedureCodesData(Some("1234"), Seq("123", "234", "235"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)

        val body = removeActionUrlEncoded("123")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to next page" when {

      "user fill both inputs with empty cache" in {

        val body = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/fiscal-information"))
      }

      "user fill only procedure code with some additional codes already added" in {

        val cachedData = ProcedureCodesData(None, Seq("123"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)
        val body = Seq(("procedureCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/fiscal-information"))
      }
    }
  }
}

object ProcedureCodesPageControllerSpec {
  val cacheWithMaximumAmountOfAdditionalCodes =
    ProcedureCodesData(Some("1234"), Seq.range[Int](100, 200, 1).map(_.toString))
}
