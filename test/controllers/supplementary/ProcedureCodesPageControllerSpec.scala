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
import forms.supplementary.ProcedureCodes
import forms.supplementary.ProcedureCodesSpec._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import play.api.test.Helpers._

class ProcedureCodesPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {
  import ProcedureCodesPageControllerSpec._

  private val uri = uriWithContextPath("/declaration/supplementary/procedure-codes")

  before {
    authorizedUser()
  }

  "ProcedureCodesPageController on displayPage" should {
    "display the whole content" in {
      withCaching[ProcedureCodes](None, ProcedureCodes.id)
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("supplementary.procedureCodes.title"))
      contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.header"))
      contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.header.hint"))
      contentAsString(result) must include(messages("supplementary.procedureCodes.additionalProcedureCode.header"))
      contentAsString(result) must include(messages("supplementary.procedureCodes.additionalProcedureCode.header.hint"))
    }

    "display \"back\" button that links to location-of-goods page" in {
      withCaching[ProcedureCodes](None, ProcedureCodes.id)
      val result = route(app, getRequest(uri)).get

      contentAsString(result) must include(messages("site.back"))
      contentAsString(result) must include("/declaration/supplementary/location-of-goods")
    }
  }

  "ProcedureCodesPageController on submitProcedureCodes" should {

    "display the form page with error" when {
      "no value provided for procedure code" in {
        withCaching[ProcedureCodes](None, ProcedureCodes.id)
        val result = route(app, postRequest(uri, emptyProcedureCodesJSON)).get

        contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.error.empty"))
      }

      "procedure code is longer than 4 characters" in {
        withCaching[ProcedureCodes](None, ProcedureCodes.id)
        val form = buildProcedureCodes(procedureCode = "12345")
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(messages("supplementary.procedureCodes.procedureCode.error.length"))
      }

      "procedure code contains special characters" in {
        withCaching[ProcedureCodes](None, ProcedureCodes.id)
        val form = buildProcedureCodes(procedureCode = "123$")
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(
          messages("supplementary.procedureCodes.procedureCode.error.specialCharacters")
        )
      }

      "no value provided for additional procedure codes" in {
        withCaching[ProcedureCodes](None, ProcedureCodes.id)
        val emptyForm = buildProcedureCodes()
        val result = route(app, postRequest(uri, emptyForm)).get

        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.singleEmpty")
        )
      }

      "any additional procedure code is longer than 3 characters" in {
        withCaching[ProcedureCodes](None, ProcedureCodes.id)
        val form = buildProcedureCodes(additionalProcedureCodes = Seq("1234"))
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.length")
        )
      }

      "any additional procedure code contains special characters" in {
        withCaching[ProcedureCodes](None, ProcedureCodes.id)
        val form = buildProcedureCodes(additionalProcedureCodes = Seq("12#"))
        val result = route(app, postRequest(uri, form)).get

        contentAsString(result) must include(
          messages("supplementary.procedureCodes.additionalProcedureCode.error.specialCharacters")
        )
      }
    }

    "save the data to the cache" in {
      reset(mockCustomsCacheService)
      withCaching[ProcedureCodes](None, ProcedureCodes.id)
      route(app, postRequest(uri, correctProcedureCodesJSON)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[ProcedureCodes](any(), ArgumentMatchers.eq(ProcedureCodes.id), any())(any(), any(), any())
    }

    "return 303 code" in {
      withCaching[ProcedureCodes](None, ProcedureCodes.id)
      val result = route(app, postRequest(uri, correctProcedureCodesJSON)).get

      status(result) must be(SEE_OTHER)
    }

    "redirect to \"Supervising Office\" page" in {
      withCaching[ProcedureCodes](None, ProcedureCodes.id)
      val result = route(app, postRequest(uri, correctProcedureCodesJSON)).get
      val header = result.futureValue.header

      header.headers.get("Location") must be(
        Some("/customs-declare-exports/declaration/supplementary/supervising-office")
      )
    }
  }

}

object ProcedureCodesPageControllerSpec {

  def buildProcedureCodes(
    procedureCode: String = "",
    additionalProcedureCodes: Seq[String] = Seq.empty[String]
  ): JsValue = JsObject(
    Map(
      "procedureCode" -> JsString(procedureCode),
      "additionalProcedureCodes" -> JsArray(additionalProcedureCodes.map(JsString))
    )
  )

}
