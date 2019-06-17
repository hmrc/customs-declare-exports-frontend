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

import base.CustomExportsBaseSpec
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.FiscalInformation
import helpers.views.declaration.FiscalInformationMessages
import org.mockito.Mockito.reset
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers.{OK, route, status, _}

class FiscalInformationControllerSpec extends CustomExportsBaseSpec with FiscalInformationMessages {

  private val uri: String = uriWithContextPath("/declaration/fiscal-information")
  private val emptyFiscalInformationJson: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("")))
  private val incorrectFiscalInformation: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("NeitherRadioOption")))
  private val fiscalInformationWithYes: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("Yes")))
  private val fiscalInformationWithNo: JsValue = JsObject(Map("onwardSupplyRelief" -> JsString("No")))

  trait SetUp {
    authorizedUser()
    withCaching[FiscalInformation](None, FiscalInformation.formId)
  }

  trait SupplementarySetUp extends SetUp {
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  trait StandardSetUp extends SetUp {
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.StandardDec)), choiceId)
  }

  after {
    reset(mockCustomsCacheService)
  }

  "Fiscal information Controller on the supplementary journey" when {
    "on GET request" should {

      "return 200 on GET request with a success" in new SupplementarySetUp {

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "read item from cache and display it" in new SupplementarySetUp {

        val cachedData = FiscalInformation("Yes")
        withCaching[FiscalInformation](Some(cachedData), FiscalInformation.formId)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
        contentAsString(result) must include("Yes")
      }
    }

    "on POST request" should {

      "return bad request for empty form" in new SupplementarySetUp {

        val result = route(app, postRequest(uri, emptyFiscalInformationJson)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(errorMessageEmpty))
      }
    }

    "return bad request for incorrect values" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, incorrectFiscalInformation)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(errorMessageIncorrect))

    }

    "redirect to 'AdditionalFiscalReferences' page when choice is yes" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, fiscalInformationWithYes)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/additional-fiscal-references"))
    }

    "redirect to 'ItemsSummary' page when choice is no" in new SupplementarySetUp {

      val result = route(app, postRequest(uri, fiscalInformationWithNo)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/export-items"))
    }
  }

  "Fiscal information Controller on the standard journey" when {
    "on GET request" should {

      "return 200 on GET request with a success" in new StandardSetUp {

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "read item from cache and display it" in new StandardSetUp {

        val cachedData = FiscalInformation("Yes")
        withCaching[FiscalInformation](Some(cachedData), FiscalInformation.formId)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
        contentAsString(result) must include("Yes")
      }
    }

    "on POST request" should {

      "return bad request for empty form" in new StandardSetUp {

        val result = route(app, postRequest(uri, emptyFiscalInformationJson)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages(errorMessageEmpty))
      }
    }

    "return bad request for incorrect values" in new StandardSetUp {

      val result = route(app, postRequest(uri, incorrectFiscalInformation)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(errorMessageIncorrect))

    }

    "redirect to 'AdditionalFiscalReferences' page when choice is yes" in new StandardSetUp {

      val result = route(app, postRequest(uri, fiscalInformationWithYes)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/additional-fiscal-references"))
    }

    "redirect to 'ItemsSummary' page when choice is no" in new StandardSetUp {

      val result = route(app, postRequest(uri, fiscalInformationWithNo)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/export-items"))
    }
  }
}
