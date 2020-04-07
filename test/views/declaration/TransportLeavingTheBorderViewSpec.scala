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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.TransportLeavingTheBorder
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.transport_leaving_the_border

class TransportLeavingTheBorderViewSpec extends UnitViewSpec with Stubs with Injector {

  private val page = instanceOf[transport_leaving_the_border]
  private def view(implicit request: JourneyRequest[_]): Document = page(TransportLeavingTheBorder.form(request.declarationType), Mode.Normal)

  "Transport Leaving The Border Page" must {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        view.getElementsByTag("h1").text() mustBe "declaration.transport.leavingTheBorder.title"
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit").text() mustBe messages("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        val saveAndReturn = view.getElementById("submit_and_return")
        saveAndReturn.text() mustBe messages("site.save_and_come_back_later")
        saveAndReturn must haveAttribute("name", SaveAndReturn.toString)
      }

      "display 'Mode of Transport' section" which {

        "have 'Sea' option" in {
          view.getElementsByAttributeValue("for", "Border_Sea").text() mustBe "declaration.transport.leavingTheBorder.transportMode.sea"
        }

        "have 'Road' option" in {
          view.getElementsByAttributeValue("for", "Border_Road").text() mustBe "declaration.transport.leavingTheBorder.transportMode.road"
        }

        "have 'Rail' option" in {
          view.getElementsByAttributeValue("for", "Border_Rail").text() mustBe "declaration.transport.leavingTheBorder.transportMode.rail"
        }

        "have 'Air' option" in {
          view.getElementsByAttributeValue("for", "Border_Air").text() mustBe "declaration.transport.leavingTheBorder.transportMode.air"
        }

        "have 'Postal or Mail' option" in {
          view
            .getElementsByAttributeValue("for", "Border_PostalOrMail")
            .text() mustBe "declaration.transport.leavingTheBorder.transportMode.postalOrMail"
        }

        "have 'Fixed transport installations' option" in {
          view
            .getElementsByAttributeValue("for", "Border_FixedTransportInstallations")
            .text() mustBe "declaration.transport.leavingTheBorder.transportMode.fixedTransportInstallations"
        }

        "have 'Inland waterway transport' option" in {
          view
            .getElementsByAttributeValue("for", "Border_InlandWaterway")
            .text() mustBe "declaration.transport.leavingTheBorder.transportMode.inlandWaterway"
        }

        "have 'Mode unknown' option" in {
          view
            .getElementsByAttributeValue("for", "Border_Unknown")
            .text() mustBe "declaration.transport.leavingTheBorder.transportMode.unknown"
        }
      }
    }
  }

  onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
    "display 'Back' button that links to 'Supervising Customs Office' page" in {
      val backButton = view.getElementById("back-link")
      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.InlandTransportDetailsController.displayPage())
    }

    "display 'Mode of Transport' section" which {
      "not have 'I don't know' option" in {
        view.getElementsByAttributeValue("for", "Border_Empty") mustBe empty
      }
    }
  }

  onJourney(CLEARANCE) { implicit request =>
    "display 'Back' button that links to 'Supervising Customs Office' page" in {
      val backButton = view.getElementById("back-link")
      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.SupervisingCustomsOfficeController.displayPage())
    }

    "display 'Mode of Transport' section" which {
      "have 'I don't know' option" in {
        val section = view.getElementById("transportLeavingTheBorder")
        view
          .getElementsByAttributeValue("for", "Border_Empty")
          .text() mustBe "declaration.transport.leavingTheBorder.transportMode.empty"
      }
    }
  }

}
