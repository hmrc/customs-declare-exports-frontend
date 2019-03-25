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
import controllers.declaration.AdditionalInformationControllerSpec.cacheWithMaximumAmountOfHolders
import controllers.util.{Add, Remove, SaveAndContinue}
import forms.Choice
import forms.Choice.choiceId
import forms.declaration.AdditionalInformation
import helpers.views.declaration.{AdditionalInformationMessages, CommonMessages}
import models.declaration.AdditionalInformationData
import models.declaration.AdditionalInformationData.formId
import play.api.test.Helpers._

class AdditionalInformationControllerSpec
    extends CustomExportsBaseSpec with AdditionalInformationMessages with CommonMessages with ViewValidator {

  private val uri: String = uriWithContextPath("/declaration/additional-information")

  private val addActionURLEncoded = (Add.toString, "")
  private val saveAndContinueActionURLEncoded = (SaveAndContinue.toString, "")
  private def removeActionURLEncoded(value: String) = (Remove.toString, value)

  before {
    authorizedUser()
    withCaching[AdditionalInformationData](None, formId)
    withCaching[Choice](Some(Choice(Choice.AllowedChoiceValues.SupplementaryDec)), choiceId)
  }

  "Additional Information Controller on GET" should {

    "return 200 status code" in {
      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "load item from cache and display it" in {

      val cachedData = AdditionalInformationData(
        Seq(AdditionalInformation("M1l3s", "Davis"), AdditionalInformation("X4rlz", "Mingus"))
      )
      withCaching[AdditionalInformationData](Some(cachedData), formId)

      val result = route(app, getRequest(uri)).get
      val stringResult = contentAsString(result)

      status(result) must be(OK)
      stringResult must include("M1l3s-Davis")
      stringResult must include("X4rlz-Mingus")
    }
  }

  "Additional Information Controller on POST" should {

    "add an item successfully" when {

      "cache is empty" in {

        withCaching[AdditionalInformationData](None, formId)
        val body = Seq(("code", "J0ohn"), ("description", "Coltrane"), addActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }

      "it does not exist in cache" in {

        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))

        withCaching[AdditionalInformationData](Some(cachedData), formId)
        val body = Seq(("code", "x4rlz"), ("description", "Mingusss"), addActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "remove an item successfully by id" when {

      "it already exists in cache" in {

        val cachedData = AdditionalInformationData(
          Seq(AdditionalInformation("M1l3s", "Davis"), AdditionalInformation("J00hn", "Coltrane"))
        )
        withCaching[AdditionalInformationData](Some(cachedData), formId)

        val body = removeActionURLEncoded("0")
        val result = route(app, postRequestFormUrlEncoded(uri, body)).get

        status(result) must be(SEE_OTHER)
      }
    }

    "display the form page with an error" when {

      "adding" should {

        "an item without a code" in {

          val body = Seq(("code", ""), ("description", "Davis"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeEmpty, "#code")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeEmpty))
        }

        "an item without a description" in {

          val body = Seq(("code", "M1l3s"), ("description", ""), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, descriptionEmpty, "#description")

          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionEmpty))
        }

        "an item without any data" in {

          withCaching[AdditionalInformationData](None, formId)

          val body = Seq(("code", ""), ("description", ""), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeEmpty, "#code")
          checkErrorLink(page, 2, descriptionEmpty, "#description")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeEmpty))
          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionEmpty))
        }

        "an item with both fields incorrect" in {

          withCaching[AdditionalInformationData](None, formId)

          val body = Seq(("code", "1234"), ("description", createRandomString(71)), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeError, "#code")
          checkErrorLink(page, 2, descriptionError, "#description")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeError))
          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionError))
        }

        "an item with longer code" in {

          val body = Seq(("code", createRandomString(6)), ("description", ""), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeError, "#code")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeError))
        }

        "an item with shorter code" in {

          val body = Seq(("code", createRandomString(3)), ("description", ""), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeError, "#code")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeError))
        }

        "an item with longer description" in {

          val body = Seq(("code", "M1l3s"), ("description", createRandomString(100)), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, descriptionError, "#description")

          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionError))
        }

        "a duplicated item" in {

          val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))
          withCaching[AdditionalInformationData](Some(cachedData), formId)

          val body = Seq(("code", "M1l3s"), ("description", "Davis"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplication, "#")
        }

        "more than 99 items" in {

          withCaching[AdditionalInformationData](Some(cacheWithMaximumAmountOfHolders), formId)

          val body = Seq(("code", "M1l3s"), ("description", "Davis"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, limit, "#")
        }
      }

      "saving and continue" when {

        "without a code" in {

          val body = Seq(("code", ""), ("description", "Davis"), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeEmpty, "#code")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeEmpty))
        }

        "without a description" in {

          val body = Seq(("code", "123rt"), ("description", ""), addActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, descriptionEmpty, "#description")

          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionEmpty))
        }

        "both fields are incorrect" in {

          withCaching[AdditionalInformationData](None, formId)

          val body = Seq(("code", "1234"), ("description", createRandomString(71)), addActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeError, "#code")
          checkErrorLink(page, 2, descriptionError, "#description")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeError))
          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionError))
        }

        "without any items defined" in {

          val body = Seq(("code", ""), ("description", ""), saveAndContinueActionURLEncoded)

          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, continueMandatory, "#")
        }

        "an item has longer code" in {

          val body = Seq(("code", createRandomString(6)), ("description", ""), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeError, "#code")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeError))
        }

        "an item has shorter code" in {

          val body = Seq(("code", createRandomString(3)), ("description", ""), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, codeError, "#code")

          getElementByCss(page, "#error-message-code-input").text() must be(messages(codeError))
        }

        "an item has longer description" in {

          val body = Seq(("code", "M1l3s"), ("description", createRandomString(100)), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, descriptionError, "#description")

          getElementByCss(page, "#error-message-description-input").text() must be(messages(descriptionError))
        }

        "a duplicated item is entered" in {

          val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))
          withCaching[AdditionalInformationData](Some(cachedData), formId)

          val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, duplication, "#")
        }

        "with more than 99 items" in {

          withCaching[AdditionalInformationData](Some(cacheWithMaximumAmountOfHolders), formId)

          val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionURLEncoded)
          val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
          val page = contentAsString(result)

          status(result) must be(BAD_REQUEST)

          checkErrorsSummary(page)
          checkErrorLink(page, 1, limit, "#")
        }
      }

      "trying to remove a non existent code" in {

        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("M1l3s", "Davis")))
        withCaching[AdditionalInformationData](Some(cachedData), formId)

        val body = ("action", "Remove:J0ohn-Coltrane")

        val result = route(app, postRequestFormUrlEncoded(uri, body)).get
        val stringResult = contentAsString(result)

        status(result) must be(BAD_REQUEST)
        stringResult must include(messages("global.error.title"))
        stringResult must include(messages("global.error.heading"))
        stringResult must include(messages("global.error.message"))
      }
    }

    "redirect to the next page" when {

      "user provide item with empty cache" in {

        val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/add-document"))
      }

      "user doesn't fill form but some items already exist in the cache" in {

        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("Jo0hn", "Coltrane")))
        withCaching[AdditionalInformationData](Some(cachedData), formId)

        val result = route(app, postRequestFormUrlEncoded(uri, saveAndContinueActionURLEncoded)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/add-document"))
      }

      "user provide holder with some different holder in cache" in {

        val cachedData = AdditionalInformationData(Seq(AdditionalInformation("x4rlz", "Mingus")))
        withCaching[AdditionalInformationData](Some(cachedData), formId)

        val body = Seq(("code", "M1l3s"), ("description", "Davis"), saveAndContinueActionURLEncoded)

        val result = route(app, postRequestFormUrlEncoded(uri, body: _*)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-declare-exports/declaration/add-document"))
      }
    }
  }
}

object AdditionalInformationControllerSpec {
  val cacheWithMaximumAmountOfHolders = AdditionalInformationData(
    Seq
      .range[Int](100, 200, 1)
      .map(elem => AdditionalInformation(elem.toString, elem.toString))
  )
}
