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

import base.{Injector, MockTaggedCodes}
import controllers.declaration.routes.{DestinationCountryController, LocationOfGoodsController, RoutingCountriesController}
import forms.section1.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.officeOfExit.OfficeOfExit
import models.DeclarationType._
import models.declaration.DeclarationStatus.AMENDMENT_DRAFT
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.api.data.Form
import play.api.mvc.Call
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit
import views.tags.ViewTest

@ViewTest
class OfficeOfExitViewSpec extends UnitViewSpec with ExportsTestHelper with Injector with MockTaggedCodes {

  private val page: office_of_exit = instanceOf[office_of_exit]

  private def createView(form: Form[OfficeOfExit] = OfficeOfExit.form)(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  "Office of Exit View" should {
    val view = createView()(journeyRequest())

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.3")
    }

    "display page title" in {
      view.getElementsByTag("h1") must containMessageForElements("declaration.officeOfExit.title")
    }

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.officeOfExit.title")
      messages must haveTranslationFor("declaration.officeOfExit.hint")
      messages must haveTranslationFor("declaration.officeOfExit.empty")
      messages must haveTranslationFor("declaration.officeOfExit.length")
      messages must haveTranslationFor("declaration.officeOfExit.specialCharacters")
    }

    "display office of exit question" in {
      view.getElementById("officeId-hint") must containMessage("declaration.officeOfExit.hint")
      view.getElementById("officeId").attr("value") mustBe empty
    }

    "display a 'Back' button that links to /location-of-goods" in {
      verifyBackButton(view, LocationOfGoodsController.displayPage)
    }

    checkAllSaveButtonsAreDisplayed(view)

    "handle invalid input" should {

      "display errors when all inputs are incorrect" in {
        val data = OfficeOfExit("123456")
        val form = OfficeOfExit.form.fillAndValidate(data)
        val view = createView(form)(journeyRequest())

        view.getElementsByClass("govuk-error-summary__title").first() must containMessage("error.summary.title")

        view
          .getElementsByClass("govuk-list govuk-error-summary__list")
          .get(0)
          .getElementsByTag("li")
          .get(0) must containMessage("declaration.officeOfExit.length")
        view.getElementById("error-message-officeId-input") must containMessage("declaration.officeOfExit.length")
      }

      "display errors when office of exit contains special characters" in {
        val data = OfficeOfExit("12#$%^78")
        val form = OfficeOfExit.form.fillAndValidate(data)
        val view = createView(form)(journeyRequest())

        view.getElementsByClass("govuk-error-summary__title").first() must containMessage("error.summary.title")

        view
          .getElementsByClass("govuk-list govuk-error-summary__list")
          .get(0)
          .getElementsByTag("li")
          .get(0) must containMessage("declaration.officeOfExit.specialCharacters")
        view.getElementById("error-message-officeId-input") must containMessage("declaration.officeOfExit.specialCharacters")
      }
    }
  }

  "Office of Exit View" when {

    "the declaration is under amendment" should {

      "display a 'Back' button that links to /countries-of-routing" when {
        List(STANDARD, SIMPLIFIED, OCCASIONAL).foreach { declarationType =>
          s"the declaration's type is $declarationType" in {
            val view = createView()(withRequestOfType(declarationType, withStatus(AMENDMENT_DRAFT)))
            verifyBackButton(view, RoutingCountriesController.displayRoutingCountry)
          }
        }
      }

      "display a 'Back' button that links to /destination-country" when {
        List(SUPPLEMENTARY, CLEARANCE).foreach { declarationType =>
          s"the declaration's type is $declarationType" in {
            val view = createView()(withRequestOfType(declarationType, withStatus(AMENDMENT_DRAFT)))
            verifyBackButton(view, DestinationCountryController.displayPage)
          }
        }
      }
    }

    "the declaration has SUPPLEMENTARY_EIDR as additional declaration type and" when {
      "the auth codes require to skip the '/location-of-goods' page" should {
        "display a 'Back' button that links to /destination-country" in {
          val authHolders = withAuthorisationHolders(Some(taggedAuthCodes.codesSkippingLocationOfGoods.head))
          val view = createView()(withRequest(SUPPLEMENTARY_EIDR, authHolders))
          verifyBackButton(view, DestinationCountryController.displayPage)
        }
      }
    }
  }

  private def verifyBackButton(view: Document, call: Call): Assertion = {
    val backButton = view.getElementById("back-link")
    backButton must containMessage("site.backToPreviousQuestion")
    backButton.getElementById("back-link") must haveHref(call)
  }
}
