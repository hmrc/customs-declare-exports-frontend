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
import forms.declaration.DeclarationHolder
import helpers.views.declaration.{CommonMessages, DeclarationHolderMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.declaration.declaration_holder
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class DeclarationHolderViewSpec extends ViewSpec with DeclarationHolderMessages with CommonMessages {

  private val form: Form[DeclarationHolder] = DeclarationHolder.form()
  private def createView(form: Form[DeclarationHolder] = form): Html =
    declaration_holder(appConfig, form, Seq())(fakeRequest, messages)

  "Declaration Holder View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Declaration holder of authorisation")
      assertMessage(authorisationCode, "3/39 Enter the authorisation code")
      assertMessage(authorisationCodeHint, "This is a 4 digit code")
    }

    "have proper messages for error labels" in {

      assertMessage(authorisationCodeEmpty, "Authorisation code cannot be empty")
      assertMessage(authorisationCodeError, "Authorisation code is incorrect")
      assertMessage(maximumAmountReached, "You cannot have more than 99 holders")
      assertMessage(duplicatedItem, "You cannot add the same holder")
    }
  }

  "Declaration Holder View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Authorisation Code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(messages(authorisationCode))
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(
        messages(authorisationCodeHint)
      )
      getElementById(view, "authorisationTypeCode").attr("value") must be("")
    }

    "display empty input with label for EORI" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(eori))
      getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(eoriHint))
      getElementById(view, "eori").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Additional Information\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/additional-actors")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be(messages(addCaption))

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Declaration Holder View for invalid input" should {

    /*
     * Both add and save button returns the same kind of errors, so
     * no point to distinguish them and move to controller test
     */
    "display error for incorrect Authorisation code" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(DeclarationHolder(Some("12345"), Some(TestHelper.createRandomString(17))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, authorisationCodeError, "#authorisationTypeCode")

      getElementByCss(view, "#error-message-authorisationTypeCode-input").text() must be(
        messages(authorisationCodeError)
      )
    }

    "display error for incorrect EORI" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(DeclarationHolder(Some("1234"), Some(TestHelper.createRandomString(18))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eoriError, "#eori")

      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eoriError))
    }

    "display error for both incorrect fields" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(
            DeclarationHolder(Some(TestHelper.createRandomString(6)), Some(TestHelper.createRandomString(18)))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, authorisationCodeError, "#authorisationTypeCode")
      checkErrorLink(view, 2, eoriError, "#eori")

      getElementByCss(view, "#error-message-authorisationTypeCode-input").text() must be(
        messages(authorisationCodeError)
      )
      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eoriError))
    }
  }

  "Declaration Holder View when filled" should {

    "display data in Authorisation Code input" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), None)))

      getElementById(view, "authorisationTypeCode").attr("value") must be("test")
      getElementById(view, "eori").attr("value") must be("")
    }

    "display data in EORI input" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(None, Some("test"))))

      getElementById(view, "authorisationTypeCode").attr("value") must be("")
      getElementById(view, "eori").attr("value") must be("test")
    }

    "display data in both inputs" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), Some("test1"))))

      getElementById(view, "authorisationTypeCode").attr("value") must be("test")
      getElementById(view, "eori").attr("value") must be("test1")
    }

    "display one item in table" in {

      val view =
        declaration_holder(appConfig, form, Seq(DeclarationHolder(Some("1234"), Some("1234"))))(fakeRequest, messages)

      getElementByCss(view, "tbody>tr>th:nth-child(1)").text() must be("1234-1234")

      val removeButton = getElementByCss(view, "tbody>tr>th:nth-child(2)>button")

      removeButton.text() must be(messages(removeCaption))
      removeButton.attr("value") must be("1234-1234")
    }
  }
}
