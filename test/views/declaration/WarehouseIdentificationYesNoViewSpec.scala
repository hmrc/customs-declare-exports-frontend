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

import base.Injector
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.WarehouseIdentification
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.warehouse_identification_yesno
import views.tags.ViewTest

@ViewTest
class WarehouseIdentificationYesNoViewSpec extends UnitViewSpec2 with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[warehouse_identification_yesno]
  private val form: Form[WarehouseIdentification] = WarehouseIdentification.form(yesNo = false)

  private def createView(mode: Mode = Mode.Normal, form: Form[WarehouseIdentification] = form, messages: Messages = stubMessages())(
    implicit request: JourneyRequest[_]
  ): Document =
    page(mode, form)(request, messages)

  "Warehouse Identification Number View" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "have proper messages for labels" in {
        val messages = instanceOf[MessagesApi].preferred(journeyRequest())
        messages must haveTranslationFor("declaration.warehouse.identification.sectionHeader")
        messages must haveTranslationFor("declaration.warehouse.identification.optional.title")
        messages must haveTranslationFor("declaration.warehouse.identification.label")
        messages must haveTranslationFor("declaration.warehouse.identification.label.hint")
        messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.error")
        messages must haveTranslationFor("declaration.warehouse.identification.identificationNumber.empty")
        messages must haveTranslationFor("declaration.warehouse.identification.answer.error")
      }

      "display same page title as header" in {
        val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "have the correct section header" in {
        view.getElementById("section-header").text() must include("declaration.warehouse.identification.sectionHeader")
      }

      "have the correct page title" in {
        view.getElementsByTag("h1").text() mustBe "declaration.warehouse.identification.optional.title"
      }

      "display radio button with Yes option" in {
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes").text() mustBe "site.yes"
      }
      "display radio button with No option" in {
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no").text() mustBe "site.no"
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit").text() mustBe "site.save_and_continue"
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return").text() mustBe "site.save_and_come_back_later"
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Transport Leaving the Border' page" in {
        val backButton = view.getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.TransportLeavingTheBorderController.displayPage())
      }
    }
    onJourney(SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Items Summary' page" in {
        val backButton = view.getElementById("back-link")

        backButton.text() mustBe "site.back"
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage(Mode.Normal)
        )
      }
    }
  }

  "Warehouse Identification Number View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error when code is empty" in {
        val view = createView(form = form.fillAndValidate(WarehouseIdentification(Some(""))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#identificationNumber")

        view must containErrorElementWithMessage("declaration.warehouse.identification.identificationNumber.error")
      }

      "display error when code is incorrect" in {
        val view = createView(form = form.fillAndValidate(WarehouseIdentification(Some("ABC!!!"))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#identificationNumber")

        view must containErrorElementWithMessage("declaration.warehouse.identification.identificationNumber.error")
      }
    }
  }
}
