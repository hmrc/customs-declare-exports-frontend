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
import forms.declaration.NatureOfTransaction
import forms.declaration.NatureOfTransactionSpec._
import helpers.views.declaration.NatureOfTransactionMessages
import play.api.test.Helpers._

class NatureOfTransactionControllerSpec extends CustomExportsBaseSpec with NatureOfTransactionMessages {

  private val uri = uriWithContextPath("/declaration/nature-of-transaction")

  override def beforeEach() {
    authorizedUser()
    withNewCaching(createModel())
    withCaching[NatureOfTransaction](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Nature Of Transaction Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = NatureOfTransaction("1")
      withCaching[NatureOfTransaction](Some(cachedData), NatureOfTransaction.formId)

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("Purchase")
    }
  }

  "Nature Of Transaction Controller on POST" should {

    "validate request and redirect - empty value" in {

      val result = route(app, postRequest(uri, emptyNatureOfTransactionJSON)).get

      status(result) must be(BAD_REQUEST)
      contentAsString(result) must include(messages(natureOfTransactionEmpty))
    }

    "validate request and redirect - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectNatureOfTransactionJSON)).get
      val stringResult = contentAsString(result)

      status(result) must be(BAD_REQUEST)

      stringResult must include(messages(natureOfTransactionError))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctNatureOfTransactionJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/previous-documents"))
    }
  }

}
