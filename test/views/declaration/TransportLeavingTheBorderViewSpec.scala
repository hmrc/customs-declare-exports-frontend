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
import controllers.declaration.routes
import controllers.routes.GuidanceController
import forms.declaration.TransportLeavingTheBorder.form
import forms.declaration.authorisationHolder.AuthorizationTypeCodes.EXRR
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.transport_leaving_the_border
import views.tags.ViewTest

@ViewTest
class TransportLeavingTheBorderViewSpec extends PageWithButtonsSpec with Injector {

  val page = instanceOf[transport_leaving_the_border]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView()(implicit request: JourneyRequest[_]): Document = page(form)

  "Transport Leaving The Border Page" must {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.transport.leavingTheBorder.title")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
      }

      "display the expected body text" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.transport.leavingTheBorder.hint")
      }

      "not display any inset text when no authorisation code has been entered" in {
        view.getElementsByClass("govuk-inset-text").size mustBe 0
      }

      "display 'Back' button that links to 'Items Summary' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(routes.ItemsSummaryController.displayItemsSummaryPage)
      }

      checkAllSaveButtonsAreDisplayed(createView())

      "display 'Mode of Transport' section" which {

        "have 'Sea' option" in {
          view.getElementsByAttributeValue("for", "Border_Sea") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.sea"
          )
        }

        "have 'RoRo' option" in {
          view.getElementsByAttributeValue("for", "Border_Ferry") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.ferry"
          )
        }

        "have 'Road' option" in {
          view.getElementsByAttributeValue("for", "Border_Road") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.road"
          )
        }

        "have 'Rail' option" in {
          view.getElementsByAttributeValue("for", "Border_Rail") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.rail"
          )
        }

        "have 'Air' option" in {
          view.getElementsByAttributeValue("for", "Border_Air") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.air"
          )
        }

        "have 'Postal or Mail' option" in {
          view.getElementsByAttributeValue("for", "Border_PostalOrMail") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.postalOrMail"
          )
          val hint = view.getElementsByClass("govuk-radios__hint")
          hint.get(0).text mustBe messages("declaration.transport.leavingTheBorder.transportMode.postalOrMail.hint")
        }

        "have 'Fixed transport installations' option" in {
          view.getElementsByAttributeValue("for", "Border_FixedTransportInstallations") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.fixedTransportInstallations"
          )
          val hint = view.getElementsByClass("govuk-radios__hint")
          hint.get(1).text mustBe messages("declaration.transport.leavingTheBorder.transportMode.fixedTransportInstallations.hint")
        }

        "have 'Inland waterway transport' option" in {
          view.getElementsByAttributeValue("for", "Border_InlandWaterway") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.inlandWaterway"
          )
        }

        "have 'Mode unknown' option" in {
          view.getElementsByAttributeValue("for", "Border_Unknown") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.unknown"
          )
          val hint = view.getElementsByClass("govuk-radios__hint")
          hint.get(2).text mustBe messages("declaration.transport.leavingTheBorder.transportMode.unknown.hint")
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "display 'Mode of Transport' section" which {
        "not have 'I don't know' option" in {
          view.getElementsByAttributeValue("for", "Border_Empty") mustBe empty
        }
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      val view = createView()

      "display 'Mode of Transport' section" which {
        "have 'I don't know' option" in {
          view.getElementsByAttributeValue("for", "Border_Empty") must containMessageForElements(
            "declaration.transport.leavingTheBorder.transportMode.empty"
          )
        }
      }
    }

    onEveryAdditionalType(withAuthorisationHolders(Some("APE"))) { implicit request =>
      val view = createView()

      "not display any inset text when no 'EXRR' authorisation code has been entered" in {
        view.getElementsByClass("govuk-inset-text").size mustBe 0
      }
    }

    onEveryAdditionalType(withAuthorisationHolders(Some(EXRR))) { implicit request =>
      val view = createView()

      "display the expected inset text when an 'EXRR' authorisation code has been entered" in {
        val insetText = view.getElementsByClass("govuk-inset-text")
        insetText.size mustBe 1
        insetText.text mustBe messages("declaration.transport.leavingTheBorder.inset", messages("declaration.transport.leavingTheBorder.inset.link"))

        val links = view.getElementsByClass("govuk-link--no-visited-state")
        links.get(0) must haveHref(GuidanceController.sendByRoro.url)
      }
    }
  }
}
