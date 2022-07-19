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

import base.Injector
import forms.declaration.SupervisingCustomsOffice
import models.requests.JourneyRequest
import models.{DeclarationType, ExportsDeclaration, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.supervising_customs_office
import views.tags.ViewTest

@ViewTest
class SupervisingCustomsOfficeViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[supervising_customs_office]
  private val form: Form[SupervisingCustomsOffice] = SupervisingCustomsOffice.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[SupervisingCustomsOffice] = form)(implicit request: JourneyRequest[_]) =
    page(mode, form)(request, messages)

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

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }

    onJourney(DeclarationType.CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Warehouse Identification Number' page" in {
        val backButton = createView().getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.WarehouseIdentificationController.displayPage(Mode.Normal)
        )
      }
    }

    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'Warehouse Identification Number' page when procedure code ends with '78'" in {

        val modelWithProcedureCode: ExportsDeclaration =
          aDeclarationAfter(request.cacheModel, withItem(anItem(withProcedureCodes(Some("1078"), Seq("000")))))
        val backButton = createView()(journeyRequest(modelWithProcedureCode)).getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.WarehouseIdentificationController.displayPage(Mode.Normal)
        )
      }
    }

    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to 'Transport Leaving the Border' page when procedure code ends with '00'" in {

        val modelWithProcedureCode: ExportsDeclaration =
          aDeclarationAfter(request.cacheModel, withItem(anItem(withProcedureCodes(Some("0000"), Seq("000")))))
        val backButton = createView()(journeyRequest(modelWithProcedureCode)).getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.TransportLeavingTheBorderController.displayPage(Mode.Normal)
        )
      }
    }

    onJourney(DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { implicit request =>
      "display 'Back' button that links to 'Items Summary' page when procedure code ends with '00'" in {

        val modelWithProcedureCode: ExportsDeclaration =
          aDeclarationAfter(request.cacheModel, withItem(anItem(withProcedureCodes(Some("0000"), Seq("000")))))
        val backButton = createView()(journeyRequest(modelWithProcedureCode)).getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.ItemsSummaryController.displayItemsSummaryPage(Mode.Normal)
        )
      }
    }
  }
}
