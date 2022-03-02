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
import controllers.declaration.routes.{OfficeOfExitController, TotalNumberOfItemsController}
import forms.declaration.TotalPackageQuantity
import models.DeclarationType._
import models.Mode
import org.jsoup.nodes.Document
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.total_package_quantity

class TotalPackageQuantityViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  val template = instanceOf[total_package_quantity]

  "Total Package Quantity view" when {

    "rendered with empty form" should {
      "have proper messages for keys" in {
        messages must haveTranslationFor("declaration.totalPackageQuantity.title")
        messages must haveTranslationFor("declaration.totalPackageQuantity.hint")
        messages must haveTranslationFor("declaration.totalPackageQuantity.empty")
        messages must haveTranslationFor("declaration.totalPackageQuantity.error")
        messages must haveTranslationFor("tariff.expander.title.clearance")
      }

      onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { implicit request =>
        val view: Document = template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, messages)

        "display same page title as header" in {
          val viewWithMessage: Document =
            template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, realMessagesApi.preferred(request))
          viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
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

        "display 'Save and continue' button on page" in {
          val saveButton = view.getElementById("submit")
          saveButton must containMessage("site.save_and_continue")
        }

        "display Tariff section text" in {
          val tariffText = view.getElementsByClass("govuk-details__summary-text").first()

          val titleKey = request.declarationType match {
            case CLEARANCE => "tariff.expander.title.clearance"
            case _         => "tariff.expander.title.common"
          }

          tariffText must containMessage(titleKey)
        }

        "display 'Save and return' button on page" in {
          val saveAndReturnButton = view.getElementById("submit_and_return")
          saveAndReturnButton must containMessage("site.save_and_come_back_later")
        }

        "rendered with invalid form" should {
          "display error when all entered input is incorrect" in {
            val form = TotalPackageQuantity
              .form(request.declarationType)
              .withError("totalPackage", "declaration.totalPackageQuantity.error")

            val view: Document = template.apply(Mode.Normal, form)(request, messages)

            view must haveGovukGlobalErrorSummary
            view must containErrorElementWithTagAndHref("a", "#totalPackage")
          }
        }

        "redered with filled form" should {
          "display data in inputs" in {
            val form = TotalPackageQuantity.form(request.declarationType).fill(TotalPackageQuantity(Some("1")))
            val view: Document = template.apply(Mode.Normal, form)(request, messages)

            view.getElementById("totalPackage").attr("value") mustEqual "1"
          }
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      "display back button" in {
        val view: Document = template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(TotalNumberOfItemsController.displayPage(Mode.Normal))
      }
    }

    onClearance { implicit request =>
      "display back button" in {
        val view: Document = template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, messages)
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(OfficeOfExitController.displayPage(Mode.Normal))
      }
    }
  }
}
