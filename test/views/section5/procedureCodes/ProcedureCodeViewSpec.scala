/*
 * Copyright 2023 HM Revenue & Customs
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

package views.section5.procedureCodes

import base.Injector
import controllers.section5.routes.ItemsSummaryController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section5.procedurecodes.ProcedureCode
import forms.section5.procedurecodes.ProcedureCode.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import views.common.PageWithButtonsSpec
import views.html.section5.procedureCodes.procedure_codes
import views.tags.ViewTest

@ViewTest
class ProcedureCodeViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val page = instanceOf[procedure_codes]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[ProcedureCode] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  "Procedure Code View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.procedureCodes.title")
      messages must haveTranslationFor("declaration.procedureCodes.paragraph")
      messages must haveTranslationFor("declaration.procedureCodes.empty")
      messages must haveTranslationFor("declaration.procedureCodes.error.empty")
      messages must haveTranslationFor("declaration.procedureCodes.error.invalid")

      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.header")
      messages must haveTranslationFor("declaration.procedureCodes.inset.title")
      messages must haveTranslationFor("declaration.procedureCodes.inset.paragraph.1")
      messages must haveTranslationFor("declaration.procedureCodes.inset.paragraph.1.bullet.1")
      messages must haveTranslationFor("declaration.procedureCodes.inset.paragraph.1.bullet.2")
      messages must haveTranslationFor("declaration.procedureCodes.inset.paragraph.1.bullet.3")
      messages must haveTranslationFor("declaration.procedureCodes.inset.paragraph.2")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.paragraph.1")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.paragraph.2")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.removalOfGoodsFromExciseWarehouse")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.removalOfGoodsFromExciseWarehouse.link.text")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.onwardSupplyRelief")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.onwardSupplyRelief.link.text")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.endUseRelief")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.endUseRelief.link.text")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.inwardProcessing")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.inwardProcessing.link.text")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.outwardProcessing.link.text")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.temporaryExport.link.text")
      messages must haveTranslationFor("declaration.procedureCodes.readMoreExpander.reExportFollowingSpecialProcedure.link.text")
    }

    onEveryDeclarationJourney() { implicit request =>
      "provided with empty form" should {
        val view = createView()

        "display page title" in {
          view.getElementsByTag("h1") must containMessageForElements("declaration.procedureCodes.title")
        }

        "display section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.5")
        }

        "display empty input with label for Procedure Code" in {
          view.getElementsByAttributeValue("for", "procedureCode") must containMessageForElements("declaration.procedureCodes.title")
          view.getElementById("procedureCode").attr("value") mustBe empty
        }

        "display 'Back' button that links to 'Export Items' page" in {
          val backButton = view.getElementById("back-link")

          backButton must containMessage("site.backToPreviousQuestion")
          backButton.getElementById("back-link") must haveHref(ItemsSummaryController.displayItemsSummaryPage)
        }

        checkAllSaveButtonsAreDisplayed(createView())
      }

      "provided with filled form" should {
        "display data in Procedure Code input" in {
          val view = createView(form.fill(ProcedureCode("1234")))
          view.getElementById("procedureCode").attr("value") mustBe "1234"
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display inset text" in {
        val inset = createView().getElementsByClass("govuk-inset-text")
        val expected = Seq(
          messages("declaration.procedureCodes.inset.title"),
          messages("declaration.procedureCodes.inset.paragraph.1"),
          messages("declaration.procedureCodes.inset.paragraph.1.bullet.1"),
          messages("declaration.procedureCodes.inset.paragraph.1.bullet.2"),
          messages("declaration.procedureCodes.inset.paragraph.1.bullet.3"),
          messages("declaration.procedureCodes.inset.paragraph.2")
        ).mkString(" ")
        inset.get(0) must containText(expected)
      }

      "display non-standard procedures expander" in {
        createView().getElementsByClass("govuk-details__summary-text").first() must containHtml(
          messages("declaration.procedureCodes.readMoreExpander.header")
        )
      }
    }

    onJourney(CLEARANCE)(aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.yes))) { implicit request =>
      "declaration is EIDR" should {

        "display inset text" in {
          val inset = createView().getElementsByClass("govuk-inset-text")
          val expected = Seq(
            messages("declaration.procedureCodes.inset.paragraph.1"),
            messages("declaration.procedureCodes.inset.paragraph.1.bullet.1"),
            messages("declaration.procedureCodes.inset.paragraph.1.bullet.2"),
            messages("declaration.procedureCodes.inset.paragraph.1.bullet.3"),
            messages("declaration.procedureCodes.inset.paragraph.2")
          ).mkString(" ")
          inset.get(0) must containText(expected)
        }

        "display non-standard procedures expander" in {
          createView().getElementsByClass("govuk-details__summary-text").first() must containHtml(
            messages("declaration.procedureCodes.readMoreExpander.header")
          )
        }
      }
    }

    onJourney(CLEARANCE)(aDeclaration(withEntryIntoDeclarantsRecords(YesNoAnswers.no))) { implicit request =>
      "declaration is NOT EIDR" should {

        "NOT display inset text" in {
          createView().getElementsByClass("govuk-inset-text") must be(empty)
        }

        "NOT display non-standard procedures expander" in {
          Option(createView().getElementById("procedureCode-readMore")) mustBe empty
        }
      }
    }
  }
}
