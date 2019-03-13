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

import base.CustomExportsBaseSpec
import controllers.util.{Add, Remove, SaveAndContinue}
import models.declaration.supplementary.ProcedureCodesData
import models.declaration.supplementary.ProcedureCodesData.formId
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class ProcedureCodesPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  import ProcedureCodesPageControllerSpec._

  private val uri = uriWithContextPath("/declaration/supplementary/procedure-codes")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
  }

  "Procedure Codes Page Controller on display page" should {

    "display form without additional codes" in {
      withCaching[ProcedureCodesData](None, formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.procedureCodes.title"))
      stringResult must include(messages("supplementary.procedureCodes.procedureCode.header"))
      stringResult must include(messages("supplementary.procedureCodes.procedureCode.header.hint"))
      stringResult must include(messages("supplementary.procedureCodes.additionalProcedureCode.header"))
      stringResult must include(messages("supplementary.procedureCodes.additionalProcedureCode.header.hint"))
    }

    "display table with additional codes" in {
      val cachedData = ProcedureCodesData(Some("1234"), Seq("123", "234", "235"))
      withCaching[ProcedureCodesData](Some(cachedData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("name=\"Remove\"")
      stringResult must include("value=\"123\"")
      stringResult must include("value=\"234\"")
      stringResult must include("value=\"235\"")
      stringResult must include(messages("site.remove"))
    }

    "display \"Back\" button that links to \"Good item number\" page" in {
      withCaching[ProcedureCodesData](None, formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include("/declaration/supplementary/good-item-number")
    }

    "display \"Save and continue\" button on page" in {
      withCaching[ProcedureCodesData](None)

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "display \"Add\" button on page" in {
      withCaching[ProcedureCodesData](None)

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.add"))
      resultAsString must include("button id=\"add\" class=\"button\"")
    }
  }

  "Procedure Codes Page Controller on submit procedure codes" should {

    "display the form page with error" when {

      "try to save the data without procedure code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.error.empty"))
      }

      "try to save the data without additional procedure code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("procedureCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.mandatory.error")
        )
      }

      "try to save the data with shorter procedure code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("procedureCode", "123"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.error.length"))
      }

      "try to save the data with longer procedure code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("procedureCode", "12345"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.error.length"))
      }

      "try to save the data with special characters in procedure code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("procedureCode", "1@£4"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.procedureCode.error.specialCharacters")
        )
      }

      "try to save the data with shorter additional code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("additionalProcedureCode", "12"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.length")
        )
      }

      "try to save the data with longer additional code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("additionalProcedureCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.length")
        )
      }

      "try to save the data with special characters in additional procedure code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("additionalProcedureCode", "1@4"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters")
        )
      }

      "try to save more than 99 codes" in {
        withCaching[ProcedureCodesData](Some(cacheWithMaximumAmountOfAdditionalCodes), formId)
        val body = Seq(("additionalProcedureCode", "200"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")
        )
      }

      "try to add empty additional code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("supplementary.procedureCodes.additionalProcedureCode.empty"))
      }

      "try to add too long additional code" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("additionalProcedureCode", "2002"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.length")
        )
      }

      "try to add additional code with special characters" in {
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("additionalProcedureCode", "2@2"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters")
        )
      }

      "try to add duplicated additional code" in {
        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)
        val body = Seq(("additionalProcedureCode", "123"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.duplication")
        )
      }

      "try to add more than 99 codes" in {
        withCaching[ProcedureCodesData](Some(cacheWithMaximumAmountOfAdditionalCodes), formId)
        val body = Seq(("additionalProcedureCode", "200"), addActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.maximumAmount.error")
        )
      }

      "try to remove not added additional code" in {
        val cachedData = ProcedureCodesData(Some("1234"), Seq("123"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)
        val body = removeActionUrlEncoded("124")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.title"))
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
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
        withCaching[ProcedureCodesData](None, formId)
        val body = Seq(("procedureCode", "1234"), ("additionalProcedureCode", "123"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/supervising-office")
        )
      }

      "user fill only procedure code with some additional codes already added" in {
        val cachedData = ProcedureCodesData(None, Seq("123"))
        withCaching[ProcedureCodesData](Some(cachedData), formId)
        val body = Seq(("procedureCode", "1234"), saveAndContinueActionUrlEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(
          Some("/customs-declare-exports/declaration/supplementary/supervising-office")
        )
      }
    }
  }
}

object ProcedureCodesPageControllerSpec {
  val cacheWithMaximumAmountOfAdditionalCodes =
    ProcedureCodesData(Some("1234"), Seq.range[Int](100, 200, 1).map(_.toString))
}
