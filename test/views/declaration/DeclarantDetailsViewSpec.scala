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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.Eori
import forms.declaration.{DeclarantDetails, EntityDetails}
import helpers.views.declaration.CommonMessages
import models.DeclarationType.DeclarationType
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarant_details
import views.tags.ViewTest

@ViewTest
class DeclarantDetailsViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private def form(journeyType: DeclarationType): Form[DeclarantDetails] = DeclarantDetails.form(journeyType)
  private val declarantDetailsPage = instanceOf[declarant_details]
  private def createView(form: Form[DeclarantDetails]): Document =
    declarantDetailsPage(Mode.Normal, form)(journeyRequest(), messages)

  "Declarant Details View on empty page" should {

    "have correct message keys" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("declaration.declarant.title")
      messages must haveTranslationFor("supplementary.summary.parties.header")
      messages must haveTranslationFor("declaration.declarant.eori.error.format")
      messages must haveTranslationFor("declaration.declarant.eori.empty")
    }
  }

  "Declarant Details View on empty page" should {

    onEveryDeclarationJourney { implicit request =>
      "display input label as page title" in {

        createView(form(request.declarationType)).getElementsByClass("govuk-label govuk-label--l").text() mustBe messages(
          "declaration.declarant.title"
        )
      }

      "display section header" in {

        createView(form(request.declarationType)).getElementById("section-header").text() must include(
          messages("supplementary.summary.parties.header")
        )
      }

      "display empty input with label for EORI" in {

        val view = createView(form(request.declarationType))

        view.getElementsByAttributeValue("for", "details_eori").text() mustBe messages("declaration.declarant.title")
        view.getElementById("details_eori").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Consignment References' page" in {

        val view = declarantDetailsPage(Mode.Normal, form(request.declarationType))(journeyRequest(), messages)
        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.ConsignmentReferencesController.displayPage().url
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView(form(request.declarationType)).getElementById("submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val saveButton = createView(form(request.declarationType)).getElementById("submit_and_return")
        saveButton.text() mustBe messages(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }

  "Declarant Details View with invalid input" should {

    onEveryDeclarationJourney { implicit request =>
      "display error when EORI is empty" in {

        val view = createView(DeclarantDetails.form(request.declarationType).fillAndValidate(DeclarantDetails(EntityDetails(Some(Eori("")), None))))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_eori")

        view.getElementsByClass("govuk-error-message").text() contains messages("declaration.declarant.eori.empty")
      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          DeclarantDetails
            .form(request.declarationType)
            .fillAndValidate(DeclarantDetails(EntityDetails(Some(Eori(TestHelper.createRandomAlphanumericString(19))), None)))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_eori")

        view.getElementsByClass("govuk-error-message").text() contains messages("declaration.declarant.eori.error.format")
      }
    }

  }

  "Declarant Details View when filled" should {

    onEveryDeclarationJourney { implicit request =>
      "display data in EORI input" in {

        val form = DeclarantDetails.form(request.declarationType).fill(DeclarantDetails(EntityDetails(Some(Eori("1234")), None)))
        val view = createView(form)

        view.getElementById("details_eori").attr("value") mustBe "1234"
      }
    }
  }
}
