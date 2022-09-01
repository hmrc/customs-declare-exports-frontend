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
import forms.common.YesNoAnswer
import forms.declaration.AdditionalFiscalReference
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportItemIdGeneratorService
import tools.Stubs
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.fiscalInformation.additional_fiscal_references
import views.tags.ViewTest

@ViewTest
class AdditionalFiscalReferencesViewSpec extends UnitViewSpec with Stubs with CommonMessages with Injector {

  private val form: Form[YesNoAnswer] = YesNoAnswer.form()

  private val additionalFiscalReferencesPage = instanceOf[additional_fiscal_references]

  val itemId = new ExportItemIdGeneratorService().generateItemId()

  private def createView(form: Form[YesNoAnswer] = form, references: Seq[AdditionalFiscalReference], mode: Mode = Mode.Normal)(
    implicit request: JourneyRequest[_]
  ): Document =
    additionalFiscalReferencesPage(mode, itemId, form, references)

  "Additional Fiscal References View" should {
    onEveryDeclarationJourney() { implicit request =>
      val additionalReferences = AdditionalFiscalReference("FR", "12345")
      val view = createView(references = Seq(additionalReferences))

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.additionalFiscalReferences.table.heading", "0")
      }

      "display header" in {
        view.getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display 'Back' button to Additional Procedure Codes page" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(controllers.declaration.routes.AdditionalProcedureCodesController.displayPage(Mode.Normal, itemId))
      }

      val createViewWithMode: Mode => Document = mode => createView(references = Seq(additionalReferences), mode = mode)
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
        removeLink must haveHref(
          controllers.declaration.routes.AdditionalFiscalReferencesRemoveController
            .displayPage(Mode.Normal, itemId, ListItem.createId(0, additionalReferences))
        )
      }
    }
  }
}
