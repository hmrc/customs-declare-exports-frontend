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
import forms.declaration.ProcedureCodes
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.procedure_codes
import views.tags.ViewTest
import config.AppConfig

@ViewTest
class ProcedureCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val appConfig = instanceOf[AppConfig]
  private val page = new procedure_codes(mainTemplate, appConfig)
  private val form: Form[ProcedureCodes] = ProcedureCodes.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[ProcedureCodes] = form, codes: Seq[String] = Seq.empty): Document =
    page(mode, "itemId", form, codes)(journeyRequest(), stubMessages())

  "Procedure Codes View on empty page" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.procedureCodes.title")
      messages must haveTranslationFor("supplementary.items")
      messages must haveTranslationFor("supplementary.procedureCodes.procedureCode.header")
      messages must haveTranslationFor("supplementary.procedureCodes.procedureCode.header.hint")
      messages must haveTranslationFor("supplementary.procedureCodes.additionalProcedureCode.header")
      messages must haveTranslationFor("supplementary.procedureCodes.additionalProcedureCode.header.hint")
    }

    "display page title" in {
      view.getElementById("title").text() mustBe "supplementary.procedureCodes.title"
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("supplementary.items")
    }

    "display empty input with label for Procedure Code" in {
      view.getElementById("procedureCode-label").text() mustBe "supplementary.procedureCodes.procedureCode.header"
      view.getElementById("procedureCode-hint").text() mustBe "supplementary.procedureCodes.procedureCode.header.hint"
      view.getElementById("procedureCode").attr("value") mustBe empty
    }

    "display empty input with label for Additional Procedure Codes" in {
      view
        .getElementById("additionalProcedureCode-label")
        .text() mustBe "supplementary.procedureCodes.additionalProcedureCode.header"
      view
        .getElementById("additionalProcedureCode-hint")
        .text() mustBe "supplementary.procedureCodes.additionalProcedureCode.header.hint"
      view.getElementById("additionalProcedureCode").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Export Items' page" in {

      val backButton = view.getElementById("back-link")

      backButton.text() mustBe "site.back"
      backButton.getElementById("back-link") must haveHref(controllers.declaration.routes.ItemsSummaryController.displayPage(Mode.Normal))
    }

    "display both 'Add' and 'Save and continue' button on page" in {
      val addButton = view.getElementById("add")
      addButton.text() mustBe "site.add supplementary.procedureCodes.additionalProcedureCode.add.hint"

      val saveButton = view.getElementById("submit")
      saveButton.text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() mustBe "site.save_and_come_back_later"
    }
  }

  "Procedure Codes View when filled" should {

    "display data in Procedure Code input" in {

      val view = createView(form = ProcedureCodes.form().fill(ProcedureCodes(Some("Test"), Some(""))))

      view.getElementById("procedureCode").attr("value") mustBe "Test"
      view.getElementById("additionalProcedureCode").attr("value") mustBe empty
    }

    "display data in Additional Procedure Code input" in {

      val view = createView(form = ProcedureCodes.form().fill(ProcedureCodes(Some(""), Some("Test"))))

      view.getElementById("procedureCode").attr("value") mustBe empty
      view.getElementById("additionalProcedureCode").attr("value") mustBe "Test"
    }

    "display data in both inputs" in {

      val view = createView(form = ProcedureCodes.form().fill(ProcedureCodes(Some("Test"), Some("Test"))))

      view.getElementById("procedureCode").attr("value") mustBe "Test"
      view.getElementById("additionalProcedureCode").attr("value") mustBe "Test"
    }
  }
}
