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
import controllers.declaration.routes._
import forms.DeclarationPage
import forms.declaration.RepresentativeStatus
import forms.declaration.RepresentativeStatus.StatusCodes
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.html.declaration.representative_details_status
import views.tags.ViewTest

@ViewTest
class RepresentativeDetailsStatusViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[representative_details_status]
  private val form: Form[RepresentativeStatus] = RepresentativeStatus.form()
  private def createView(
    mode: Mode = Mode.Normal,
    navigationForm: DeclarationPage = RepresentativeStatus,
    form: Form[RepresentativeStatus] = form
  ): Document =
    page(mode, navigationForm, form)(journeyRequest(), messages)

  "Representative Details Status View on empty page" should {
    val view = createView()

    "display page title" in {
      view.getElementsByClass(Styles.gdsPageLegend).first() must containMessage("declaration.representative.status.title")
    }

    "display two radio buttons with description (not selected)" in {

      val view = createView(form = RepresentativeStatus.form().fill(RepresentativeStatus(None)))

      view.getElementsByClass("govuk-radios__item").size mustBe 2

      val optionDirect = view.getElementById("2")
      optionDirect.attr("checked") mustBe empty

      val optionIndirect = view.getElementById("3")
      optionIndirect.attr("checked") mustBe empty

    }

    "display 'Back' button that links to 'Representative Eori' page" in {

      val backButton = view.getElementById("back-link")

      backButton must containMessage("site.back")
      backButton.getElementById("back-link") must haveHref(RepresentativeEntityController.displayPage(Mode.Normal))
    }

    val createViewWithMode: Mode => Document = mode => createView(mode = mode)
    checkAllSaveButtonsAreDisplayed(createViewWithMode)
  }

  "Representative Details Status View for invalid input" should {

    "display errors when status is incorrect" in {

      val view = createView(form =
        RepresentativeStatus
          .form()
          .bind(Map("statusCode" -> "invalid"))
      )

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", s"#${StatusCodes.DirectRepresentative}")

      view.getElementsByClass("#govuk-error-message").text() contains messages("choicePage.input.error.empty")
    }
  }

  "Representative Details Status View when filled" should {

    "display data" in {

      val form = RepresentativeStatus
        .form()
        .bind(Map("statusCode" -> "2"))
      val view = createView(form = form)

      view.getElementById("2").getElementsByAttribute("checked").size() mustBe 1
    }

  }
}
