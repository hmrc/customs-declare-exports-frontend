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
import controllers.util.{Add, SaveAndContinue, SaveAndReturn}
import forms.common.Eori
import forms.declaration.DeclarationAdditionalActors
import helpers.views.declaration.CommonMessages
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declaration_additional_actors
import views.tags.ViewTest
import DeclarationType._

@ViewTest
class DeclarationAdditionalActorsViewSpec extends UnitViewSpec with CommonMessages with ExportsTestData with Stubs with Injector {

  private val form: Form[DeclarationAdditionalActors] = DeclarationAdditionalActors.form()
  private val declarationAdditionalActorsPage = instanceOf[declaration_additional_actors]

  private def createView(form: Form[DeclarationAdditionalActors], request: JourneyRequest[_]): Document =
    declarationAdditionalActorsPage(Mode.Normal, form, Seq())(request, messages)

  "Declaration Additional Actors" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(journeyRequest())

      messages must haveTranslationFor("declaration.additionalActors.title")
      messages must haveTranslationFor("declaration.additionalActors.title.hint")
      messages must haveTranslationFor("declaration.additionalActors.eori")
      messages must haveTranslationFor("declaration.eori.empty")
      messages must haveTranslationFor("declaration.additionalActors.partyType")
      messages must haveTranslationFor("declaration.partyType")
      messages must haveTranslationFor("declaration.partyType.CS")
      messages must haveTranslationFor("declaration.partyType.MF")
      messages must haveTranslationFor("declaration.partyType.FW")
      messages must haveTranslationFor("declaration.partyType.WH")
      messages must haveTranslationFor("declaration.partyType.empty")
      messages must haveTranslationFor("declaration.partyType.error")
    }
  }

  "Declaration Additional Actors View on empty page" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { request =>
      val view = createView(form, request)

      "display page title" in {

        view.getElementsByClass("govuk-fieldset__heading").first().text() mustBe messages("declaration.additionalActors.title")
      }

      "display section header" in {

        view.getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
      }

      "display five radio buttons with description (not selected)" in {

        val view = createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some(Eori("")), Some(""))), request)

        def checkOption(key: String, messagePrefix: String = "declaration.partyType.") = {
          val option = view.getElementById(key)
          option.attr("checked") mustBe empty
          val optionLabel = view.getElementsByAttributeValueMatching("for", key).first()
          optionLabel.text() mustBe messages(s"$messagePrefix$key")
        }

        checkOption("CS")
        checkOption("MF")
        checkOption("FW")
        checkOption("WH")
        checkOption("no", "site.")

      }

      "display both 'Add' and 'Save and continue' button on page" in {
        val addButton = view.getElementsByAttributeValueMatching("name", Add.toString).first()
        addButton.text() mustBe "site.addsupplementary.additionalActors.add.hint"

        val saveAndContinueButton = view.getElementsByAttributeValueMatching("name", SaveAndContinue.toString).first()
        saveAndContinueButton.text() mustBe messages(saveAndContinueCaption)

        val saveAndReturn = view.getElementsByAttributeValueMatching("name", SaveAndReturn.toString).first()
        saveAndReturn.text() mustBe messages(saveAndReturnCaption)
      }
    }

    onJourney(DeclarationType.SUPPLEMENTARY) { request =>
      "display 'Back' button that links to 'Representative Details' page" in {

        val view = declarationAdditionalActorsPage(Mode.Normal, form, Seq())(request, messages)

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.RepresentativeDetailsController.displayPage().url
      }
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { request =>
      "display 'Back' button that links to 'Carrier Details' page" in {

        val view = declarationAdditionalActorsPage(Mode.Normal, form, Seq())(request, messages)
        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.CarrierDetailsController.displayPage().url
      }
    }

  }

  "Declaration Additional Actors View with invalid input" must {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { request =>
      def incorrectEori(partyType: String) = {
        val view = createView(
          DeclarationAdditionalActors
            .form()
            .fillAndValidate(DeclarationAdditionalActors(Some(Eori(TestHelper.createRandomAlphanumericString(18))), Some(partyType))),
          request
        )

        view must haveGovukGlobalErrorSummary

        view must containErrorElementWithTagAndHref("a", s"#eori$partyType")
        view.getElementsByClass("govuk-error-message").text() contains messages("supplementary.eori.error.format")
      }

      "display errors when EORI is provided, but is incorrect" in {

        incorrectEori("CS")
        incorrectEori("MF")
        incorrectEori("FW")
        incorrectEori("WH")
      }

    }
  }

  "Declaration Additional Actors View when filled" must {

    def createViewAndFill(request: JourneyRequest[_], partyType: String) =
      createView(DeclarationAdditionalActors.form().fill(DeclarationAdditionalActors(Some(Eori("GB1234")), Some(partyType))), request)

    def ensureRadioIsChecked(view: Document, partyType: String): Unit =
      view.getElementById(partyType).getElementsByAttribute("checked").size() mustBe 1

    def ensureRadiosAreUnChecked(view: Document, partyTypes: String*): Unit =
      partyTypes.foreach { partyType =>
        view.getElementById(partyType).attr("checked") mustBe empty
      }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { request =>
      "display EORI with CS selected" in {

        val view = createViewAndFill(request, "CS")

        view.getElementById("eoriCS").attr("value") mustBe "GB1234"
        ensureRadioIsChecked(view, "CS")
        ensureRadiosAreUnChecked(view, "MF", "FW", "WH")
      }

      "display EORI with MF selected" in {

        val view = createViewAndFill(request, "MF")

        view.getElementById("eoriMF").attr("value") mustBe "GB1234"
        ensureRadioIsChecked(view, "MF")
        ensureRadiosAreUnChecked(view, "CS", "FW", "WH")
      }

      "display EORI with FW selected" in {

        val view = createViewAndFill(request, "FW")

        view.getElementById("eoriFW").attr("value") mustBe "GB1234"
        ensureRadioIsChecked(view, "FW")
        ensureRadiosAreUnChecked(view, "CS", "MF", "WH")
      }

      "display EORI with WH selected" in {

        val view = createViewAndFill(request, "WH")

        view.getElementById("eoriWH").attr("value") mustBe "GB1234"
        ensureRadioIsChecked(view, "WH")
        ensureRadiosAreUnChecked(view, "CS", "MF", "FW")
      }

      "display one row with data in table" in {

        val view =
          declarationAdditionalActorsPage(Mode.Normal, form, Seq(DeclarationAdditionalActors(Some(Eori("GB12345")), Some("CS"))))(
            request,
            realMessagesApi.preferred(request)
          )

        view.select("#actor_party_type_0").text() mustBe "Consolidator"
        view.select("#actor_0").text() mustBe "GB12345"

        val removeButton = view.select("#actor_action_0>button")

        removeButton.text() must include("Remove")
        removeButton.attr("value") mustBe """{"eori":"GB12345","partyType":"CS"}"""
      }

    }
  }
}
