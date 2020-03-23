/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.common.Eori
import forms.declaration.DeclarationHolder
import helpers.views.declaration.CommonMessages
import models.{DeclarationType, Mode}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declaration_holder
import views.tags.ViewTest
import DeclarationType._

@ViewTest
class DeclarationHolderViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationHolder] = DeclarationHolder.form()
  private val declarationHolderPage = new declaration_holder(mainTemplate)
  private def createView(form: Form[DeclarationHolder] = form)(implicit request: JourneyRequest[_]): Document =
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
    onEveryDeclarationJourney { implicit request =>
      val document = createView()

      "display page title" in {

        document.getElementById("title").text() mustBe messages("supplementary.declarationHolder.title")
      }

      "display section header" in {

        document.getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
      }

      "display empty input with label for Authorisation Code" in {

        document.getElementById("authorisationTypeCode-label").text() mustBe messages("supplementary.declarationHolder.authorisationCode")
        document.getElementById("authorisationTypeCode-hint").text() mustBe messages("supplementary.declarationHolder.authorisationCode.hint")
        document.getElementById("authorisationTypeCode").attr("value") mustBe empty
      }

      "display empty input with label for EORI" in {

        document.getElementById("eori-label").text() mustBe messages("supplementary.declarationHolder.eori")
        document.getElementById("eori").attr("value") mustBe empty
      }

      "display both 'Add' and 'Save and continue' button on page" in {
        val addButton = document.getElementById("add")
        addButton.text() mustBe messages("site.add supplementary.declarationHolders.add.hint")

        val saveAndContinueButton = document.getElementById("submit")
        saveAndContinueButton.text() mustBe messages(saveAndContinueCaption)

        val saveAndReturnButton = document.getElementById("submit_and_return")
        saveAndReturnButton.text() mustBe messages(saveAndReturnCaption)
        saveAndReturnButton.attr("name") mustBe SaveAndReturn.toString
      }
    }

    onJourney(STANDARD, SIMPLIFIED, SUPPLEMENTARY, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Additional Info' page" in {

        val document = createView()
        val backButton = document.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarationAdditionalActorsController.displayPage().url
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Representative Details' page" in {

        val document = createView()
        val backButton = document.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.RepresentativeDetailsController.displayPage().url
      }
    }
  }

  "Declaration Holder View for invalid input" should {
    onEveryDeclarationJourney { implicit request =>
      /*
       * Both add and save button returns the same errors, so
       * no point to distinguish them and move to controller test
       */
      "display error for incorrect Authorisation code" in {

        val view = createView(
          DeclarationHolder
            .form()
            .fillAndValidate(DeclarationHolder(Some("12345"), Some(Eori(TestHelper.createRandomAlphanumericString(17)))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("authorisationTypeCode", "#authorisationTypeCode")

        view.select("#error-message-authorisationTypeCode-input").text() mustBe messages("supplementary.declarationHolder.authorisationCode.invalid")
      }

      "display error for incorrect EORI" in {

        val view = createView(
          DeclarationHolder
            .form()
            .fillAndValidate(DeclarationHolder(Some("ACE"), Some(Eori(TestHelper.createRandomAlphanumericString(18)))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("eori", "#eori")

        view.select("#error-message-eori-input").text() mustBe messages("supplementary.eori.error.format")
      }

      "display error for both incorrect fields" in {

        val view = createView(
          DeclarationHolder
            .form()
            .fillAndValidate(
              DeclarationHolder(Some(TestHelper.createRandomAlphanumericString(6)), Some(Eori(TestHelper.createRandomAlphanumericString(18))))
            )
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("authorisationTypeCode", "#authorisationTypeCode")
        view must haveFieldErrorLink("eori", "#eori")

        view.select("#error-message-authorisationTypeCode-input").text() mustBe messages("supplementary.declarationHolder.authorisationCode.invalid")
        view.select("#error-message-eori-input").text() mustBe messages("supplementary.eori.error.format")
      }
    }
  }

  "Declaration Holder View when filled" should {
    onEveryDeclarationJourney { implicit request =>
      "display data in Authorisation Code input" in {

        val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), None)))

        view.getElementById("authorisationTypeCode").attr("value") mustBe "test"
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display data in EORI input" in {

        val view = createView(DeclarationHolder.form().fill(DeclarationHolder(None, Some(Eori("test")))))

        view.getElementById("authorisationTypeCode").attr("value") mustBe empty
        view.getElementById("eori").attr("value") mustBe "test"
      }

      "display data in both inputs" in {

        val view = createView(DeclarationHolder.form().fill(DeclarationHolder(Some("test"), Some(Eori("test1")))))

        view.getElementById("authorisationTypeCode").attr("value") mustBe "test"
        view.getElementById("eori").attr("value") mustBe "test1"
      }

      "display one row with data in table" in {

        val view =
          declarationHolderPage(Mode.Normal, form, Seq(DeclarationHolder(Some("1234"), Some(Eori("1234")))))(request, messages)

        view.getElementById("removable_elements-row0-label").text() mustBe "1234-1234"

        val removeButton = view.getElementsByClass("remove button--secondary")

        removeButton.text() mustBe "site.remove site.remove.hint"
        removeButton.attr("value") mustBe "1234-1234"
      }
    }
  }
}
