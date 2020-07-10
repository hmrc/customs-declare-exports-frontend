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

import base.Injector
import controllers.declaration.routes
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import helpers.views.declaration.CommonMessages
import models.DeclarationType.DeclarationType
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.additionalInformtion.additional_information_required
import views.tags.ViewTest

@ViewTest
class AdditionalInformationRequiredViewSpec extends UnitViewSpec2 with ExportsTestData with CommonMessages with Stubs with Injector {
  val itemId = "a7sc78"
  private def form(journeyType: DeclarationType): Form[YesNoAnswer] = YesNoAnswer.form()
  private val additionalInfoReqPage = instanceOf[additional_information_required]
  private def createView(form: Form[YesNoAnswer])(implicit request: JourneyRequest[_]): Document = additionalInfoReqPage(Mode.Normal, itemId, form)

  "Additional Information Required View on empty page" should {

    "have correct message keys" in {

      messages must haveTranslationFor("declaration.additionalInformationRequired.title")
      messages must haveTranslationFor("supplementary.consignmentReferences.heading")
      messages must haveTranslationFor("declaration.additionalInformationRequired.error")
      messages must haveTranslationFor("declaration.additionalInformationRequired.hint")

    }
  }

  "Additional Information Required View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        createView(form(request.declarationType)).getElementsByTag("h1") must containMessageForElements(
          "declaration.additionalInformationRequired.title"
        )
      }

      "display section header" in {
        createView(form(request.declarationType)).getElementById("section-header") must containMessage("supplementary.items")
      }

      "display radio button with Yes option" in {
        val view = createView(form(request.declarationType))
        view.getElementById("required_Yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "required_Yes") must containMessageForElements("site.yes")
      }
      "display radio button with No option" in {
        val view = createView(form(request.declarationType))
        view.getElementById("required_No").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "required_No") must containMessageForElements("site.no")
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView(form(request.declarationType)).getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }

    }

  }

  "Additional Information Required View back link" should {

    onJourney(DeclarationType.STANDARD, DeclarationType.CLEARANCE, DeclarationType.SUPPLEMENTARY) { implicit request =>
      "display 'Back' button that links to the 'Commodity Measure' page" in {

        val view = createView(form(request.declarationType))
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.CommodityMeasureController.displayPage(Mode.Normal, itemId))
      }
    }

    onJourney(DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL) { implicit request =>
      "display 'Back' button that links to the 'Package Information' page" in {

        val view = createView(form(request.declarationType))
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.PackageInformationSummaryController.displayPage(Mode.Normal, itemId))
      }
    }

  }
}
