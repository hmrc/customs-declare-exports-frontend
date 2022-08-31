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

import base.Injector
import controllers.declaration.routes.{DestinationCountryController, LocationOfGoodsController}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.declarationHolder.AuthorizationTypeCodes.codeThatSkipLocationOfGoods
import forms.declaration.officeOfExit.OfficeOfExit
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.AnyContent
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.office_of_exit
import views.tags.ViewTest

@ViewTest
class OfficeOfExitViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page: office_of_exit = instanceOf[office_of_exit]

  private def createView(form: Form[OfficeOfExit] = OfficeOfExit.form, mode: Mode = Mode.Normal): Document =
    page(mode, form)(journeyRequest(), messages)

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
        backButton.getElementById("back-link") must haveHref(LocationOfGoodsController.displayPage(Mode.Normal))
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

      "handle invalid input" should {

        "display errors when all inputs are incorrect" in {
          val data = OfficeOfExit("123456")
          val form = OfficeOfExit.form.fillAndValidate(data)
          val view = createView(form)

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

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

          view.getElementById("error-summary-title") must containMessage("error.summary.title")

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
      val modifier = withDeclarationHolders(Some(codeThatSkipLocationOfGoods))
      implicit val request: JourneyRequest[AnyContent] = withRequest(SUPPLEMENTARY_EIDR, modifier)
      val skipLocationOfGoodsView = page(Mode.Normal, OfficeOfExit.form)

      val backButton = skipLocationOfGoodsView.getElementById("back-link")

      backButton must containMessage("site.backToPreviousQuestion")
      backButton.getElementById("back-link") must haveHref(DestinationCountryController.displayPage(Mode.Normal))
    }
  }
}
