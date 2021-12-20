/*
 * Copyright 2021 HM Revenue & Customs
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

import base.ExportsTestData.itemWith1040AsPC
import base.Injector
import controllers.declaration.routes
import controllers.helpers.TransportSectionHelper.altAdditionalTypesOnTransportSection
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.inland_transport_details
import views.tags.ViewTest

@ViewTest
class InlandTransportDetailsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[inland_transport_details]
  private val form: Form[InlandModeOfTransportCode] = InlandModeOfTransportCode.form()

  private def createView(mode: Mode = Mode.Normal, form: Form[InlandModeOfTransportCode] = form)(implicit request: JourneyRequest[_]): Document =
    page(mode, form)(request, messages)

  "Inland Transport Details View" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()

      "have required messages" in {
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.title")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.body")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.error.incorrect")

        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.sea")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.sea.hint")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.rail")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.road")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.road.hint")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.air")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.air.hint")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.postalOrMail")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.postalOrMail.hint")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.fixedTransportInstallations")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.fixedTransportInstallations.hint")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.inlandWaterway")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.unknown")
        messages must haveTranslationFor("declaration.warehouse.inlandTransportDetails.transportMode.unknown.hint")
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

      "display 'Save and continue' button on page" in {
        view.getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
      }
    }

    altAdditionalTypesOnTransportSection.foreach { additionalType =>
      implicit val request = withRequest(additionalType)
      "display 'Back' button that links to the 'Inland Or Border' page" when {
        s"AdditionalDeclarationType is $additionalType" in {
          val view = createView()
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(routes.InlandOrBorderController.displayPage())
        }
      }
    }

    List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
      implicit val request = withRequest(additionalType)
      val view = createView()

      "display 'Back' button that links to 'Supervising Customs Office' page" when {
        s"AdditionalDeclarationType is SUPPLEMENTARY_EIDR" in {
          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(routes.SupervisingCustomsOfficeController.displayPage())
        }
      }
    }

    List(SUPPLEMENTARY_EIDR, OCCASIONAL_FRONTIER, OCCASIONAL_PRE_LODGED, SIMPLIFIED_FRONTIER, SIMPLIFIED_PRE_LODGED).foreach { additionalType =>
      "display 'Back' button that links to 'Supervising Customs Office' page" when {
        implicit val request = withRequest(additionalType)
        val view = createView()

        s"AdditionalDeclarationType is ${additionalType}" in {
          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(routes.SupervisingCustomsOfficeController.displayPage())
        }
      }
    }

    List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
      "display 'Back' button that links to 'Transport Leaving The Border' page" when {
        "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code and" when {
          implicit val request = withRequest(additionalType, withItem(itemWith1040AsPC))
          val view = createView()

          s"AdditionalDeclarationType is ${additionalType}" in {
            val backButton = view.getElementById("back-link")

            backButton must containMessage("site.back")
            backButton.getElementById("back-link") must haveHref(routes.TransportLeavingTheBorderController.displayPage())
          }
        }
      }
    }

    List(OCCASIONAL_FRONTIER, OCCASIONAL_PRE_LODGED, SIMPLIFIED_FRONTIER, SIMPLIFIED_PRE_LODGED).foreach { additionalType =>
      "display 'Back' button that links to 'Items Summary' page" when {
        "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code and" when {
          implicit val request = withRequest(additionalType, withItem(itemWith1040AsPC))
          val view = createView()

          s"AdditionalDeclarationType is ${additionalType}" in {
            val backButton = view.getElementById("back-link")

            backButton must containMessage("site.back")
            backButton.getElementById("back-link") must haveHref(routes.ItemsSummaryController.displayItemsSummaryPage())
          }
        }
      }
    }
  }
}
