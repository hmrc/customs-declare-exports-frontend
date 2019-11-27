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

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.DeclarationHolder
import helpers.views.declaration.{CommonMessages, DeclarationHolderMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declaration_holder
import views.tags.ViewTest

@ViewTest
class DeclarationHolderViewSpec extends UnitViewSpec with DeclarationHolderMessages with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationHolder] = DeclarationHolder.form()
  private val declarationHolderPage = new declaration_holder(mainTemplate)
  private def createView(form: Form[DeclarationHolder] = form): Document =
    declarationHolderPage(Mode.Normal, form, Seq())(request, messages)

  "Declaration holder" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.declarationHolder.title")
      messages must haveTranslationFor("supplementary.declarationHolder.title.hint")
      messages must haveTranslationFor("supplementary.declarationHolder.eori")
      messages must haveTranslationFor("supplementary.declarationHolder.authorisationCode")
      messages must haveTranslationFor("supplementary.declarationHolder.authorisationCode.hint")
      messages must haveTranslationFor("supplementary.declarationHolder.authorisationCode.invalid")
      messages must haveTranslationFor("supplementary.declarationHolder.authorisationCode.empty")
      messages must haveTranslationFor("supplementary.declarationHolders.maximumAmount.error")
      messages must haveTranslationFor("supplementary.declarationHolders.duplicated")
    }
  }

  "Declaration Holder View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(title)
    }

    "display section header" in {

      createView().getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
    }

    "display empty input with label for Authorisation Code" in {

      val view = createView()

      view.getElementById("authorisationTypeCode-label").text() mustBe messages(authorisationCode)
      view.getElementById("authorisationTypeCode-hint").text() mustBe messages(authorisationCodeHint)
      view.getElementById("authorisationTypeCode").attr("value") mustBe empty
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("eori-label").text() mustBe messages(declarationHolderEori)
      view.getElementById("eori").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Additional Information' page" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.DeclarationAdditionalActorsController.displayPage().url
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val view = createView()

      val addButton = view.getElementById("add")
      addButton.text() mustBe messages("site.add supplementary.declarationHolders.add.hint")

      val saveAndContinueButton = view.getElementById("submit")
      saveAndContinueButton.text() mustBe messages(saveAndContinueCaption)

      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() mustBe messages(saveAndReturnCaption)
      saveAndReturnButton.attr("name") mustBe SaveAndReturn.toString
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

      view.select("#error-message-authorisationTypeCode-input").text() mustBe messages(authorisationCodeError)
    }

    "display error for incorrect EORI" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(DeclarationHolder(Some("ACE"), Some(TestHelper.createRandomAlphanumericString(18))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("eori", "#eori")

      view.select("#error-message-eori-input").text() mustBe messages(eoriError)
    }

    "display error for both incorrect fields" in {

      val view = createView(
        DeclarationHolder
          .form()
          .fillAndValidate(DeclarationHolder(Some(TestHelper.createRandomAlphanumericString(6)), Some(TestHelper.createRandomAlphanumericString(18))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("authorisationTypeCode", "#authorisationTypeCode")
      view must haveFieldErrorLink("eori", "#eori")

      view.select("#error-message-authorisationTypeCode-input").text() mustBe messages(authorisationCodeError)
      view.select("#error-message-eori-input").text() mustBe messages(eoriError)
    }
  }

  "Declaration Holder View when filled" should {

    "display data in Authorisation Code input" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), None)))

      view.getElementById("authorisationTypeCode").attr("value") mustBe "test"
      view.getElementById("eori").attr("value") mustBe empty
    }

    "display data in EORI input" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(None, Some("test"))))

      view.getElementById("authorisationTypeCode").attr("value") mustBe empty
      view.getElementById("eori").attr("value") mustBe "test"
    }

    "display data in both inputs" in {

      val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), Some("test1"))))

      view.getElementById("authorisationTypeCode").attr("value") mustBe "test"
      view.getElementById("eori").attr("value") mustBe "test1"
    }

    "display one row with data in table" in {

      val view =
        declarationHolderPage(Mode.Normal, form, Seq(DeclarationHolder(Some("1234"), Some("1234"))))(request, messages)

      view.select("tbody>tr>th:nth-child(1)").text() mustBe "1234-1234"

      val removeButton = view.select("tbody>tr>th:nth-child(2)>button")

      removeButton.text() mustBe "site.remove site.remove.hint"
      removeButton.attr("value") mustBe "1234-1234"
    }
  }
}
