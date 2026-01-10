/*
 * Copyright 2024 HM Revenue & Customs
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

import base.{Injector, MockAuthAction}
import controllers.helpers.TransportSectionHelper.{postalOrFTIModeOfTransportCodes, Guernsey, Jersey}
import controllers.section6.routes._
import forms.common.Country
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section6.ContainerFirst
import forms.section6.ContainerFirst.HasContainerAnswers
import forms.section6.InlandOrBorder.Border
import models.DeclarationType
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import services.cache.ExportsTestHelper
import tools.Stubs
import views.common.UnitViewSpec
import views.helpers.CommonMessages
import views.html.section6.container_add_first
import views.tags.ViewTest

@ViewTest
class ContainerAddFirstViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with CommonMessages with Injector with MockAuthAction {

  private val form: Form[ContainerFirst] = ContainerFirst.form
  private val page = instanceOf[container_add_first]

  private def createView(form: Form[ContainerFirst] = form)(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  "Transport Containers Add First View" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.transportInformation.containers.first.title")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display radio button with Yes option" in {
      view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
      view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("declaration.transportInformation.containers.yes")
    }

    "display radio button with No option" in {
      view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
      view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("declaration.transportInformation.containers.no")
    }

    "display paragraph text" in {
      val para = view.getElementsByClass("govuk-body")
      val expected = Seq(messages("declaration.transportInformation.containers.paragraph")).mkString(" ")
      para.get(0) must containText(expected)
    }

    "display bullet list text" in {
      val bullets = view.getElementsByClass("govuk-list--bullet")
      val expected = Seq(
        messages("declaration.transportInformation.containers.bullet1"),
        messages("declaration.transportInformation.containers.bullet2"),
        messages("declaration.transportInformation.containers.bullet3")
      ).mkString(" ")
      bullets.get(0) must containText(expected)
    }

    "display 'Back' button that links to the 'ExpressConsignment' page" when {
      List(STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE).foreach { declarationType =>
        s"declaration's type is $declarationType" in {
          implicit val request = withRequestOfType(declarationType)
          verifyBackButton(createView(), ExpressConsignmentController.displayPage)
        }
      }
    }

    "not contain opt-not-to-declare radio" when {
      DeclarationType.allDeclarationTypes.filterNot(_ == SUPPLEMENTARY).foreach { declarationType =>
        s"declaration type is $declarationType" in {
          val view = createView()(withRequestOfType(declarationType))
          view.getElementById("code_optNotToDeclare") mustBe null
        }
      }
    }

    checkAllSaveButtonsAreDisplayed(view)
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

      "contain opt-not-to-declare option" in {
        val view = createView()(withRequestOfType(SUPPLEMENTARY))
        view.getElementById("code_optNotToDeclare").attr("value") mustBe HasContainerAnswers.optNotToDeclare
        view.getElementsByAttributeValue("for", "code_optNotToDeclare") must containMessageForElements(
          "declaration.transportInformation.containers.optNotToDeclare"
        )
        view.getElementsByAttributeValue("for", "id") must containMessageForElements("declaration.transportInformation.supplementary.containerId")
      }
    }
  }

  "Transport Containers Add View" should {
    "display errors for invalid input" in {
      val view = createView(ContainerFirst.form.fillAndValidate(ContainerFirst(Some("abc123@#"), HasContainerAnswers.yes)))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")
      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.invalid")
    }

    "display errors for invalid length" in {
      val view = createView(ContainerFirst.form.fillAndValidate(ContainerFirst(Some("123456789012345678"), HasContainerAnswers.yes)))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#id")
      view must containErrorElementWithMessageKey("declaration.transportInformation.containerId.error.length")
    }
  }

  "Transport Containers Add View when filled" should {
    "display data in Container ID input" in {
      val view = createView(ContainerFirst.form.fill(ContainerFirst(Some("Test"), HasContainerAnswers.yes)))

      view.getElementsByAttributeValue("for", "id").get(0) must containMessage("declaration.transportInformation.containerId")
      view.getElementById("id").attr("value") must be("Test")
    }
  }

  private def verifyBackButton(view: Document, call: Call): Assertion = {
    val backButton = view.getElementById("back-link")
    backButton must containMessage(backToPreviousQuestionCaption)
    backButton must haveHref(call)
  }
}
