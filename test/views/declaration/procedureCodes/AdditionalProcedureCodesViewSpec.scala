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

package views.declaration.procedureCodes

import base.Injector
import models.codes.{ProcedureCode, AdditionalProcedureCode => AdditionalProcedureCodeModel}
import forms.declaration.procedurecodes.AdditionalProcedureCode
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.procedureCodes.additional_procedure_codes
import views.tags.ViewTest

@ViewTest
class AdditionalProcedureCodesViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[additional_procedure_codes]
  private val form: Form[AdditionalProcedureCode] = AdditionalProcedureCode.form()
  private val itemId = "itemId"
  private val sampleProcedureCode = ProcedureCode("1040", "blah blah blah")
  private val defaultAdditionalProcedureCodes = Seq(AdditionalProcedureCodeModel("000", "None"))

  private def createView(
    form: Form[AdditionalProcedureCode] = form,
    validCodes: Seq[AdditionalProcedureCodeModel] = defaultAdditionalProcedureCodes,
    codes: Seq[String] = Seq.empty
  )(implicit request: JourneyRequest[_]): Document =
    page(Mode.Normal, itemId, form, sampleProcedureCode, validCodes, codes)(request, messages)

  "Additional Procedure Codes View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.additionalProcedureCodes.title")
      messages must haveTranslationFor("declaration.additionalProcedureCodes.paragraph")
      messages must haveTranslationFor("declaration.additionalProcedureCodes.table.header")
      messages must haveTranslationFor("declaration.additionalProcedureCodes.inset")
      messages must haveTranslationFor("declaration.additionalProcedureCodes.inset.linkText")
    }

    onEveryDeclarationJourney() { implicit request =>
      "provided with empty form" should {
        val view = createView()

        "display page title" in {
          view.getElementsByTag("h1") must containMessageForElements("declaration.additionalProcedureCodes.title", sampleProcedureCode.code)
        }

        "display section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.5")
        }

        "display empty input with label for Additional Procedure Codes" in {
          view.getElementById("additionalProcedureCode").attr("value") mustBe empty
        }

        "display 'Back' button that links to 'Procedure Codes' page" in {
          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.back")
          backButton.getElementById("back-link") must haveHref(
            controllers.declaration.routes.ProcedureCodesController.displayPage(Mode.Normal, itemId)
          )
        }

        "display 'Add' button on page" in {
          val addButton = view.getElementById("add")
          addButton.text() must include(messages("site.add"))
          addButton.text() must include(messages("declaration.additionalProcedureCodes.add.hint"))
        }

        "display 'Save and continue' button on page" in {
          val saveButton = view.getElementById("submit")
          saveButton.text() mustBe messages("site.save_and_continue")
        }

        "display 'Save and return' button on page" in {
          val saveAndReturnButton = view.getElementById("submit_and_return")
          saveAndReturnButton must containMessage("site.save_and_come_back_later")
        }
      }

      "provided with filled form" should {
        "display data in Additional Procedure Code input" in {
          val view = createView(form = AdditionalProcedureCode.form().fill(AdditionalProcedureCode(Some("123"))))

          view.getElementById("additionalProcedureCode").attr("value") mustBe "123"
        }

        "display table headers" in {
          val view = createView(codes = Seq("123", "456"))

          view.getElementsByTag("th").get(0).text() mustBe messages("declaration.additionalProcedureCodes.table.header")
        }

        "have visually hidden header for Remove links" in {
          val view = createView(codes = Seq("123", "456"))

          view.getElementsByTag("th").get(1).text() mustBe messages("site.remove.header")
        }

        "display table values in reverse order they were entered" in {
          val view = createView(codes = Seq("123", "456"))

          view.getElementsByTag("tr").get(1).text() must include("456")
          view.getElementsByTag("tr").get(2).text() must include("123")
        }
      }
    }
  }
}
