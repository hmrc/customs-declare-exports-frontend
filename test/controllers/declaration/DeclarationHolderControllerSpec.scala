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

import base.TestHelper.createRandomString
import base.{CustomExportsBaseSpec, ViewValidator}
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.DeclarationHolder
import helpers.views.declaration.{CommonMessages, DeclarationHolderMessages}
import models.declaration.DeclarationHoldersData
import models.declaration.DeclarationHoldersData.formId
import play.api.test.Helpers._

class DeclarationHolderControllerSpec
    extends CustomExportsBaseSpec with DeclarationHolderMessages with CommonMessages with ViewValidator {
  import DeclarationHolderControllerSpec._

  private val uri = uriWithContextPath("/declaration/holder-of-authorisation")
  private val addActionUrlEncoded = (Add.toString, "")
  private val saveAndContinueActionUrlEncoded = (SaveAndContinue.toString, "")
  private def removeActionUrlEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[DeclarationHoldersData](None, formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Declaration Holder Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("8899"), Some("0099887766"))))
      withCaching[DeclarationHoldersData](Some(cachedData), "DeclarationHoldersData")

      val result = route(app, getRequest(uri)).get
      val page = contentAsString(result)

      status(result) must be(OK)
      page must include("8899")
      page must include("0099887766")
    }
  }

  "Declaration Holder Controller on POST" should {

    "validate request and show error" when {

      "adding holder" when {

        "without EORI number" in {

          val body = Seq(("authorisationTypeCode", "1234"), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, eoriEmpty, "#eori")

          getElementByCss(page, "#error-message-eori-input").text() must be(messages(eoriEmpty))
        }

        "with longer EORI" in {
          val body = Seq(("eori", createRandomString(18)), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
        }

        "with EORI with special characters" in {

          val body = Seq(("authorisationTypeCode", "1234"), ("eori", "e@#$1"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
        }

        "without Authorisation code" in {

          val body = Seq(("eori", "eori1"), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, authorisationCodeEmpty, "#authorisationTypeCode")

          getElementByCss(page, "#error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
        }

        "with longer Authorisation code" in {

          val body = Seq(("authorisationTypeCode", "12345"), ("eori", "eori1"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(authorisationCodeError))
        }

        "with Authorisation code with special characters" in {

          val body = Seq(("authorisationTypeCode", "1$#4"), ("eori", "eori1"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(authorisationCodeError))
        }

        "with both inputs empty" in {

          val body = addActionUrlEncoded

          val result = route(app, postRequestFormUrlEncoded(uri, body)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, authorisationCodeEmpty, "#authorisationTypeCode")
          checkErrorLink(page, 2, eoriEmpty, "#eori")

          getElementByCss(page, "#error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
          getElementByCss(page, "#error-message-eori-input").text() must be(messages(eoriEmpty))
        }

        "with duplicated holder" in {

          val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
          withCaching[DeclarationHoldersData](Some(cachedData), formId)
          val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplicatedItem, "#")
        }

        "with more than 99 holders" in {

          withCaching[DeclarationHoldersData](Some(cacheWithMaximumAmountOfHolders), formId)
          val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), addActionUrlEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, maximumAmountReached, "#")
        }
      }

      "saving holder" when {

        "without EORI number" in {

          withCaching[DeclarationHoldersData](None, formId)

          val body = Seq(("authorisationTypeCode", "1234"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, eoriEmpty, "#eori")

          getElementByCss(page, "#error-message-eori-input").text() must be(messages(eoriEmpty))
        }

        "with longer EORI" in {

          withCaching[DeclarationHoldersData](None, formId)

          val body = Seq(("eori", createRandomString(18)), addActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
        }

        "with EORI with special characters" in {

          withCaching[DeclarationHoldersData](None, formId)

          val body = Seq(("authorisationTypeCode", "1234"), ("eori", "e@#$1"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(eoriError))
        }

        "without Authorisation code" in {

          withCaching[DeclarationHoldersData](None, formId)

          val body = Seq(("eori", "eori1"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, authorisationCodeEmpty, "#authorisationTypeCode")

          getElementByCss(page, "#error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
        }

        "with longer Authorisation code" in {

          withCaching[DeclarationHoldersData](None, formId)

          val body = Seq(("authorisationTypeCode", "12345"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(authorisationCodeError))
        }

        "with Authorisation code with special characters" in {

          withCaching[DeclarationHoldersData](None, formId)

          val body = Seq(("authorisationTypeCode", "1$#4"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

          status(result) must be(BAD_REQUEST)
          contentAsString(result) must include(messages(authorisationCodeError))
        }

        "with both input empty" in {

          val result = route(app, postRequestFormUrlEncoded(uri, saveAndContinueActionUrlEncoded)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, authorisationCodeEmpty, "#authorisationTypeCode")
          checkErrorLink(page, 2, eoriEmpty, "#eori")

          getElementByCss(page, "#error-message-authorisationTypeCode-input").text() must be(
            messages(authorisationCodeEmpty)
          )
          getElementByCss(page, "#error-message-eori-input").text() must be(messages(eoriEmpty))
        }

        "with duplicated holder" in {

          val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
          withCaching[DeclarationHoldersData](Some(cachedData), formId)

          val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplicatedItem, "#")
        }

        "with more than 99 holders" in {

          withCaching[DeclarationHoldersData](Some(cacheWithMaximumAmountOfHolders), formId)

          val body = Seq(("authorisationTypeCode", "9999"), ("eori", "eori9"), saveAndContinueActionUrlEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, maximumAmountReached, "#")
        }
      }

      "try to remove not added Additional code" in {

        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)

        val body = removeActionUrlEncoded("4321-eori")
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages(globalErrorTitle))
        stringResult must include(messages(globalErrorHeading))
        stringResult must include(messages(globalErrorMessage))
      }
    }

    "add holder without error" when {

      "user provide holder with empty cache" in {

        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), addActionUrlEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "user provide holder that not exists in cache" in {

        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("4321"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)

        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), addActionUrlEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove holder" when {

      "holder exists in cache" in {
        val cachedData = DeclarationHoldersData(
          Seq(DeclarationHolder(Some("4321"), Some("eori")), DeclarationHolder(Some("4321"), Some("eori")))
        )
        withCaching[DeclarationHoldersData](Some(cachedData), formId)

        val body = removeActionUrlEncoded("4321-eori")
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "redirect to the next page" when {

      "user provide holder with empty cache" in {

        val body = Seq(("authorisationTypeCode", "1234"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/destination-countries"))
      }

      "user doesn't fill form but some holder exists inside the cache" in {

        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)

        val body = saveAndContinueActionUrlEncoded
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/destination-countries"))
      }

      "user provide holder with some different holder in cache" in {

        val cachedData = DeclarationHoldersData(Seq(DeclarationHolder(Some("1234"), Some("eori"))))
        withCaching[DeclarationHoldersData](Some(cachedData), formId)

        val body = Seq(("authorisationTypeCode", "4321"), ("eori", "eori1"), saveAndContinueActionUrlEncoded)
        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/destination-countries"))
      }
    }
  }
}

object DeclarationHolderControllerSpec {
  val cacheWithMaximumAmountOfHolders =
    DeclarationHoldersData(
      Seq
        .range[Int](100, 200, 1)
        .map(elem => DeclarationHolder(Some(elem.toString), Some(elem.toString)))
    )
}
