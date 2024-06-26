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

package views.section2

import base.{Injector, TestHelper}
import controllers.section2.routes.RepresentativeAgentController
import forms.section2.RepresentativeEntity
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.section2.representative_details_entity
import views.tags.ViewTest

@ViewTest
class RepresentativeDetailsEntityViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[representative_details_entity]
  private val form: Form[RepresentativeEntity] = RepresentativeEntity.form
  private def createView(form: Form[RepresentativeEntity] = form): Document =
    page(form)(journeyRequest(), messages)

  "Representative Details Entity View on empty page" should {
    val view = createView()

    "display page title" in {
      view.getElementsByTag("h1").first() must containMessage("declaration.representative.entity.title")
    }

    "display empty input" in {
      view.getElementById("details_eori").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Representative for another agent' page" in {
      val backButton = view.getElementById("back-link")

      backButton must containMessage("site.backToPreviousQuestion")
      backButton.getElementById("back-link") must haveHref(RepresentativeAgentController.displayPage)
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }

  "Representative Details Entity View for invalid input" should {

    "display errors when EORI is incorrect" in {
      val view = createView(form =
        RepresentativeEntity.form
          .bind(Map("details.eori" -> TestHelper.createRandomAlphanumericString(50)))
      )

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#details_eori")

      view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
    }

    "display errors when EORI is missing" in {
      val view = createView(form =
        RepresentativeEntity.form
          .bind(Map("details.eori" -> ""))
      )

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#details_eori")

      view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
    }
  }

  "Representative Details Entity View when filled" should {

    "display data in EORI input" in {
      val form = RepresentativeEntity.form
        .bind(Map("details.eori" -> "1234"))
      val view = createView(form = form)

      view.getElementById("details_eori").attr("value") must be("1234")
    }
  }
}
