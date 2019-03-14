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

package views.supplementary

import forms.supplementary.DeclarationHolder
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.declaration_holder
import views.tags.ViewTest

@ViewTest
class DeclarationHolderViewSpec extends ViewSpec {

  private val form: Form[DeclarationHolder] = DeclarationHolder.form()

  private val prefix = s"${basePrefix}declarationHolder."
  private val prefixWithS = s"${basePrefix}declarationHolders."

  private val title = Item(prefix, "title")
  private val authorisationCode = Item(prefix, "authorisationCode")
  private val maximumAmount = Item(prefixWithS, "maximumAmount")
  private val duplicated = Item(prefixWithS, "duplicated")
  private val eori = Item(basePrefix, "eori")

  private def createView(form: Form[DeclarationHolder] = form): Html =
    declaration_holder(appConfig, form, Seq())(fakeRequest, messages)

  "Declaration Holder View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "Declaration holder of authorisation")
      assertMessage(authorisationCode.withPrefix, "3/39 Enter the authorisation code")
      assertMessage(authorisationCode.withHint, "This is a 4 digit code")
      assertMessage(eori.withPrefix, "EORI number")
      assertMessage(eori.withHint, "Enter the EORI number or business details")
    }

    "have proper messages for error labels" in {

      assertMessage(authorisationCode.withEmpty, "Authorisation code cannot be empty")
      assertMessage(authorisationCode.withError, "Authorisation code is incorrect")
      assertMessage(eori.withEmpty, "EORI number cannot be empty")
      assertMessage(eori.withError, "EORI number is incorrect")
      assertMessage(maximumAmount.withError, "You cannot have more than 99 holders")
      assertMessage(duplicated.withPrefix, "You cannot add the same holder")
    }
  }

  "Declaration Holder View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title.withPrefix))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title.withPrefix))
    }

    "display empty input with label for Authorisation Code" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(3)>label>span:nth-child(1)").text() must be(
        messages(authorisationCode.withPrefix)
      )
      getElementByCss(view, "form>div:nth-child(3)>label>span.form-hint").text() must be(
        messages(authorisationCode.withHint)
      )
      getElementById(view, "authorisationTypeCode").attr("value") must be("")
    }

    "display empty input with label for EORI" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(4)>label>span:nth-child(1)").text() must be(messages(eori.withPrefix))
      getElementByCss(view, "form>div:nth-child(4)>label>span.form-hint").text() must be(messages(eori.withHint))
      getElementById(view, eori.key).attr("value") must be("")
    }

    "display \"Back\" button that links to \"Additional Information\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/additional-actors")
    }

    "display both \"Add\" and \"Save and continue\" button on page" in {

      val view = createView()

      val addButton = getElementByCss(view, "#add")
      addButton.text() must be("Add")

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Declaration Holder View for invalid input" should {

    "display error for empty Authorisation code" in {

      val view =
        createView(DeclarationHolder.form().withError("authorisationTypeCode", messages(authorisationCode.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, authorisationCode.withEmpty, "#authorisationTypeCode")

      getElementByCss(view, "#error-message-authorisationTypeCode-input").text() must be(
        messages(authorisationCode.withEmpty)
      )
    }

    "display error for empty EORI" in {

      val view = createView(DeclarationHolder.form().withError(eori.key, messages(eori.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eori.withEmpty, eori.asLink)

      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eori.withEmpty))
    }

    "display error for both empty fields" in {

      val view = createView(
        DeclarationHolder
          .form()
          .withError("authorisationTypeCode", messages(authorisationCode.withEmpty))
          .withError(eori.key, messages(eori.withEmpty))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, authorisationCode.withEmpty, "#authorisationTypeCode")
      checkErrorLink(view, 2, eori.withEmpty, eori.asLink)

      getElementByCss(view, "#error-message-authorisationTypeCode-input").text() must be(
        messages(authorisationCode.withEmpty)
      )
      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eori.withEmpty))
    }

    "display error for incorrect Authorisation code" in {

      val view = createView(
        DeclarationHolder
          .form()
          .withError("authorisationTypeCode", messages(authorisationCode.withError))
          .withError(eori.key, messages(eori.withError))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, authorisationCode.withError, "#authorisationTypeCode")

      getElementByCss(view, "#error-message-authorisationTypeCode-input").text() must be(
        messages(authorisationCode.withError)
      )
    }

    "display error for incorrect EORI" in {

      val view = createView(DeclarationHolder.form().withError(eori.key, messages(eori.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eori.withError, eori.asLink)

      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eori.withError))
    }

    "display error for both incorrect fields" in {

      val view = createView(
        DeclarationHolder
          .form()
          .withError("authorisationTypeCode", messages(authorisationCode.withError))
          .withError(eori.key, messages(eori.withError))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, authorisationCode.withError, "#authorisationTypeCode")
      checkErrorLink(view, 2, eori.withError, eori.asLink)

      getElementByCss(view, "#error-message-authorisationTypeCode-input").text() must be(
        messages(authorisationCode.withError)
      )
      getElementByCss(view, "#error-message-eori-input").text() must be(messages(eori.withError))
    }

    /*
     * global errors does not have any link to field, as they are not
     * per field - that's why they have # as default id
     */
    "display error for duplicated holder" in {

      val view = createView(DeclarationHolder.form().withError("", messages(duplicated.withPrefix)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, duplicated.withPrefix, "#")
    }

    "display error for more then 99 holders" in {

      val view = createView(DeclarationHolder.form().withError("", messages(maximumAmount.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, maximumAmount.withError, "#")
    }
  }

  "Declaration Holder View when filled" should {

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

      removeButton.text() must be("Remove")
      removeButton.attr("value") must be("1234-1234")
    }
  }
}
