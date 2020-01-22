/*
 * Copyright 2020 HM Revenue & Customs
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

import base.TestHelper
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.declaration.{DeclarantDetails, EntityDetails, Eori}
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarant_details
import views.tags.ViewTest

@ViewTest
class DeclarantDetailsViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs {

  private val form: Form[DeclarantDetails] = DeclarantDetails.form()
  private val declarantDetailsPage = new declarant_details(mainTemplate)
  private def createView(form: Form[DeclarantDetails] = form): Document =
    declarantDetailsPage(Mode.Normal, form)(journeyRequest(), messages)

  "Declarant Details View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages("supplementary.declarant.title")
    }

    "display section header" in {

      val view = createView()

      view.getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("details_eori-label").text() mustBe messages("supplementary.declarant.eori.info")
      view.getElementById("details_eori").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Consignee Details' page" in {

      val view = declarantDetailsPage(Mode.Normal, form)(journeyRequest(), messages)
      val backButton = view.getElementById("back-link")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.ConsigneeDetailsController.displayPage().url
    }

    "display 'Save and continue' button on page" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() mustBe messages(saveAndReturnCaption)
      saveButton.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Declarant Details View with invalid input" should {

    "display error when EORI is empty" in {

      val view = createView(DeclarantDetails.form().fillAndValidate(DeclarantDetails(EntityDetails(Some(Eori("")), None))))

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_eori", "#details_eori")

      view.getElementById("error-message-details_eori-input").text() mustBe messages("supplementary.eori.empty")
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(DeclarantDetails(EntityDetails(Some(Eori(TestHelper.createRandomAlphanumericString(19))), None)))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_eori", "#details_eori")

      view.getElementById("error-message-details_eori-input").text() mustBe messages("supplementary.eori.error.format")
    }

  }

  "Declarant Details View when filled" should {

    "display data in EORI input" in {

      val form = DeclarantDetails.form().fill(DeclarantDetails(EntityDetails(Some(Eori("1234")), None)))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") mustBe "1234"
    }
  }
}
