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
import controllers.declaration.routes.{AdditionalActorsSummaryController, AuthorisationProcedureCodeChoiceController, ConsigneeDetailsController}
import forms.common.YesNoAnswer.{No, Yes}
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType._
import models.Mode
import models.Mode.Normal
import models.declaration.{EoriSource, Parties}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.declarationHolder.declaration_holder_summary
import views.tags.ViewTest

@ViewTest
class DeclarationHolderSummaryViewSpec extends UnitViewSpec with ExportsTestHelper with Stubs with Injector {

  private val page = instanceOf[declaration_holder_summary]
  val declarationHolder1: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB123456543")), Some(EoriSource.OtherEori))
  val declarationHolder2: DeclarationHolder = DeclarationHolder(Some("CVA"), Some(Eori("GB6543253678")), Some(EoriSource.OtherEori))

  private def createView(holders: Seq[DeclarationHolder] = Seq.empty, mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, YesNoAnswer.form(), holders)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.declarationHolders.add.another")
  }

  "DeclarationHolder Summary View" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { implicit request =>
      "display a back button linking to the /authorisation-choice page" in {
        val view = createView()
        view.getElementById("back-link") must haveHref(AuthorisationProcedureCodeChoiceController.displayPage(Normal))
      }
    }

    "display a back button linking to the /authorisation-choice page" when {
      "AdditionalDeclarationType is 'STANDARD_PRE_LODGED' and" when {
        List(Choice1040, ChoiceOthers).foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '${choice.value}'" in {
            val request = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(choice))
            val view = createView()(request)
            view.getElementById("back-link") must haveHref(AuthorisationProcedureCodeChoiceController.displayPage(Normal))
          }
        }
      }
    }

    onOccasional { implicit request =>
      "display a back button linking  to the /other-parties-involved page" in {
        val view = createView()
        view.getElementById("back-link") must haveHref(AdditionalActorsSummaryController.displayPage(Normal))
      }
    }

    onClearance { implicit req =>
      "display a back button linking to the /authorisation-choice page" when {
        "EIDR is true" in {
          val request = journeyRequest(req.cacheModel.copy(parties = Parties(isEntryIntoDeclarantsRecords = Yes)))

          val view = createView()(request)
          view.getElementById("back-link") must haveHref(AuthorisationProcedureCodeChoiceController.displayPage(Normal))
        }
      }

      "display a back button linking to the /consignee-details page" when {
        "EIDR is false" in {
          val request = journeyRequest(req.cacheModel.copy(parties = Parties(isEntryIntoDeclarantsRecords = No)))

          val view = createView()(request)
          view.getElementById("back-link") must haveHref(ConsigneeDetailsController.displayPage(Normal))
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
        view.getElementsByTag("h1").text mustBe messages("declaration.declarationHolders.table.multiple.heading", "0")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }
  }

  "DeclarationHolder Summary View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display one row with data in table" in {

        val view = createView(Seq(declarationHolder1))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text mustBe messages("declaration.declarationHolders.table.type")
        view.select("table>thead>tr>th:nth-child(2)").text mustBe messages("declaration.declarationHolders.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text mustBe messages("site.change.header")
        view.select("table>thead>tr>th:nth-child(4)").text mustBe messages("site.remove.header")

        // check row
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text must include("ACE")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text mustBe "GB123456543"

        val changeLink = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        changeLink must containMessage("site.change")
        changeLink must containMessage("declaration.declarationHolders.table.change.hint", "ACE-GB123456543")

        val removeLink = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(4)").get(0)
        removeLink must containMessage("site.remove")
        removeLink must containMessage("declaration.declarationHolders.table.remove.hint", "ACE-GB123456543")
      }

      "display two rows with data in table" in {

        val view = createView(Seq(declarationHolder1, declarationHolder2))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text mustBe messages("declaration.declarationHolders.table.type")
        view.select("table>thead>tr>th:nth-child(2)").text mustBe messages("declaration.declarationHolders.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text mustBe messages("site.change.header")
        view.select("table>thead>tr>th:nth-child(4)").text mustBe messages("site.remove.header")

        // check rows
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text must include("ACE")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text mustBe "GB123456543"

        val changeLink1 = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        changeLink1 must containMessage("site.change")
        changeLink1 must containMessage("declaration.declarationHolders.table.change.hint", "ACE-GB123456543")

        val removeLink1 = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(4)").get(0)
        removeLink1 must containMessage("site.remove")
        removeLink1 must containMessage("declaration.declarationHolders.table.remove.hint", "ACE-GB123456543")

        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(1)").text must include("CVA")
        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(2)").text mustBe "GB6543253678"

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
