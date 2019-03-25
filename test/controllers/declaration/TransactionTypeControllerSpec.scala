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
import org.scalatest.BeforeAndAfter
import play.api.test.Helpers._

class TransactionTypeControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val uri = uriWithContextPath("/declaration/transaction-type")

  before {
    authorizedUser()
    withCaching[TransactionType](None)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)

  }

  "Transaction Type Controller on display page" should {

    "display transaction type form" in {

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("supplementary.transactionType.documentTypeCode.header"))
      stringResult must include(messages("supplementary.transactionType.documentTypeCode"))
      stringResult must include(messages("supplementary.transactionType.hint"))
      stringResult must include(messages("supplementary.transactionType.identifier"))
    }

    "display \"Back\" button that links to \"Total Number of Items\" page" in {

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include(messages("site.back"))
      stringResult must include(messages("/declaration/total-numbers-of-items"))
    }

    "display \"Save and continue\" button on page" in {

      val result = route(app, getRequest(uri)).get
      val resultAsString = contentAsString(result)

      resultAsString must include(messages("site.save_and_continue"))
      resultAsString must include("button id=\"submit\" class=\"button\"")
    }

    "validate form - empty value" in {

      val result = route(app, postRequest(uri, emptyTransactionTypeJSON)).get

      contentAsString(result) must include(messages("supplementary.transactionType.documentTypeCode.empty"))
    }

    "validate form - incorrect values" in {

      val result = route(app, postRequest(uri, incorrectTransactionTypeJSON)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.transactionType.documentTypeCode.error"))
      stringResult must include(messages("supplementary.transactionType.identifier.error"))
    }

    "validate form - correct values" in {

      val result = route(app, postRequest(uri, correctTransactionTypeJSON)).get
      val header = result.futureValue.header

      status(result) must be(SEE_OTHER)
      header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/previous-documents"))
    }
  }

}
