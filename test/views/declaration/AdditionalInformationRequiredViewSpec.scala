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

package views.declaration

import base.Injector
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.Mode
import models.declaration.ProcedureCodesData
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.mvc.Call
import services.cache.ExportsTestData
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalInformation.additional_information_required
import views.tags.ViewTest

@ViewTest
class AdditionalInformationRequiredViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val itemId = "a7sc78"
  private val url = "/test"

  private val additionalInfoRequiredPage = instanceOf[additional_information_required]
  val maybeProcedureCodesData = Some(ProcedureCodesData(Some("1040"), Seq("000")))

  private def createView(implicit request: JourneyRequest[_]): Document =
    additionalInfoRequiredPage(Mode.Normal, itemId, YesNoAnswer.form(), Call("GET", url), maybeProcedureCodesData)

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
        backButton must containMessage(backCaption)
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
