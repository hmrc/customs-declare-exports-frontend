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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.WarehouseIdentification
import forms.declaration.WarehouseIdentification.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import views.components.gds.Styles
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.warehouse_identification_yesno
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationYesNoViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[warehouse_identification_yesno]

  override val typeAndViewInstance = (STANDARD, page(form(false))(_, _))

  def createView(frm: Form[WarehouseIdentification] = form(false))(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Warehouse Identification Number View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.warehouse.identification.optional.title")
      messages must haveTranslationFor("declaration.warehouse.identification.label")
      messages must haveTranslationFor("declaration.warehouse.identification.label.hint")
      messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.error")
      messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.empty")
      messages must haveTranslationFor("declaration.warehouse.identification.answer.error")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display same page title as header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "have the correct section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
      }

      "have the correct page title" in {
        view.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.warehouse.identification.optional.title")
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }
      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Transport Leaving the Border' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(TransportLeavingTheBorderController.displayPage)
      }
    }
    onJourney(SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Items Summary' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(ItemsSummaryController.displayItemsSummaryPage)
      }
    }
  }

  "Warehouse Identification Number View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when code is empty" in {
        val view = createView(form(false).fillAndValidate(WarehouseIdentification(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#identificationNumber")

        view must containErrorElementWithMessageKey("declaration.warehouse.identification.identificationNumber.empty")
      }

      "display error when code is incorrect" in {
        val view = createView(form(false).fillAndValidate(WarehouseIdentification(Some("ABC!!!"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#identificationNumber")

        view must containErrorElementWithMessageKey("declaration.warehouse.identification.identificationNumber.format")
      }
    }
  }
}
