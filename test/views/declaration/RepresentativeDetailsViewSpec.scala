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

import base.{Injector, TestHelper}
import forms.declaration.RepresentativeDetails
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.representative_details
import views.tags.ViewTest

@ViewTest
class RepresentativeDetailsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[representative_details]
  private val form: Form[RepresentativeDetails] = RepresentativeDetails.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[RepresentativeDetails] = form): Document =
    page(mode, form)(journeyRequest(), stubMessages())

  "Representative Details View on empty page" should {
    val view = createView()

    "display page title" in {
      view.getElementById("title").text() must be("supplementary.representative.title")
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(form = RepresentativeDetails.form().fill(RepresentativeDetails(None, None)))

      view.getElementsByClass("govuk-radios__item").size mustBe 2

      val optionDirect = view.getElementById("2")
      optionDirect.attr("checked") mustBe empty

      val optionIndirect = view.getElementById("3")
      optionIndirect.attr("checked") mustBe empty

    }

    "display empty input" in {
      view.getElementById("details_eori").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Consignee Details' page" in {

      val backButton = view.getElementById("back-link")

      backButton.text() must be("site.back")
      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage(Mode.Normal))
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementsByClass("govuk-button").first()
      saveButton.text() must be("site.save_and_continue")
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementsByClass("govuk-link govuk-link--no-visited-state").first()
      saveAndReturnButton.text() must be("site.save_and_come_back_later")
    }
  }

  "Representative Details View for invalid input" can {

    "status is not selected" when {

      "display errors when status is empty and has eori" in {

        val view = createView(
          form = RepresentativeDetails.adjustErrors(
            RepresentativeDetails
              .form()
              .bind(Map("details.eori" -> "GB12345678912345", "statusCode" -> ""))
          )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#")

        view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
      }

      "display errors when EORI is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(Map("details.eori" -> TestHelper.createRandomAlphanumericString(50), "statusCode" -> ""))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details.eori")

        view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
      }

    }

    "status is selected" when {

      "display errors when EORI is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(Map("details.eori" -> TestHelper.createRandomAlphanumericString(50), "statusCode" -> "2"))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details.eori")

        view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
      }

    }
  }

  "Representative Details View when filled" when {

    "direct is selected" should {

      "display data in EORI input" in {

        val form = RepresentativeDetails
          .form()
          .bind(Map("details.eori" -> "1234", "statusCode" -> "2"))
        val view = createView(form = form)

        view.getElementById("details_eori").attr("value") must be("1234")
        view.getElementById("2").getElementsByAttribute("checked").size() mustBe 1
      }

    }

    "indirect is selected" should {

      "display data in EORI input" in {

        val form = RepresentativeDetails
          .form()
          .bind(Map("details.eori" -> "1234", "statusCode" -> "3"))
        val view = createView(form = form)

        view.getElementById("details_eori").attr("value") must be("1234")
        view.getElementById("3").getElementsByAttribute("checked").size() mustBe 1
      }

    }
  }
}
