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

import base.{Injector, MockTaggedAuthCodes}
import controllers.declaration.routes.{DestinationCountryController, LocationOfGoodsController}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.officeOfExit.OfficeOfExit
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import services.cache.ExportsTestHelper
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit
import views.tags.ViewTest

@ViewTest
class OfficeOfExitViewSpec extends UnitViewSpec with ExportsTestHelper with Injector with MockTaggedAuthCodes {

  private val page: office_of_exit = instanceOf[office_of_exit]

  private def createView(form: Form[OfficeOfExit] = OfficeOfExit.form): Document =
    page(form)(journeyRequest(), messages)

  "Office of Exit View" should {
    val view = createView()
    onEveryDeclarationJourney() { implicit request =>
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

      "display 'Back' button that links to 'Location of Goods' page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage("site.backToPreviousQuestion")
        backButton.getElementById("back-link") must haveHref(LocationOfGoodsController.displayPage)
      }

      checkAllSaveButtonsAreDisplayed(createView())

      "handle invalid input" should {

        "display errors when all inputs are incorrect" in {
          val data = OfficeOfExit("123456")
          val form = OfficeOfExit.form.fillAndValidate(data)
          val view = createView(form)

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
          val view = createView(form)

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

    "display 'Back' button that links to 'Destination Country' page" in {
      val modifier = withDeclarationHolders(Some(taggedAuthCodes.codesSkippingLocationOfGoods.head))
      implicit val request: JourneyRequest[AnyContent] = withRequest(SUPPLEMENTARY_EIDR, modifier)
      val skipLocationOfGoodsView = page(OfficeOfExit.form)

      val backButton = skipLocationOfGoodsView.getElementById("back-link")

      backButton must containMessage("site.backToPreviousQuestion")
      backButton.getElementById("back-link") must haveHref(DestinationCountryController.displayPage)
    }
  }
}
