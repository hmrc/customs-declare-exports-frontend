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

import base.ExportsTestData.{allValuesRequiringToSkipInlandOrBorder, itemWithPC, valuesRequiringToSkipInlandOrBorder}
import base.Injector
import controllers.declaration.routes.{
  InlandOrBorderController,
  ItemsSummaryController,
  SupervisingCustomsOfficeController,
  TransportLeavingTheBorderController
}
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.InlandModeOfTransportCode.form
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.inland_transport_details
import views.tags.ViewTest

@ViewTest
class InlandTransportDetailsViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val page = instanceOf[inland_transport_details]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[InlandModeOfTransportCode] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

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
        view.title must include(view.getElementsByTag("h1").text())
      }

      "display 'Mode of Transport' section" which {

        "have 'Sea' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.sea"
          view.getElementsByAttributeValue("for", "Inland_Sea") must containMessageForElements(key)
        }

        "have 'Road' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.rail"
          view.getElementsByAttributeValue("for", "Inland_Rail") must containMessageForElements(key)
        }

        "have 'Rail' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.road"
          view.getElementsByAttributeValue("for", "Inland_Road") must containMessageForElements(key)
        }

        "have 'Air' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.air"
          view.getElementsByAttributeValue("for", "Inland_Air") must containMessageForElements(key)
        }

        "have 'Postal or Mail' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.postalOrMail"
          view.getElementsByAttributeValue("for", "Inland_PostalOrMail") must containMessageForElements(key)
        }

        "have 'Fixed transport installations' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.fixedTransportInstallations"
          view.getElementsByAttributeValue("for", "Inland_FixedTransportInstallations") must containMessageForElements(key)
        }

        "have 'Inland waterway transport' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.inlandWaterway"
          view.getElementsByAttributeValue("for", "Inland_InlandWaterway") must containMessageForElements(key)
        }

        "have 'Mode unknown' option" in {
          val key = "declaration.warehouse.inlandTransportDetails.transportMode.unknown"
          view.getElementsByAttributeValue("for", "Inland_Unknown") must containMessageForElements(key)
        }
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }

    "display 'Back' button that links to /inland-or-border" when {
      additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
        s"AdditionalDeclarationType is $additionalType" in {
          val view = createView()(withRequest(additionalType))
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton.getElementById("back-link") must haveHref(InlandOrBorderController.displayPage)
        }
      }
    }

    "display 'Back' button that links to /supervising-customs-office" when {

      additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
        s"AdditionalDeclarationType is $additionalType and" when {
          "the user has previously entered a value which requires to skip the /inland-or-border page" in {
            allValuesRequiringToSkipInlandOrBorder.foreach { modifier =>
              val view = createView()(withRequest(additionalType, modifier))
              val backButton = view.getElementById("back-link")
              backButton must containMessage("site.backToPreviousQuestion")
              backButton.getElementById("back-link") must haveHref(SupervisingCustomsOfficeController.displayPage)
            }
          }
        }
      }

      List(SUPPLEMENTARY_EIDR, OCCASIONAL_FRONTIER, OCCASIONAL_PRE_LODGED).foreach { additionalType =>
        s"AdditionalDeclarationType is ${additionalType}" in {
          val view = createView()(withRequest(additionalType))
          val backButton = view.getElementById("back-link")
          backButton must containMessage("site.backToPreviousQuestion")
          backButton.getElementById("back-link") must haveHref(SupervisingCustomsOfficeController.displayPage)
        }
      }
    }

    "display 'Back' button that links to /transport-leaving-the-border" when {
      "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code and" when {

        additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
          s"AdditionalDeclarationType is $additionalType and" when {
            "the user has previously entered a value which requires to skip the /inland-or-border page" in {
              valuesRequiringToSkipInlandOrBorder.foreach { modifier =>
                val view = createView()(withRequest(additionalType, modifier, withItem(itemWithPC("1040"))))
                val backButton = view.getElementById("back-link")
                backButton must containMessage("site.backToPreviousQuestion")
                backButton.getElementById("back-link") must haveHref(TransportLeavingTheBorderController.displayPage)
              }
            }
          }
        }

        List(SUPPLEMENTARY_EIDR).foreach { additionalType =>
          s"AdditionalDeclarationType is ${additionalType}" in {
            val view = createView()(withRequest(additionalType, withItem(itemWithPC("1040"))))
            val backButton = view.getElementById("back-link")
            backButton must containMessage("site.backToPreviousQuestion")
            backButton.getElementById("back-link") must haveHref(TransportLeavingTheBorderController.displayPage)
          }
        }
      }
    }

    "display 'Back' button that links to /declaration-items-list" when {
      "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code and" when {
        List(OCCASIONAL_FRONTIER, OCCASIONAL_PRE_LODGED).foreach { additionalType =>
          s"AdditionalDeclarationType is ${additionalType}" in {
            val view = createView()(withRequest(additionalType, withItem(itemWithPC("1040"))))
            val backButton = view.getElementById("back-link")
            backButton must containMessage("site.backToPreviousQuestion")
            backButton.getElementById("back-link") must haveHref(ItemsSummaryController.displayItemsSummaryPage)
          }
        }
      }
    }
  }
}
