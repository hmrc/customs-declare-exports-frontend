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
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationHolder.declaration_holder_add
import views.tags.ViewTest

@ViewTest
class DeclarationHolderAddViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val form: Form[DeclarationHolder] = DeclarationHolder.form()
  private val declarationHolderPage = instanceOf[declaration_holder_add]
  private def createView(form: Form[DeclarationHolder] = form)(implicit request: JourneyRequest[_]): Document =
    declarationHolderPage(Mode.Normal, form)(request, messages)

  "Declaration holder" should {

    "have correct message keys" in {

      messages must haveTranslationFor("declaration.declarationHolder.title")
      messages must haveTranslationFor("declaration.declarationHolder.title.hint")
      messages must haveTranslationFor("declaration.declarationHolder.eori")
      messages must haveTranslationFor("declaration.declarationHolder.authorisationCode")
      messages must haveTranslationFor("declaration.declarationHolder.authorisationCode.hint")
      messages must haveTranslationFor("declaration.declarationHolder.authorisationCode.invalid")
      messages must haveTranslationFor("declaration.declarationHolder.authorisationCode.empty")
      messages must haveTranslationFor("declaration.declarationHolders.maximumAmount.error")
      messages must haveTranslationFor("declaration.declarationHolders.duplicated")
    }
  }

  "Declaration Holder View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val document = createView()

      "display page title" in {

        document.getElementsByTag("h1").text() mustBe messages("declaration.declarationHolder.title")
      }

      "display section header" in {

        document.getElementById("section-header").text() must include(messages("declaration.section.2"))
      }

      "display empty input with label for Authorisation Code" in {

        document.getElementById("authorisationTypeCode-label").text() mustBe messages("declaration.declarationHolder.authorisationCode")
        document.getElementById("authorisationTypeCode-hint").text() mustBe messages("declaration.declarationHolder.authorisationCode.hint")
        document.getElementById("authorisationTypeCode").attr("value") mustBe empty
      }

      "display empty input with label for EORI" in {

        document.getElementsByAttributeValue("for", "eori").text() mustBe messages("declaration.declarationHolder.eori")
        document.getElementById("eori").attr("value") mustBe empty
      }

      "display 'Save and continue' button on page" in {

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
        backButton.attr("href") mustBe routes.AdditionalActorsSummaryController.displayPage().url
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Consignee Details' page" in {

        val document = createView()
        val backButton = document.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.ConsigneeDetailsController.displayPage().url
      }
    }
  }

  "Declaration Holder View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
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

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#authorisationTypeCode")

        view must containErrorElementWithMessageKey("declaration.declarationHolder.authorisationCode.invalid")
      }

      "display error for incorrect EORI" in {

        val view = createView(
          DeclarationHolder
            .form()
            .fillAndValidate(DeclarationHolder(Some("ACE"), Some(Eori(TestHelper.createRandomAlphanumericString(18)))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#eori")

        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }

      "display error for both incorrect fields" in {

        val view = createView(
          DeclarationHolder
            .form()
            .fillAndValidate(
              DeclarationHolder(Some(TestHelper.createRandomAlphanumericString(6)), Some(Eori(TestHelper.createRandomAlphanumericString(18))))
            )
        )

        view must haveGovukGlobalErrorSummary

        view must containErrorElementWithTagAndHref("a", "#authorisationTypeCode")
        view must containErrorElementWithTagAndHref("a", "#eori")

        view must containErrorElementWithMessageKey("declaration.declarationHolder.authorisationCode.invalid")
        view must containErrorElementWithMessageKey("declaration.eori.error.format")

      }
    }
  }

  "Declaration Holder View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
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

    }
  }
}
