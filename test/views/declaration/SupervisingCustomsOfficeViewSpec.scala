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

import base.ExportsTestData.modifiersForWarehouseRequired
import base.Injector
import controllers.declaration.routes.{TransportLeavingTheBorderController, WarehouseIdentificationController}
import forms.declaration.SupervisingCustomsOffice
import models.DeclarationType
import models.DeclarationType._
import models.requests.JourneyRequest
import play.api.data.Form
import play.twirl.api.HtmlFormat.Appendable
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.supervising_customs_office
import views.tags.ViewTest

@ViewTest
class SupervisingCustomsOfficeViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[supervising_customs_office]
  private val form: Form[SupervisingCustomsOffice] = SupervisingCustomsOffice.form

  private def createView(form: Form[SupervisingCustomsOffice] = form)(implicit request: JourneyRequest[_]): Appendable =
    page(form)(request, messages)

  "Supervising Customs Office View" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.warehouse.supervisingCustomsOffice.title")
        messages must haveTranslationFor("declaration.warehouse.supervisingCustomsOffice.paragraph.1")
        messages must haveTranslationFor("declaration.warehouse.supervisingCustomsOffice.paragraph.2")
        messages must haveTranslationFor("declaration.warehouse.supervisingCustomsOffice.paragraph.3")
        messages must haveTranslationFor("declaration.warehouse.supervisingCustomsOffice.label")
        messages must haveTranslationFor("declaration.warehouse.supervisingCustomsOffice.error")
      }

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.warehouse.supervisingCustomsOffice.title")
      }

      "display same page title and header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    onJourney(DeclarationType.CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Warehouse Identification Number' page" in {
        val backButton = createView().getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(WarehouseIdentificationController.displayPage)
      }
    }
  }

  "Supervising Customs Office View" when {

    List(STANDARD, OCCASIONAL, SUPPLEMENTARY, SIMPLIFIED).foreach { declarationType =>
      s"declaration type is $declarationType" should {

        "display 'Back' button that links to 'Transport Leaving the Border' page" in {
          implicit val request = withRequestOfType(declarationType)
          val backButton = createView().getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton.getElementById("back-link") must haveHref(TransportLeavingTheBorderController.displayPage)
        }

        "display 'Back' button that links to 'Warehouse Identification Number' page" when {
          "the given PC ends with '07' or '71' or '78'" in {
            modifiersForWarehouseRequired.foreach { modifier =>
              implicit val request = withRequestOfType(declarationType, modifier)
              val backButton = createView().getElementById("back-link")
              backButton must containMessage("site.backToPreviousQuestion")
              backButton.getElementById("back-link") must haveHref(WarehouseIdentificationController.displayPage)
            }
          }
        }
      }
    }
  }
}
