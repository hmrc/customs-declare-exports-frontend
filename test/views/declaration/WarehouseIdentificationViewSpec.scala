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

package views.declaration

import base.Injector
import controllers.declaration.routes.{ItemsSummaryController, TransportLeavingTheBorderController}
import forms.declaration.WarehouseIdentification
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.warehouse_identification
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[warehouse_identification]
  val form: Form[WarehouseIdentification] = WarehouseIdentification.form(yesNo = false)

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[WarehouseIdentification] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Warehouse Identification Number View" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "have proper messages for labels" in {
        val messages = instanceOf[MessagesApi].preferred(journeyRequest())
        messages must haveTranslationFor("declaration.warehouse.identification.required.title")
        messages must haveTranslationFor("declaration.warehouse.identification.label")
        messages must haveTranslationFor("declaration.warehouse.identification.label.hint")
        messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.error")
        messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.empty")
        messages must haveTranslationFor("declaration.warehouse.identification.answer.error")
      }

      "display same page title as header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "have the correct section header" in {
        view.getElementById("section-header").text() mustBe messages("declaration.section.6")
      }

      "have the correct page title" in {
        view.getElementsByTag("h1").text() mustBe messages("declaration.warehouse.identification.required.title")
      }

      "display input field" in {
        view.getElementById("identificationNumber").attr("value") mustBe empty
        view.getElementsByAttributeValue("for", "identificationNumber").text() mustBe messages("declaration.warehouse.identification.required.title")
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Transport Leaving the Border' page" in {
        val backButton = createView().getElementById("back-link")
        backButton.text() mustBe messages("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(TransportLeavingTheBorderController.displayPage)
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Items Summary' page" in {
        val backButton = createView().getElementById("back-link")
        backButton.text() mustBe messages("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(ItemsSummaryController.displayItemsSummaryPage)
      }
    }
  }

  "Warehouse Identification Number View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when code is empty" in {
        val view = createView(form.fillAndValidate(WarehouseIdentification(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#identificationNumber")

        val text = view.getElementsByClass("govuk-error-message").get(0).text()
        text contains messages("declaration.warehouse.identification.identificationNumber.empty")
      }

      "display error when code is incorrect" in {
        val view = createView(form.fillAndValidate(WarehouseIdentification(Some("ABC!!!"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#identificationNumber")

        val text = view.getElementsByClass("govuk-error-message").get(0).text()
        text contains messages("declaration.warehouse.identification.identificationNumber.format")
      }
    }
  }
}
