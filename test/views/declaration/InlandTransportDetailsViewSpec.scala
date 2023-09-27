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

import base.ExportsTestData.{modifierForPC1040, valuesRequiringToSkipInlandOrBorder}
import base.Injector
import controllers.declaration.routes.{InlandOrBorderController, SupervisingCustomsOfficeController, TransportLeavingTheBorderController}
import controllers.helpers.TransportSectionHelper.additionalDeclTypesAllowedOnInlandOrBorder
import forms.declaration.InlandModeOfTransportCode
import forms.declaration.InlandModeOfTransportCode.form
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
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
  }

  "Inland Transport Details View" when {

    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType" should {

        "display a 'Back' button linking to /inland-or-border" in {
          verifyBackButton(InlandOrBorderController.displayPage)(withRequest(additionalType))
        }

        "display a 'Back' button linking to /supervising-customs-office" when {
          "the 'InlandOrBorder' page is skipped" in {
            valuesRequiringToSkipInlandOrBorder.foreach { modifier =>
              implicit val request = withRequest(additionalType, modifier)
              verifyBackButton(SupervisingCustomsOfficeController.displayPage)
            }
          }
        }

        "display a 'Back' button linking to /transport-leaving-the-border" when {
          "all declaration's items have '1040' as PC and '000' as APC" in {
            valuesRequiringToSkipInlandOrBorder.foreach { modifier =>
              implicit val request = withRequest(additionalType, modifier, modifierForPC1040)
              verifyBackButton(TransportLeavingTheBorderController.displayPage)
            }
          }
        }
      }
    }

    "AdditionalDeclarationType is SUPPLEMENTARY_EIDR" should {
      "display a 'Back' button linking to /supervising-customs-office" in {
        verifyBackButton(SupervisingCustomsOfficeController.displayPage)(withRequest(SUPPLEMENTARY_EIDR))
      }
    }

    def verifyBackButton(call: Call)(implicit request: JourneyRequest[_]): Unit = {
      val backButton = createView().getElementById("back-link")
      backButton must containMessage(backToPreviousQuestionCaption)
      backButton must haveHref(call)
    }
  }
}
