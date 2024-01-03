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

import base.{Injector, MockAuthAction}
import controllers.declaration.routes
import controllers.helpers.TransportSectionHelper._
import forms.common.YesNoAnswer
import forms.declaration.InlandOrBorder.Border
import forms.declaration.countries.Country
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.express_consignment
import views.tags.ViewTest

@ViewTest
class ExpressConsignmentViewSpec extends UnitViewSpec with CommonMessages with Injector with MockAuthAction {

  private val page = instanceOf[express_consignment]
  private val form: Form[YesNoAnswer] = YesNoAnswer.form()

  private def createView(form: Form[YesNoAnswer] = form)(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  private val msgKey = "declaration.transportInformation.expressConsignment"

  "'Express Consignment' view" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView()

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
      }

      "display page title" in {
        view.getElementsByTag("h1").first() must containMessage(s"$msgKey.title")
      }

      "display two Yes/No radio buttons" in {
        val radios = view.getElementsByClass("govuk-radios").first.children
        radios.size mustBe 2
        Option(radios.first.getElementById("code_yes")) mustBe defined
        Option(radios.last.getElementById("code_no")) mustBe defined
      }

      "select the 'Yes' radio when clicked" in {
        val form = YesNoAnswer.form().bind(Map("yesNo" -> "Yes"))
        val view = createView(form = form)
        view.getElementById("code_yes") must beSelected
      }

      "select the 'No' radio when clicked" in {
        val form = YesNoAnswer.form().bind(Map("yesNo" -> "No"))
        val view = createView(form = form)
        view.getElementById("code_no") must beSelected
      }
    }
  }

  "'Express Consignment' view" when {

    List(STANDARD, OCCASIONAL, SIMPLIFIED).foreach { declarationType =>
      s"DeclarationType is '$declarationType'" should {

        "display the expected tariff details" in {
          implicit val request = withRequestOfType(declarationType)
          verifyTariffDetails(createView(), "common")
        }

        "display a 'Back' button linking the 'TransportCountry' page" in {
          implicit val request = withRequestOfType(declarationType)
          verifyBackButton(createView(), routes.TransportCountryController.displayPage)
        }

        postalOrFTIModeOfTransportCodes.foreach { transportCode =>
          "display a back button linking to the /inland-or-border page" when {
            s"TransportLeavingTheBorder is '${transportCode.value}' and" when {
              "InlandOrBorder is 'Border'" in {
                val modeOfTransportCode = withBorderModeOfTransportCode(transportCode)
                implicit val request = withRequestOfType(declarationType, modeOfTransportCode, withInlandOrBorder(Some(Border)))
                verifyBackButton(createView(), routes.InlandOrBorderController.displayPage)
              }
            }
          }

          "display a back button linking to the /inland-transport-details page" when {
            s"InlandTransportDetails is '${transportCode.value}' and" when {
              "InlandOrBorder is NOT 'Border'" in {
                implicit val request = withRequestOfType(declarationType, withInlandModeOfTransportCode(transportCode.value))
                verifyBackButton(createView(), routes.InlandTransportDetailsController.displayPage)
              }
            }
          }
        }
      }
    }

    "DeclarationType is 'STANDARD'" should {

      List(Guernsey, Jersey).foreach { country =>
        val destinationCountry = withDestinationCountry(Country(Some(country)))

        "display a back button linking to the /inland-transport-details page" when {
          s"the destination country selected is '$country' and" when {
            "InlandOrBorder is NOT 'Border'" in {
              implicit val request = withRequestOfType(STANDARD, destinationCountry)
              verifyBackButton(createView(), routes.InlandTransportDetailsController.displayPage)
            }
          }
        }

        "display a back button linking to the /inland-or-border page" when {
          s"the destination country selected is '$country' and" when {
            "InlandOrBorder is 'Border'" in {
              implicit val request = withRequestOfType(STANDARD, destinationCountry, withInlandOrBorder(Some(Border)))
              verifyBackButton(createView(), routes.InlandOrBorderController.displayPage)
            }
          }
        }
      }
    }

    "DeclarationType is 'CLEARANCE'" should {

      "display the expected tariff details" in {
        implicit val request = withRequestOfType(CLEARANCE)
        verifyTariffDetails(createView(), "common")
      }

      "display a 'Back' button linking to the 'Departure Transport' page" when {
        nonPostalOrFTIModeOfTransportCodes.foreach { transportCode =>
          s"TransportLeavingTheBorder is '$transportCode'" in {
            val modeOfTransportCode = withBorderModeOfTransportCode(Some(transportCode))
            implicit val request = withRequestOfType(CLEARANCE, modeOfTransportCode)
            verifyBackButton(createView(), routes.DepartureTransportController.displayPage)
          }
        }
      }

      "display a 'Back' button linking to the 'Supervising Customs Office' page" when {
        postalOrFTIModeOfTransportCodes.foreach { transportCode =>
          s"TransportLeavingTheBorder is '${transportCode.value}'" in {
            val modeOfTransportCode = withBorderModeOfTransportCode(transportCode)
            implicit val request = withRequestOfType(CLEARANCE, modeOfTransportCode)
            verifyBackButton(createView(), routes.SupervisingCustomsOfficeController.displayPage)
          }
        }
      }

      "display a 'Back' button linking to the 'Supervising Customs Office' page with PC 1040 and APC 000" when {
        postalOrFTIModeOfTransportCodes.foreach { transportCode =>
          s"TransportLeavingTheBorder is '${transportCode.value}'" in {
            val modeOfTransportCode = withBorderModeOfTransportCode(transportCode)
            val item = withItem(anItem(withProcedureCodes(Some("1040"), Seq("000"))))
            implicit val request = withRequestOfType(CLEARANCE, item, modeOfTransportCode)
            verifyBackButton(createView(), routes.SupervisingCustomsOfficeController.displayPage)
          }
        }
      }
    }
  }

  private def verifyBackButton(view: Document, call: Call): Assertion = {
    val backButton = view.getElementById("back-link")
    backButton must containMessage(backToPreviousQuestionCaption)
    backButton must haveHref(call)
  }

  private def verifyTariffDetails(view: Document, key: String): Assertion = {
    val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
    tariffTitle.first must containMessage(s"tariff.expander.title.$key")

    val expected = removeLineBreakIfAny(messages("tariff.declaration.text", messages(s"tariff.declaration.expressConsignment.$key.linkText.0")))

    val tariffDetails = view.getElementsByClass("govuk-details__text").first
    removeBlanksIfAnyBeforeDot(tariffDetails.text) mustBe expected
  }
}
