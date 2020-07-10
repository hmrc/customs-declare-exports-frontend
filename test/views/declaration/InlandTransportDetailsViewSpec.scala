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
import forms.declaration.InlandModeOfTransportCode
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.inland_transport_details
import views.tags.ViewTest

@ViewTest
class InlandTransportDetailsViewSpec extends UnitViewSpec2 with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[inland_transport_details]
  private val form: Form[InlandModeOfTransportCode] = InlandModeOfTransportCode.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[InlandModeOfTransportCode] = form)(implicit request: JourneyRequest[_]): Document =
    page(mode, form)(request, messages)

  "Inland Transport Details View" should {
    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL) { implicit request =>
      val view = createView()

      "have proper messages for labels" in {
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.sectionHeader")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.title")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.hint")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.error.incorrect")
      }

      "display same page title as header" in {
        val viewWithMessage = createView()
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display 'Mode of Transport' section" which {

        "have 'Sea' option" in {
          view.getElementsByAttributeValue("for", "Inland_Sea") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.sea"
          )
        }

        "have 'Road' option" in {
          view.getElementsByAttributeValue("for", "Inland_Rail") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.rail"
          )
        }

        "have 'Rail' option" in {
          view.getElementsByAttributeValue("for", "Inland_Road") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.road"
          )
        }

        "have 'Air' option" in {
          view.getElementsByAttributeValue("for", "Inland_Air") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.air"
          )
        }

        "have 'Postal or Mail' option" in {
          view
            .getElementsByAttributeValue("for", "Inland_PostalOrMail") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.postalOrMail"
          )
        }

        "have 'Fixed transport installations' option" in {
          view
            .getElementsByAttributeValue("for", "Inland_FixedTransportInstallations") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.fixedTransportInstallations"
          )
        }

        "have 'Inland waterway transport' option" in {
          view
            .getElementsByAttributeValue("for", "Inland_InlandWaterway") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.inlandWaterway"
          )
        }

        "have 'Mode unknown' option" in {
          view
            .getElementsByAttributeValue("for", "Inland_Unknown") must containMessageForElements(
            "declaration.warehouse.inlandTransportDetails.transportMode.unknown"
          )
        }
      }

      "display 'Back' button that links to 'Supervising Customs Office' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.back")
        backButton.getElementById("back-link") must haveHref(
          controllers.declaration.routes.SupervisingCustomsOfficeController.displayPage(Mode.Normal)
        )
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
      }
    }
  }
}
