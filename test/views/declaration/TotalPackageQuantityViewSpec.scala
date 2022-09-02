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
import controllers.declaration.routes.{InvoiceAndExchangeRateChoiceController, InvoiceAndExchangeRateController}
import forms.declaration.TotalPackageQuantity
import forms.declaration.TotalPackageQuantity.form
import models.DeclarationType._
import models.Mode
import models.Mode.Normal
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.total_package_quantity

class TotalPackageQuantityViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  val template = instanceOf[total_package_quantity]

  def createView(mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document = template(mode, form(request.declarationType))

  "Total Package Quantity view" should {

    "have proper messages for keys" in {
      messages must haveTranslationFor("declaration.totalPackageQuantity.title")
      messages must haveTranslationFor("declaration.totalPackageQuantity.hint")
      messages must haveTranslationFor("declaration.totalPackageQuantity.empty")
      messages must haveTranslationFor("declaration.totalPackageQuantity.error")
      messages must haveTranslationFor("tariff.expander.title.clearance")
    }

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView()

      "display same page title as header" in {
        view.title must include(view.getElementsByTag("h1").text)
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display header" in {
        view.getElementsByTag("h1").first() must containMessage("declaration.totalPackageQuantity.title")
      }

      "display empty input with label for Total Package" in {
        view.getElementById("totalPackage").attr("value") mustBe empty
        view.getElementById("totalPackage-hint").text() mustBe messages("declaration.totalPackageQuantity.hint")
      }

      "display Tariff section text" in {
        val tariffText = view.getElementsByClass("govuk-details__summary-text").first()

        val titleKey = request.declarationType match {
          case CLEARANCE => "tariff.expander.title.clearance"
          case _         => "tariff.expander.title.common"
        }

        tariffText must containMessage(titleKey)
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

      "display error when all entered input is incorrect" in {
        val error = "declaration.totalPackageQuantity.error"
        val formWithError = form(request.declarationType).withError("totalPackage", error)
        val view: Document = template(Normal, formWithError)(request, messages)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#totalPackage")
      }

      "display existing input data" in {
        val formWithData = form(request.declarationType).fill(TotalPackageQuantity(Some("1")))
        val view = template(Normal, formWithData)(request, messages)

        view.getElementById("totalPackage").attr("value") mustEqual "1"
      }
    }

    List(STANDARD, SUPPLEMENTARY).foreach { declarationType =>
      "display back button linking to /invoice-and-exchange-choice" when {
        s"the declaration type $declarationType and" when {
          implicit val request = withRequestOfType(declarationType)

          "the invoice's total amount is NOT defined" in {
            val backButton = createView().getElementById("back-link")
            backButton must containMessage("site.backToPreviousQuestion")
            backButton.getElementById("back-link") must haveHref(InvoiceAndExchangeRateChoiceController.displayPage(Normal))
          }
        }
      }

      "display back button linking to /invoice-and-exchange" when {
        s"the declaration type $declarationType and" when {
          implicit val request = withRequestOfType(declarationType, withTotalNumberOfItems(Some("1000000")))

          "the invoice's total amount entered is defined (it should always be equal or greater than 100,000)" in {
            val backButton = createView().getElementById("back-link")
            backButton must containMessage("site.backToPreviousQuestion")
            backButton.getElementById("back-link") must haveHref(InvoiceAndExchangeRateController.displayPage(Normal))
          }
        }
      }
    }
  }
}
