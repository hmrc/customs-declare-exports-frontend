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
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import play.api.test.Helpers._
import utils.FakeRequestCSRFSupport._

class AdditionalFiscalReferencesControllerSpec extends CustomExportsBaseSpec {

  private val uri = uriWithContextPath("/declaration/additional-fiscal-references")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  override def beforeEach() {
    authorizedUser()
    withCaching[AdditionalFiscalReferencesData](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Additional Fiscal References Controller on GET" should {

    "return 200 status code" in {
      val Some(result) = route(app, getRequest(uri))

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("France", "7232")))
      withCaching[AdditionalFiscalReferencesData](Some(cachedData), AdditionalFiscalReferencesData.formId)

      val Some(result) = route(app, getRequest(uri))
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("France")
      page must include("7232")
    }
  }

  "Additional Fiscal References Controller on POST" should {

    "remove item from the cache" in {
      val cachedData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("FR", "7232")))
      withCaching[AdditionalFiscalReferencesData](Some(cachedData), AdditionalFiscalReferencesData.formId)

      val body = (Remove.toString, "0")
      val Some(result) = route(app, postRequestFormUrlEncoded(uri, body))

      status(result) must be(SEE_OTHER)
    }
    "return bad request" when {

      "form contains errors during adding item" in {
        val body = Seq(("country", "hello"), ("reference", "12345"), addActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*).withCSRFToken

        val Some(result) = route(app, request)

        status(result) must be(BAD_REQUEST)
      }

      "form contains errors during saving" in {
        val body = Seq(("country", "hello"), ("reference", "12345"), saveAndContinueActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*).withCSRFToken

        val Some(result) = route(app, request)

        status(result) must be(BAD_REQUEST)
      }
    }

    "return see other" when {

      "user adds correct item" in {
        val body = Seq(("country", "FR"), ("reference", "12345"), addActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*).withCSRFToken

        val Some(result) = route(app, request)

        status(result) must be(SEE_OTHER)

      }

      "user clicks save with empty form and item in the cache" in {
        val cachedData = AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference("FR", "7232")))
        withCaching[AdditionalFiscalReferencesData](Some(cachedData), AdditionalFiscalReferencesData.formId)

        val body = Seq(saveAndContinueActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*).withCSRFToken

        val Some(result) = route(app, request)

        status(result) must be(SEE_OTHER)

      }

      "user clicks save with form filled" in {
        val body = Seq(("country", "FR"), ("reference", "12345"), saveAndContinueActionUrlEncoded)
        val request =
          postRequestFormUrlEncoded(uri, body: _*).withCSRFToken

        val Some(result) = route(app, request)

        status(result) must be(SEE_OTHER)

      }
    }
  }
}
