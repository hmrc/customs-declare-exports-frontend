/*
 * Copyright 2022 HM Revenue & Customs
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

import base.ExportsTestData._
import base.Injector
import base.TestHelper.createRandomAlphanumericString
import controllers.helpers.SaveAndReturn
import forms.common.Eori
import forms.common.YesNoAnswer.{No, Yes}
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import models.declaration.EoriSource.{OtherEori, UserEori}
import models.declaration.{DeclarationHoldersData, Parties}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.declarationHolder.declaration_holder_add
import views.tags.ViewTest

@ViewTest
class DeclarationHolderAddViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val declarationHolderPage = instanceOf[declaration_holder_add]

  private def createForm(implicit request: JourneyRequest[_]): Form[DeclarationHolder] =
    DeclarationHolder.form(eori, request.cacheModel.additionalDeclarationType)

  private def createView(form: Form[DeclarationHolder])(implicit request: JourneyRequest[_]): Document =
    declarationHolderPage(Mode.Normal, form, eori)(request, messages)

  "Declaration Holder View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView(createForm)

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.declarationHolder.title")
      }

      "display section header" in {
        view.getElementById("section-header").text must include(messages("declaration.section.2"))
      }

      "display empty input with label for Authorisation Code" in {
        view.getElementById("authorisationTypeCode-label").text mustBe messages("declaration.declarationHolder.authorisationCode")
        view.getElementById("authorisationTypeCode").attr("value") mustBe empty
      }

      "display unselected radio buttons with labels" in {
        view
          .getElementById("UserEori")
          .getElementsByAttribute("checked")
          .size mustBe 0

        view.getElementsByAttributeValue("for", "UserEori").text mustBe messages("declaration.declarationHolder.eori.user.text", eori)

        view
          .getElementById("OtherEori")
          .getElementsByAttribute("checked")
          .size mustBe 0

        view.getElementsByAttributeValue("for", "OtherEori").text mustBe messages("declaration.declarationHolder.eori.other.text")
      }

      "do not display the Eori text form field" in {
        view.getElementById("conditional-OtherEori").attr("class").contains("hidden") mustBe true
      }

      "display empty input with label and hint for EORI" in {
        view.getElementsByAttributeValue("for", "eori").text mustBe messages("declaration.declarationHolder.eori.other.label")
        view.getElementById("eori-hint").text mustBe messages("declaration.declarationHolder.eori.other.hint")
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display 'Save and continue' button on page" in {
        val saveAndContinueButton = view.getElementById("submit")
        saveAndContinueButton.text mustBe messages(saveAndContinueCaption)

        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton.text mustBe messages(saveAndReturnCaption)
        saveAndReturnButton.attr("name") mustBe SaveAndReturn.toString
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { implicit request =>
      Seq(Yes, No).foreach { answer =>
        s"cache contains DeclarationHoldersData.isRequired field with '${answer.get.answer}' answer" should {
          "display back link to Declaration Holder Required page" in {
            val parties = Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq.empty, answer)))
            val req = journeyRequest(request.cacheModel.copy(parties = parties))

            val view = createView(createForm)(req)
            view must containElementWithID("back-link")
            view.getElementById("back-link") must haveHref(
              controllers.declaration.routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
            )
          }
        }

      }

      "cache does NOT contain DeclarationHoldersData.isRequired field" should {
        "display back link to Authorisation Procedure Code Choice page" in {
          val view = createView(createForm)
          view must containElementWithID("back-link")
          view.getElementById("back-link") must haveHref(
            controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
          )
        }
      }
    }

    onJourney(OCCASIONAL) { implicit request =>
      "display back link to Other Parties page" in {
        val view = createView(createForm)
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(Mode.Normal))
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "EIDR is true" must {
        "display back link to Authorisation Choice page" in {
          val parties = Parties(isEntryIntoDeclarantsRecords = Yes)
          val req = journeyRequest(request.cacheModel.copy(parties = parties))

          val view = createView(createForm)(req)
          view must containElementWithID("back-link")
          view.getElementById("back-link") must haveHref(
            controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
          )
        }
      }

      "EIDR is false" must {
        "display back link to Consignee Details page" in {
          val parties = Parties(isEntryIntoDeclarantsRecords = No)
          val req = journeyRequest(request.cacheModel.copy(parties = parties))

          val view = createView(createForm)(req)
          view must containElementWithID("back-link")
          view.getElementById("back-link") must haveHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage(Mode.Normal))
        }
      }
    }
  }

  "Declaration Holder View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      /*
       * Both add and save button returns the same errors, so
       * no point to distinguish them and move to controller test
       */
      "display error for empty Authorisation code" in {
        val eori = Eori(createRandomAlphanumericString(17))
        val declarationHolder = DeclarationHolder(None, Some(eori), Some(OtherEori))
        val view = createView(createForm.fillAndValidate(declarationHolder))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#authorisationTypeCode")

        view must containErrorElementWithMessageKey("declaration.declarationHolder.authorisationCode.empty")
      }

      "display error for incorrect EORI" in {
        val eori = Eori(createRandomAlphanumericString(18))
        val declarationHolder = DeclarationHolder(Some("ACE"), Some(eori), Some(OtherEori))
        val view = createView(createForm.fillAndValidate(declarationHolder))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#eori")

        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }

      "display error for both incorrect fields" in {
        val eori = Eori(createRandomAlphanumericString(18))
        val declarationHolder = DeclarationHolder(None, Some(eori), Some(OtherEori))
        val view = createView(createForm.fillAndValidate(declarationHolder))

        view must haveGovukGlobalErrorSummary

        view must containErrorElementWithTagAndHref("a", "#authorisationTypeCode")
        view must containErrorElementWithTagAndHref("a", "#eori")

        view must containErrorElementWithMessageKey("declaration.declarationHolder.authorisationCode.empty")
        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }
    }
  }

  "Declaration Holder View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in Authorisation Code input only" in {
        val declarationHolder = DeclarationHolder(Some("test"), None, Some(OtherEori))
        val view = createView(createForm.fill(declarationHolder))

        view.getElementById("authorisationTypeCode").attr("value") mustBe "test"
        view.getElementById("eori").attr("value") mustBe empty
      }

      "display UserEori checked" in {
        val declarationHolder = DeclarationHolder(Some("test"), Some(Eori("test1")), Some(UserEori))
        val view = createView(createForm.fill(declarationHolder))

        view.getElementById("conditional-OtherEori").attr("class").contains("hidden") mustBe true
        view.getElementById("UserEori").getElementsByAttribute("checked").size mustBe 1
      }

      "display OtherEori checked and data is present in EORI input only" in {
        val declarationHolder = DeclarationHolder(None, Some(Eori("test")), Some(OtherEori))
        val view = createView(createForm.fill(declarationHolder))

        view.getElementById("authorisationTypeCode").attr("value") mustBe empty
        view.getElementById("conditional-OtherEori").attr("class").contains("hidden") mustBe false
        view.getElementById("OtherEori").getElementsByAttribute("checked").size mustBe 1
        view.getElementById("eori").attr("value") mustBe "test"
      }

      "display data in both inputs" in {
        val declarationHolder = DeclarationHolder(Some("test"), Some(Eori("test1")), Some(OtherEori))
        val view = createView(createForm.fill(declarationHolder))

        view.getElementById("authorisationTypeCode").attr("value") mustBe "test"
        view.getElementById("conditional-OtherEori").attr("class").contains("hidden") mustBe false
        view.getElementById("OtherEori").getElementsByAttribute("checked").size mustBe 1
        view.getElementById("eori").attr("value") mustBe "test1"
      }
    }
  }
}
