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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.CatOrDogFurDetails
import models.DeclarationType.STANDARD
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import views.declaration.spec.UnitViewSpec
import views.html.declaration.cat_or_dog_fur
import views.tags.ViewTest

@ViewTest
class CatOrDogFurViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[cat_or_dog_fur]
  private val itemId = "item1"
  private def createForm: Form[CatOrDogFurDetails] =
    CatOrDogFurDetails.form
  private def createView(form: Form[CatOrDogFurDetails])(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, form, itemId)(request, messages)

  // scalastyle:off
  def catOrDogFurDetailsView(catOrDogFurDetails: Option[CatOrDogFurDetails] = None)(implicit request: JourneyRequest[_]): Unit = {
    val form = CatOrDogFurDetails.form
    val view = createView(catOrDogFurDetails.fold(form)(form.fillAndValidate))

    "display page title" in {
      view.getElementById("title").text mustBe messages("declaration.catOrDogFur.header")
    }

    "display section-header" in {
      view.getElementById("section-header").text mustBe messages("declaration.section.4")
    }

    "display 'Back' button that links to 'Commodity Codes' page" in {
      val backButton = view.getElementById("back-link")
      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.CommodityDetailsController.displayPage(Mode.Normal, itemId))
    }

    "display body text" in {
      val body = view.getElementsByClass("govuk-body").get(0)
      body.text mustBe messages("declaration.catOrDogFur.body")
    }

    "display YesNo radio" in {
      Option(view.getElementById("code_yes")) mustBe defined
      Option(view.getElementById("code_no")) mustBe defined
    }

    "conditionally display sub radio" in {
      val yesNoAnswer = catOrDogFurDetails.map(_.yesNo).getOrElse("")

      if (yesNoAnswer == YesNoAnswers.yes) {
        view.getElementById("conditional-code_yes") mustNot haveClass("govuk-radios__conditional--hidden")
      } else {
        view.getElementById("conditional-code_yes") must haveClass("govuk-radios__conditional--hidden")
      }
    }

    "display details expander about page" in {
      view.getElementById("cat-or-dog-fur-expander") must containMessage("declaration.catOrDogFur.expander.title")
      view.getElementById("cat-or-dog-fur-expander") must containMessage("declaration.catOrDogFur.expander.body")
    }

    "display tariff expander with expected details" in {
      val tariffExpander = view.getElementById("tariffReference")
      val title = tariffExpander.getElementsByClass("govuk-details__summary-text")
      title.text mustBe messages(s"tariff.expander.title.common")

      val tariffDetails = tariffExpander.getElementsByClass("govuk-details__text").first

      (1 to 2).map { i =>
        val prefix = s"tariff.declaration.item.catOrDogFurDetails.$i"
        val expectedText = messages(s"$prefix.common.text", messages(s"$prefix.common.linkText.0"))
        val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)

        actualText must include(expectedText)
      }
    }
  }

  "Cat Or Dog Fur view with no cached details" should {
    onJourney(STANDARD) { implicit request =>
      behave like catOrDogFurDetailsView(None)
    }
  }

  "Cat or Dog fur view with filled form" should {
    onJourney(STANDARD) { implicit request =>
      val details = CatOrDogFurDetails(YesNoAnswers.yes, Some(CatOrDogFurDetails.EducationalOrTaxidermyPurposes))
      behave like catOrDogFurDetailsView(Some(details))
    }
  }

  "Cat or dog fur with invalid form" should {
    onJourney(STANDARD) { implicit request =>
      "show error when Yes selected but conditional radio not filled" in {
        val form = createForm.bind(Map("yesNo" -> "Yes", "purpose" -> ""))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithMessageKey("declaration.catOrDogFur.radios.purpose.empty")
      }

      "show error when nothing filled" in {
        val form = createForm.bind(Map("yesNo" -> "", "purpose" -> ""))
        val view = createView(form)

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithMessageKey("declaration.catOrDogFur.radios.yesNo.empty")
      }
    }
  }
}
