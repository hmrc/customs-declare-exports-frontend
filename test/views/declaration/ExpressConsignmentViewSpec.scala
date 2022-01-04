/*
 * Copyright 2022 HM Revenue & Customs
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
import base.{Injector, MockAuthAction}
import controllers.declaration.routes
import controllers.helpers.SupervisingCustomsOfficeHelperSpec.skipDepartureTransportPageCodes
import forms.common.YesNoAnswer
import forms.declaration.ModeOfTransportCode.meaningfulModeOfTransportCodes
import models.DeclarationType._
import models.Mode
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

  private def createView(mode: Mode = Mode.Normal, form: Form[YesNoAnswer] = form)(implicit request: JourneyRequest[_]): Document =
    page(mode, form)(request, messages)

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
        Option(radios.first.getElementById("code_yes")) must be('defined)
        Option(radios.last.getElementById("code_no")) must be('defined)
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

      "display 'Save and continue' button" in {
        val saveButton = view.getElementById("submit")
        saveButton must containMessage("site.save_and_continue")
      }

      "display 'Save and come back later' link" in {
        val saveAndReturnButton = view.getElementById("submit_and_return")
        saveAndReturnButton must containMessage("site.save_and_come_back_later")
      }
    }

    onStandard { implicit request =>
      val view: Document = createView()

      "display 'Back' button to the 'Border Transport' page" in {
        verifyBackButton(view, routes.BorderTransportController.displayPage(Mode.Normal))
      }

      "display the expected tariff details" in {
        verifyTariffDetails(view, "common")
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view: Document = createView()

      "display 'Back' button to the 'Supervising Customs Office' page" in {
        verifyBackButton(view, routes.SupervisingCustomsOfficeController.displayPage(Mode.Normal))
      }

      "display the expected tariff details" in {
        verifyTariffDetails(view, "common")
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL)(aDeclaration(withItem(itemWith1040AsPC))) { implicit request =>
      val view: Document = createView()

      "display 'Back' button to the 'Items Summary' page" when {
        "all declaration's items have '1040' as Procedure code and '000' as unique Additional Procedure code" in {
          verifyBackButton(view, routes.ItemsSummaryController.displayItemsSummaryPage())
        }
      }
    }

    onClearance { implicit request =>
      "display 'Back' button to the 'Supervising Customs Office' page" when {
        skipDepartureTransportPageCodes.foreach { modeOfTransportCode =>
          val cachedDec = aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(Some(modeOfTransportCode)))
          val requestWithUpdatedDec = new JourneyRequest(getAuthenticatedRequest(), cachedDec)
          val view: Document = createView()(requestWithUpdatedDec)

          s"transportLeavingBoarderCode is ${modeOfTransportCode}" in {
            verifyBackButton(view, routes.SupervisingCustomsOfficeController.displayPage(Mode.Normal))
          }
        }
      }

      "display 'Back' button to the 'Departure Transport' page" when {
        meaningfulModeOfTransportCodes
          .filter(!skipDepartureTransportPageCodes.contains(_))
          .foreach { modeOfTransportCode =>
            val cachedDec = aDeclaration(withType(request.declarationType), withBorderModeOfTransportCode(Some(modeOfTransportCode)))
            val requestWithUpdatedDec = new JourneyRequest(getAuthenticatedRequest(), cachedDec)
            val view: Document = createView()(requestWithUpdatedDec)

            s"transportLeavingBoarderCode is ${modeOfTransportCode}" in {
              verifyBackButton(view, routes.DepartureTransportController.displayPage(Mode.Normal))
            }
          }
      }

      "display the expected tariff details" in {
        val view: Document = createView()
        verifyTariffDetails(view, "clearance")
      }
    }
  }

  private def verifyBackButton(view: Document, call: Call): Assertion = {
    val backButton = view.getElementById("back-link")
    backButton must containMessage(backCaption)
    backButton must haveHref(call)
  }

  private def verifyTariffDetails(view: Document, key: String): Assertion = {
    val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
    tariffTitle.first must containMessage(s"tariff.expander.title.$key")

    val expected = removeLineBreakIfAny(
      messages(s"tariff.declaration.expressConsignment.$key.text", messages(s"tariff.declaration.expressConsignment.$key.linkText.0"))
    )

    val tariffDetails = view.getElementsByClass("govuk-details__text").first
    removeBlanksIfAnyBeforeDot(tariffDetails.text) mustBe expected
  }
}
