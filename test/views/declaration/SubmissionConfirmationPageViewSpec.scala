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
import models.DeclarationType
import models.DeclarationType.DeclarationType
import models.responses.FlashKeys
import org.jsoup.nodes.Document
import play.api.mvc.Flash
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.submission_confirmation_page
import views.tags.ViewTest

@ViewTest
class SubmissionConfirmationPageViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new submission_confirmation_page(mainTemplate)
  private val realMessages = validatedMessages
  private val withoutFlash = new Flash(Map.empty)
  private def withFlash(devType: DeclarationType) = new Flash(Map(FlashKeys.lrn -> "some-lrn", FlashKeys.decType -> devType.toString))
  private def createView(flash: Flash): Document =
    page()(journeyRequest(), flash, realMessages)

  "Confirmation Page View on empty page" should {
    val view = createView(withoutFlash)

    "display header with default" in {
      val highlightBox = view.selectFirst("article>div.govuk-box-highlight")
      highlightBox must containText("Declaration has been submitted")
      highlightBox mustNot containText("The LRN is")
    }

    "display declaration status" in {
      val declarationInfo = view.getElementById("submissions-link")
      declarationInfo must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
    }

    "render start again button" in {
      val button = view.getElementsByClass("button").first()
      button must haveHref(controllers.routes.ChoiceController.displayPage().url)
      button must containText("Back to start")
    }
  }

  "Confirmation Page View when filled" should {

    "display header with declaration type Standard and LRN" in {
      val highlightBox = createView(withFlash(DeclarationType.STANDARD)).selectFirst("article>div.govuk-box-highlight")
      highlightBox must containText("Standard declaration has been submitted")
      highlightBox must containText("The LRN is some-lrn")
    }

    "display header with declaration type Simplified and LRN" in {
      val highlightBox = createView(withFlash(DeclarationType.SIMPLIFIED)).selectFirst("article>div.govuk-box-highlight")
      highlightBox must containText("Simplified declaration has been submitted")
      highlightBox must containText("The LRN is some-lrn")
    }

    "display header with declaration type Supplementary and LRN" in {
      val highlightBox = createView(withFlash(DeclarationType.SUPPLEMENTARY)).selectFirst("article>div.govuk-box-highlight")
      highlightBox must containText("Supplementary declaration has been submitted")
      highlightBox must containText("The LRN is some-lrn")
    }
  }
}
