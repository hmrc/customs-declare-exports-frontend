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
import forms.declaration.TotalPackageQuantity
import models.DeclarationType._
import models.Mode
import org.jsoup.nodes.Document
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.total_package_quantity

class TotalPackageQuantityViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  val template = new total_package_quantity(mainTemplate)

  "Total Package Quantity view" when {

    "rendered with empty form" should {
      "have proper messages for keys" in {
        val messages = instanceOf[MessagesApi].preferred(journeyRequest())
        messages must haveTranslationFor("declaration.totalPackageQuantity.title")
        messages must haveTranslationFor("supplementary.totalPackageQuantity")
        messages must haveTranslationFor("supplementary.totalPackageQuantity.empty")
        messages must haveTranslationFor("supplementary.totalPackageQuantity.error")
        messages must haveTranslationFor("declaration.totalPackageQuantity.error.required")
        messages must haveTranslationFor("site.details.summary_text_this")
      }

      onJourney(STANDARD, SUPPLEMENTARY, CLEARANCE) { request =>
        val view: Document = template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, messages)

        "display same page title as header" in {
          val viewWithMessage: Document =
            template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, realMessagesApi.preferred(request))
          viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
        }

        "display section header" in {
          view.getElementById("section-header").text() must include("supplementary.items")
        }

        "display header" in {
          view.getElementById("title").text() must be("declaration.totalPackageQuantity.title")
        }

        "display empty input with label for Total Package" in {
          view.getElementById("totalPackage-label").text() must be("")
          view.getElementById("totalPackage").attr("value") mustBe empty
        }

        "display 'Save and continue' button on page" in {
          val saveButton = view.getElementById("submit")
          saveButton.text() must be("site.save_and_continue")
        }

        "display Tariff section text" in {
          val tariffText = view.getElementsByClass("govuk-details__summary-text").first().text()
          tariffText.text() must be("site.details.summary_text_this")
        }

        "display 'Save and return' button on page" in {
          val saveAndReturnButton = view.getElementById("submit_and_return")
          saveAndReturnButton.text() must be("site.save_and_come_back_later")
        }

        "rendered with invalid form" should {
          "display error when all entered input is incorrect" in {
            val form = TotalPackageQuantity.form(request.declarationType).withError("totalPackage", "supplementary.totalPackageQuantity.error")
            val view: Document = template.apply(Mode.Normal, form)(request, messages)

            view must haveGlobalErrorSummary
            view must haveFieldErrorLink("totalPackage", "#totalPackage")
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
    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "display back button" in {
        val view: Document = template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, messages)
        val backButton = view.getElementById("back-link")

        backButton.text() must be("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.TotalNumberOfItemsController.displayPage(Mode.Normal))
      }
    }

    onClearance { request =>
      "display back button" in {
        val view: Document = template.apply(Mode.Normal, TotalPackageQuantity.form(request.declarationType))(request, messages)
        val backButton = view.getElementById("back-link")

        backButton.text() must be("site.back")
        backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.OfficeOfExitController.displayPage(Mode.Normal))
      }
    }
  }

}
