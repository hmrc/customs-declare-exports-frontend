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

import base.ExportsTestData.pc1040
import base.Injector
import forms.common.YesNoAnswer.{form, YesNoAnswers}
import models.DeclarationType.STANDARD
import models.Mode.Normal
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.mvc.Call
import services.cache.ExportsTestHelper
import views.components.gds.Styles
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.additionalInformation.additional_information_required
import views.tags.ViewTest

@ViewTest
class AdditionalInformationRequiredViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val url = "/test"
  val call = Call("GET", url)

  override val typeAndViewInstance = (STANDARD, page(Normal, itemId, form(), call, pc1040)(_, _))

  val page = instanceOf[additional_information_required]

  def createView(implicit request: JourneyRequest[_]): Document =
    page(Normal, itemId, form(), call, pc1040)

  "Additional Information Required View on empty page" should {
    "have correct message keys" in {
      messages must haveTranslationFor("declaration.additionalInformationRequired.title")
      messages must haveTranslationFor("declaration.additionalInformationRequired.error")
      messages must haveTranslationFor("declaration.additionalInformationRequired.hint")
    }
  }

  "Additional Information Required View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display 'Back' button that links to the given url" in {
        val backButton = createView.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe url
      }

      "display page title" in {
        createView.getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.additionalInformationRequired.title")
      }

      "display section header" in {
        createView.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display radio button with Yes option" in {
        val view = createView
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }

      "display radio button with No option" in {
        val view = createView
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Save and continue' button on page" in {
        val saveButton = createView.getElementById("submit")
        saveButton must containMessage(saveAndContinueCaption)
      }
    }
  }
}
