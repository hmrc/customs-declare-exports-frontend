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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{DeclarantIsExporter, IsExs}
import models.Mode
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import services.cache.ExportsTestData
import tools.Stubs
import views.components.gds.Styles
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.is_exs

class IsExsViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val page = instanceOf[is_exs]
  private def createView()(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, IsExs.form)(request, messages)

  "Is Exs View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.exs.title")
      messages must haveTranslationFor("declaration.exs.hint")
      messages must haveTranslationFor("declaration.exs.error")
    }

    onClearance { implicit request =>
      "display page title" in {

        createView().getElementsByClass(Styles.gdsPageLegend) must containMessageForElements("declaration.exs.title")
      }

      "display section header" in {

        createView().getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display radio button with Yes option" in {

        val view = createView()
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
      }
      "display radio button with No option" in {

        val view = createView()
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
      }

      "display 'Back' button that links to 'Exporter Details' page" when {

        "declarant is not an exporter" in {

          val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.no)))
          val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

          val view = createView()(requestWithCachedParties)
          val backButton = view.getElementById("back-link")

          backButton must containMessage(backCaption)
          backButton.attr("href") mustBe routes.ExporterDetailsController.displayPage().url
        }
      }

      "display 'Back' button that links to 'Declarant Exporter' page" when {

        "declarant is an exporter" in {

          val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.yes)))
          val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

          val view = createView()(requestWithCachedParties)
          val backButton = view.getElementById("back-link")

          backButton must containMessage(backCaption)
          backButton.attr("href") mustBe routes.DeclarantExporterController.displayPage().url
        }
      }

      "display 'Save and continue' button on page" in {

        val saveButton = createView().getElementById("submit")

        saveButton must containMessage(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {

        val saveButton = createView().getElementById("submit_and_return")

        saveButton must containMessage(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }
}
