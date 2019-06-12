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
import forms.declaration.TransactionType
import forms.declaration.TransactionTypeSpec._
import helpers.views.declaration.TransactionTypeMessages
import play.api.test.Helpers._

class TransactionTypeControllerSpec extends CustomExportsBaseSpec with TransactionTypeMessages {

  private val uri = uriWithContextPath("/declaration/transaction-type")

  before {
    authorizedUser()
    withCaching[TransactionType](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Transaction Type Controller on GET" should {

    "return 200 code" in {

      val result = route(app, getRequest(uri)).get

      status(result) mustBe OK
    }

    "read item from cache and display it" in {

      val cachedData = TransactionType("AAA9", Some("FancyShoes"))
      withCaching[TransactionType](Some(cachedData), "TransactionType")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) mustBe OK
      page must include("AAA9")
      page must include("FancyShoes")
    }
  }

  "Transaction Type Controller on POST" should {

    "validate request and redirect - empty value" in {

      val result = route(app, postRequest(uri, emptyTransactionTypeJSON)).get

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(messages(documentTypeCodeEmpty))
    }

    "validate request and redirect - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectTransactionTypeJSON)).get
      val stringResult = contentAsString(result)

      status(result) mustBe BAD_REQUEST

      stringResult must include(messages(documentTypeCodeError))
      stringResult must include(messages(identifierError))
    }

    "validate request and redirect - correct values" in {

      val result = route(app, postRequest(uri, correctTransactionTypeJSON)).get
      val header = result.futureValue.header

      status(result) mustBe SEE_OTHER

      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/previous-documents"))
    }
  }

}
