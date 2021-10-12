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

package views.declaration.confirmation

import base.Injector
import controllers.declaration.routes.ConfirmationController
import views.declaration.spec.UnitViewSpec
import views.html.declaration.confirmation.holding_confirmation_page
import views.tags.ViewTest

@ViewTest
class HoldingConfirmationViewSpec extends UnitViewSpec with Injector {

  private val holdingConfirmationPage = instanceOf[holding_confirmation_page]

  "Declaration Holder View" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = holdingConfirmationPage()(request, messages)

      "wrap all elements by a form" in {
        val htmlForm = view.getElementsByTag("form").get(0)
        htmlForm.attr("method") mustBe "GET"
        htmlForm.attr("action") mustBe ConfirmationController.displaySubmissionConfirmation.url
      }

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.confirmation.holding.title")
      }

      "display the expected paragraph body" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.confirmation.holding.paragraph")
      }

      "display a 'Continue' button" in {
        val continueButton = view.getElementById("continue")
        continueButton.text mustBe messages("site.continue")
        continueButton.attr("class") mustBe "govuk-button"
      }
    }
  }
}
