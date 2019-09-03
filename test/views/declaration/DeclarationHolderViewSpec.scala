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

package views.declaration

import base.TestHelper
import controllers.util.SaveAndReturn
import forms.declaration.DeclarationHolder
import helpers.views.declaration.{CommonMessages, DeclarationHolderMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.AppViewSpec
import views.html.declaration.declaration_holder
import views.tags.ViewTest

@ViewTest
class DeclarationHolderViewSpec extends AppViewSpec with DeclarationHolderMessages with CommonMessages {

  private val form: Form[DeclarationHolder] = DeclarationHolder.form()
  private val declarationHolderPage = app.injector.instanceOf[declaration_holder]
  private def createView(form: Form[DeclarationHolder] = form): Document =
    declarationHolderPage(Mode.Normal, form, Seq())(fakeRequest, messages)

  "Declaration Holder View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Parties")
    }

    "display empty input with label for Authorisation Code" in {

      val view = createView()

      view.getElementById("authorisationTypeCode-label").text() must be(messages(authorisationCode))
      view.getElementById("authorisationTypeCode-hint").text() must be(messages(authorisationCodeHint))
      view.getElementById("authorisationTypeCode").attr("value") must be("")
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("eori-label").text() must be(messages(declarationHolderEori))
      view.getElementById("eori-hint").text() must be(messages(eoriHint))
      view.getElementById("eori").attr("value") must be("")
    }

    "display 'Back' button that links to 'Additional Information' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/additional-actors")
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() must be(messages(addCaption))

      val saveAndContinueButton = view.getElementById("submit")
      saveAndContinueButton.text() must be(messages(saveAndContinueCaption))

      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be(messages(saveAndReturnCaption))
      saveAndReturnButton.attr("name") must be(SaveAndReturn.toString)
    }
  }

  "Declaration Holder View for invalid input" should {

    /*
     * Both add and save button returns the same errors, so
     * no point to distinguish them and move to controller test
     */
    "display error for incorrect Authorisation code" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(DeclarationHolder(Some("12345"), Some(TestHelper.createRandomAlphanumericString(17))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("authorisationTypeCode", "#authorisationTypeCode")

      view.select("#error-message-authorisationTypeCode-input").text() must be(messages(authorisationCodeError))
    }

    "display error for incorrect EORI" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(DeclarationHolder(Some("ACE"), Some(TestHelper.createRandomAlphanumericString(18))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("eori", "#eori")

      view.select("#error-message-eori-input").text() must be(messages(eoriError))
    }

    "display error for both incorrect fields" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(
            DeclarationHolder(
              Some(TestHelper.createRandomAlphanumericString(6)),
              Some(TestHelper.createRandomAlphanumericString(18))
            )
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("authorisationTypeCode", "#authorisationTypeCode")
      view must haveFieldErrorLink("eori", "#eori")

      view.select("#error-message-authorisationTypeCode-input").text() must be(messages(authorisationCodeError))
      view.select("#error-message-eori-input").text() must be(messages(eoriError))
    }
  }

  "Declaration Holder View when filled" should {

    "display data in Authorisation Code input" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), None)))

      view.getElementById("authorisationTypeCode").attr("value") must be("test")
      view.getElementById("eori").attr("value") must be("")
    }

    "display data in EORI input" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(None, Some("test"))))

      view.getElementById("authorisationTypeCode").attr("value") must be("")
      view.getElementById("eori").attr("value") must be("test")
    }

    "display data in both inputs" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), Some("test1"))))

      view.getElementById("authorisationTypeCode").attr("value") must be("test")
      view.getElementById("eori").attr("value") must be("test1")
    }

    "display one row with data in table" in {

      val view =
        declarationHolderPage(Mode.Normal, form, Seq(DeclarationHolder(Some("1234"), Some("1234"))))(
          fakeRequest,
          messages
        )

      view.select("tbody>tr>th:nth-child(1)").text() must be("1234-1234")

      val removeButton = view.select("tbody>tr>th:nth-child(2)>button")

      removeButton.text() must be(messages(removeCaption))
      removeButton.attr("value") must be("1234-1234")
    }
  }
}
