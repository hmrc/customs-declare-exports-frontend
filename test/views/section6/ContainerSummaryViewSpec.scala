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

package views.section6

import base.Injector
import controllers.helpers.TransportSectionHelper.{postalOrFTIModeOfTransportCodes, Guernsey, Jersey}
import controllers.section6.routes._
import forms.common.YesNoAnswer.form
import forms.common.{Country, YesNoAnswer}
import forms.section6.InlandOrBorder.Border
import forms.section6.Seal
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.declaration.Container
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import views.html.section6.container_summary
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class ContainerSummaryViewSpec extends PageWithButtonsSpec with Injector {

  val containerId = "212374"
  val sealId = "76434574"
  val container = Container(1, containerId, List(Seal(1, sealId)))

  val page = instanceOf[container_summary]

  override val typeAndViewInstance = (STANDARD, page(form(), List(container))(_, _))

  def createView(frm: Form[YesNoAnswer] = form(), containers: Seq[Container] = List(container))(implicit request: JourneyRequest[_]): Document =
    page(frm, containers)(request, messages)

  "Transport Containers Summary View" should {
    val view = createView()

    "display page title for one container" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.title")
      view.title() must include(messages("declaration.transportInformation.containers.title"))
    }

    "display page title for multiple containers" in {
      val multiContainerView = createView(containers = List(container, container))
      multiContainerView.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.multiple.title", 2)
      multiContainerView.title() must include(messages("declaration.transportInformation.containers.multiple.title", 2))
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display table with headers" in {
      val tableHead = view.getElementsByTag("th")

      tableHead.get(0).text() mustBe messages("declaration.transportInformation.containerId.title")
      tableHead.get(1).text() mustBe messages("declaration.seal.summary.heading")
    }

    "have visually hidden headers for Change and Remove links" in {
      val tableHead = view.getElementsByTag("th")

      tableHead.get(2).text() mustBe messages("site.change.header")
      tableHead.get(3).text() mustBe messages("site.remove.header")
    }

    "display summary of container with seals" in {
      view.getElementById("containers-row0-container").text() must be(containerId)
      view.getElementById("containers-row0-seals").text() must be(sealId)
    }

    "display summary of container with no seals" in {
      val view = createView(containers = List(Container(1, containerId, Seq.empty)))

      view.getElementById("containers-row0-container").text() must be(containerId)
      view.getElementById("containers-row0-seals") must containMessage("declaration.seal.summary.noSeals")
    }

    "display 'Back' button that links to the 'ExpressConsignment' page" when {
      List(STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE).foreach { declarationType =>
        s"declaration's type is $declarationType" in {
          implicit val request = withRequestOfType(declarationType)
          verifyBackButton(createView(), ExpressConsignmentController.displayPage)
        }
      }
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Transport Containers Add First View" when {
    "declaration's type is SUPPLEMENTARY" should {

      "display 'Back' button that links to the 'Transport Country' page" in {
        implicit val request = withRequestOfType(SUPPLEMENTARY)
        verifyBackButton(createView(), TransportCountryController.displayPage)
      }

      List(Guernsey, Jersey).foreach { country =>
        val destinationCountry = withDestinationCountry(Country(Some(country)))

        "display a back button linking to the /inland-transport-details page" when {
          s"the destination country selected is '$country' and" when {
            "InlandOrBorder is NOT 'Border'" in {
              implicit val request = withRequestOfType(SUPPLEMENTARY, destinationCountry)
              verifyBackButton(createView(), InlandTransportDetailsController.displayPage)
            }
          }
        }

        "display a back button linking to the /inland-or-border page" when {
          s"the destination country selected is '$country' and" when {
            "InlandOrBorder is 'Border'" in {
              implicit val request = withRequestOfType(SUPPLEMENTARY, destinationCountry, withInlandOrBorder(Some(Border)))
              verifyBackButton(createView(), InlandOrBorderController.displayPage)
            }
          }
        }
      }

      postalOrFTIModeOfTransportCodes.foreach { transportCode =>
        "display a back button linking to the /inland-or-border page" when {
          s"TransportLeavingTheBorder is '${transportCode.value}' and" when {
            "InlandOrBorder is 'Border'" in {
              val modeOfTransportCode = withTransportLeavingTheBorder(transportCode)
              implicit val request = withRequestOfType(SUPPLEMENTARY, modeOfTransportCode, withInlandOrBorder(Some(Border)))
              verifyBackButton(createView(), InlandOrBorderController.displayPage)
            }
          }
        }

        "display a back button linking to the /inland-transport-details page" when {
          s"InlandTransportDetails is '${transportCode.value}' and" when {
            "InlandOrBorder is NOT 'Border'" in {
              implicit val request = withRequestOfType(SUPPLEMENTARY, withInlandModeOfTransportCode(transportCode.value))
              verifyBackButton(createView(), InlandTransportDetailsController.displayPage)
            }
          }
        }
      }
    }
  }

  "Transport Containers Summary View for invalid input" should {
    "display error if nothing is entered" in {
      val view = createView(form().bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#code_yes")

      view must containErrorElementWithMessageKey("error.yesNo.required")
    }
  }

  private def verifyBackButton(view: Document, call: Call): Assertion = {
    val backButton = view.getElementById("back-link")
    backButton must containMessage(backToPreviousQuestionCaption)
    backButton must haveHref(call)
  }
}
