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

import base.Injector
import forms.common.YesNoAnswer.{No, Yes}
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType._
import models.Mode
import models.declaration.{DeclarationHoldersData, EoriSource, ExportDeclarationTestData, Parties}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationHolder.declaration_holder_summary
import views.tags.ViewTest

@ViewTest
class DeclarationHolderSummaryViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = instanceOf[declaration_holder_summary]
  val declarationHolder1: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB123456543")), Some(EoriSource.OtherEori))
  val declarationHolder2: DeclarationHolder = DeclarationHolder(Some("CVA"), Some(Eori("GB6543253678")), Some(EoriSource.OtherEori))

  private def createView(mode: Mode = Mode.Normal, form: Form[YesNoAnswer] = YesNoAnswer.form(), holders: Seq[DeclarationHolder] = Seq.empty)(
    implicit request: JourneyRequest[_]
  ): Document = page(mode, form, holders)(request, messages)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.declarationHolders.table.heading")
    messages must haveTranslationFor("declaration.declarationHolders.table.multiple.heading")
    messages must haveTranslationFor("declaration.declarationHolders.table.change.hint")
    messages must haveTranslationFor("declaration.declarationHolders.table.remove.hint")
    messages must haveTranslationFor("declaration.declarationHolders.table.type")
    messages must haveTranslationFor("declaration.declarationHolders.table.eori")
    messages must haveTranslationFor("declaration.declarationHolders.add.another")
  }

  "DeclarationHolder Summary View back link" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { implicit request =>
      Seq(Yes, No).foreach { answer =>
        s"cache contains DeclarationHoldersData.isRequired field with '${answer.get.answer}' answer" when {

          "cache contains empty DeclarationHoldersData.holders field" should {

            "display back link to Declaration Holder Required page" in {
              val parties = Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq.empty, answer)))
              val req = journeyRequest(request.cacheModel.copy(parties = parties))

              val view = createView()(req)
              view must containElementWithID("back-link")
              view.getElementById("back-link") must haveHref(
                controllers.declaration.routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
              )
            }
          }

          "cache contains non-empty DeclarationHoldersData.holders field" should {

            "display back link to Authorisation Procedure Code Choice page" in {
              val parties =
                Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq(ExportDeclarationTestData.correctDeclarationHolder), answer)))
              val req = journeyRequest(request.cacheModel.copy(parties = parties))

              val view = createView()(req)
              view must containElementWithID("back-link")
              view.getElementById("back-link") must haveHref(
                controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
              )
            }
          }
        }

      }

      "cache does NOT contain DeclarationHoldersData.isRequired field" should {
        "display back link to Authorisation Procedure Code Choice page" in {
          val view = createView()
          view must containElementWithID("back-link")
          view.getElementById("back-link") must haveHref(
            controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
          )
        }
      }
    }

    onJourney(OCCASIONAL) { implicit request =>
      "display back link to Other Parties page" in {
        val view = createView()
        view must containElementWithID("back-link")
        view.getElementById("back-link") must haveHref(controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(Mode.Normal))
      }
    }

    onJourney(CLEARANCE) { implicit request =>
      "EIDR is true" must {
        "display back link to Authorisation Choice page" in {
          val parties = Parties(isEntryIntoDeclarantsRecords = Yes)
          val req = journeyRequest(request.cacheModel.copy(parties = parties))

          val view = createView()(req)
          view must containElementWithID("back-link")
          view.getElementById("back-link") must haveHref(
            controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage(Mode.Normal)
          )
        }
      }

      "EIDR is false" must {
        "display back link to Consignee Details page" in {
          val parties = Parties(isEntryIntoDeclarantsRecords = No)
          val req = journeyRequest(request.cacheModel.copy(parties = parties))

          val view = createView()(req)
          view must containElementWithID("back-link")
          view.getElementById("back-link") must haveHref(controllers.declaration.routes.ConsigneeDetailsController.displayPage(Mode.Normal))
        }
      }
    }
  }

  "DeclarationHolder Summary View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.declarationHolders.table.multiple.heading", "0")
      }

      "display page title for multiple items" in {
        createView(holders = Seq.empty).getElementsByTag("h1").text() mustBe messages("declaration.declarationHolders.table.multiple.heading", "0")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display'Save and continue' button on page" in {
        view.getElementById("submit") must containMessage("site.save_and_continue")
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return") must containMessage("site.save_and_come_back_later")
      }
    }
  }

  "DeclarationHolder Summary View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display one row with data in table" in {

        val view = createView(holders = Seq(declarationHolder1))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text() mustBe messages("declaration.declarationHolders.table.type")
        view.select("table>thead>tr>th:nth-child(2)").text() mustBe messages("declaration.declarationHolders.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text() mustBe messages("site.change.header")
        view.select("table>thead>tr>th:nth-child(4)").text() mustBe messages("site.remove.header")

        // check row
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() must include("ACE")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "GB123456543"

        val changeLink = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        changeLink must containMessage("site.change")
        changeLink must containMessage("declaration.declarationHolders.table.change.hint", "ACE-GB123456543")

        val removeLink = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(4)").get(0)
        removeLink must containMessage("site.remove")
        removeLink must containMessage("declaration.declarationHolders.table.remove.hint", "ACE-GB123456543")
      }

      "display two rows with data in table" in {

        val view = createView(holders = Seq(declarationHolder1, declarationHolder2))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text() mustBe messages("declaration.declarationHolders.table.type")
        view.select("table>thead>tr>th:nth-child(2)").text() mustBe messages("declaration.declarationHolders.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text() mustBe messages("site.change.header")
        view.select("table>thead>tr>th:nth-child(4)").text() mustBe messages("site.remove.header")

        // check rows
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text() must include("ACE")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text() mustBe "GB123456543"

        val changeLink1 = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        changeLink1 must containMessage("site.change")
        changeLink1 must containMessage("declaration.declarationHolders.table.change.hint", "ACE-GB123456543")

        val removeLink1 = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(4)").get(0)
        removeLink1 must containMessage("site.remove")
        removeLink1 must containMessage("declaration.declarationHolders.table.remove.hint", "ACE-GB123456543")

        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(1)").text() must include("CVA")
        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(2)").text() mustBe "GB6543253678"

        val changeLink2 = view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(3)").get(0)
        changeLink2 must containMessage("site.change")
        changeLink2 must containMessage("declaration.declarationHolders.table.change.hint", "CVA-GB6543253678")

        val removeLink2 = view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(4)").get(0)
        removeLink2 must containMessage("site.remove")
        removeLink2 must containMessage("declaration.declarationHolders.table.remove.hint", "CVA-GB6543253678")
      }
    }
  }
}
