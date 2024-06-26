/*
 * Copyright 2023 HM Revenue & Customs
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

package views.section2

import base.{Injector, TestHelper}
import controllers.section2.routes.ConsigneeDetailsController
import forms.common.Eori
import forms.section2.AdditionalActor.form
import forms.section2.AdditionalActor
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import views.declaration.spec.PageWithButtonsSpec
import views.html.section2.additionalActors.additional_actors_add
import views.tags.ViewTest

@ViewTest
class AdditionalActorsAddViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[additional_actors_add]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[AdditionalActor] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)

  "Declaration Additional Actors" should {

    "have correct message keys" in {
      messages must haveTranslationFor("declaration.additionalActors.body.text")
      messages must haveTranslationFor("declaration.additionalActors.eori")
      messages must haveTranslationFor("declaration.eori.empty")
      messages must haveTranslationFor("declaration.additionalActors.partyType")
      messages must haveTranslationFor("declaration.partyType")
      messages must haveTranslationFor("declaration.partyType.empty")
      messages must haveTranslationFor("declaration.partyType.error")
    }
  }

  "Declaration Additional Actors View on empty page" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { implicit request =>
      val view = createView(form)

      "display page title" in {
        view.getElementsByClass("govuk-fieldset__heading").first() must containMessage("declaration.additionalActors.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display five radio buttons with description (not selected)" in {
        val view = createView(AdditionalActor.form.fill(AdditionalActor(Some(Eori("")), Some(""))))

        def checkOption(key: String, messagePrefix: String = "declaration.partyType."): Assertion = {
          val option = view.getElementById(key)
          option.attr("checked") mustBe empty
          val optionLabel = view.getElementsByAttributeValueMatching("for", key).first()
          optionLabel must containMessage(s"$messagePrefix$key")
        }

        checkOption("CS")
        checkOption("MF")
        checkOption("FW")
        checkOption("WH")
        checkOption("no", "site.")

        view.getElementById("WH-item-hint").text mustBe messages("declaration.partyType.warehouseKeeper.hint")
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to 'Consignee Details' page" in {
        val view = page(form)(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe ConsigneeDetailsController.displayPage.url
      }
    }

  }

  "Declaration Additional Actors View with invalid input" must {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, SUPPLEMENTARY) { implicit request =>
      def incorrectEori(partyType: String): Boolean = {
        val view = createView(
          AdditionalActor.form
            .fillAndValidate(AdditionalActor(Some(Eori(TestHelper.createRandomAlphanumericString(18))), Some(partyType)))
        )

        view must haveGovukGlobalErrorSummary

        view must containErrorElementWithTagAndHref("a", s"#eori$partyType")
        view.getElementsByClass("govuk-error-message").text() contains messages("declaration.eori.error.format")
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
      createView(AdditionalActor.form.fill(AdditionalActor(Some(Eori("GB1234")), Some(partyType))))(request)

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
    }
  }
}
