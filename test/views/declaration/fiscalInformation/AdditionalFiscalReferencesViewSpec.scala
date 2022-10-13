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

package views.declaration.fiscalInformation

import base.Injector
import controllers.declaration.routes.{AdditionalFiscalReferencesRemoveController, AdditionalProcedureCodesController}
import forms.common.YesNoAnswer.form
import forms.declaration.AdditionalFiscalReference
import models.Mode
import models.Mode.Normal
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.fiscalInformation.additional_fiscal_references
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesViewSpec extends UnitViewSpec with Injector {

  val page = instanceOf[additional_fiscal_references]

  def createView(references: Seq[AdditionalFiscalReference], mode: Mode = Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, itemId, form(), references)

  "Additional Fiscal References View" should {
    onEveryDeclarationJourney() { implicit request =>
      val additionalReferences = AdditionalFiscalReference("FR", "12345")
      val view = createView(Seq(additionalReferences))

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.additionalFiscalReferences.table.heading", "0")
      }

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display 'Back' button to Additional Procedure Codes page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(AdditionalProcedureCodesController.displayPage(Normal, itemId))
      }

      val createViewWithMode: Mode => Document = mode => createView(Seq(additionalReferences), mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)

      "display table header" in {
        view.getElementsByClass("govuk-table__header").get(0) must containMessage("declaration.additionalFiscalReferences.country.header")
        view.getElementsByClass("govuk-table__header").get(1) must containMessage("declaration.additionalFiscalReferences.numbers.header")

      }

      "have visually hidden header for Remove links" in {
        view.getElementsByTag("th").get(2) must containMessage("site.remove.header")
      }

      "display references" in {
        view.getElementsByTag("tr").get(1).text() must include("FR")
      }

      "display remove link" in {
        val removeLink = view.getElementsByTag("tr").select(".govuk-link").get(0)
        removeLink must containMessage("site.remove", ("declaration.additionalInformation.table.remove.hint", "12345"))

        val href = AdditionalFiscalReferencesRemoveController.displayPage(Normal, itemId, ListItem.createId(0, additionalReferences))
        removeLink must haveHref(href)
      }
    }
  }
}
