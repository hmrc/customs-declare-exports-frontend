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
import controllers.util.SaveAndReturn
import forms.DeclarationPage
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.{ExporterDetails, IsExs}
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.is_exs

class IsExsViewSpec extends UnitViewSpec with ExportsTestData with CommonMessages with Stubs with Injector {

  private val page = instanceOf[is_exs]
  private def createView(form: Form[IsExs] = IsExs.form, navigationPage: DeclarationPage = IsExs)(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, navigationPage, form)(request, stubMessages())

  "Is Exs View" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())

      messages must haveTranslationFor("declaration.exs.title")
      messages must haveTranslationFor("declaration.exs.hint")
      messages must haveTranslationFor("declaration.exs.error")
    }

    onClearance { implicit request =>
      "display page title" in {

        createView().getElementsByTag("h1").text() mustBe "declaration.exs.title"
      }

      "display section header" in {

        createView().getElementById("section-header").text() must include("declaration.summary.parties.header")
      }

      "display radio button with Yes option" in {

        val view = createView()
        view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
        view.getElementsByAttributeValue("for", "code_yes").text() mustBe "site.yes"
      }
      "display radio button with No option" in {

        val view = createView()
        view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
        view.getElementsByAttributeValue("for", "code_no").text() mustBe "site.no"
      }

      "display 'Back' button that links to 'Exporter Details' page" when {

        "declarant is not an exporter" in {

          val view = createView(navigationPage = IsExs)
          val backButton = view.getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe routes.ExporterDetailsController.displayPage().url
        }
      }

      "display 'Back' button that links to 'Declarant Exporter' page" when {

        "declarant is an exporter" in {

          val view = createView(navigationPage = ExporterDetails)
          val backButton = view.getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe routes.DeclarantExporterController.displayPage().url
        }
      }

      "display 'Save and continue' button on page" in {

        val saveButton = createView().getElementById("submit")

        saveButton.text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {

        val saveButton = createView().getElementById("submit_and_return")

        saveButton.text() mustBe messages(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }
}
