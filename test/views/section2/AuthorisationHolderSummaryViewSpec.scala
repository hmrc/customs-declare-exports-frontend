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

package views.section2

import base.Injector
import controllers.section2.routes.{AdditionalActorsSummaryController, AuthorisationProcedureCodeChoiceController, ConsigneeDetailsController}
import forms.common.Eori
import forms.common.YesNoAnswer.{form, No, Yes}
import forms.section2.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.section1.AdditionalDeclarationType.STANDARD_PRE_LODGED
import forms.section2.authorisationHolder.AuthorisationHolder
import models.DeclarationType._
import models.declaration.{EoriSource, Parties}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import views.html.section2.authorisationHolder.authorisation_holder_summary
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

@ViewTest
class AuthorisationHolderSummaryViewSpec extends PageWithButtonsSpec with Injector {

  val authorisationHolder1: AuthorisationHolder = AuthorisationHolder(Some("ACE"), Some(Eori("GB123456543")), Some(EoriSource.OtherEori))
  val authorisationHolder2: AuthorisationHolder = AuthorisationHolder(Some("CVA"), Some(Eori("GB6543253678")), Some(EoriSource.OtherEori))

  val page = instanceOf[authorisation_holder_summary]

  override val typeAndViewInstance = (STANDARD, page(form(), Seq.empty)(_, _))

  def createView(holders: Seq[AuthorisationHolder] = Seq.empty)(implicit request: JourneyRequest[_]): Document =
    page(form(), holders)

  "have proper messages for labels" in {
    messages must haveTranslationFor("declaration.authorisationHolder.add.another")
  }

  "AuthorisationHolder Summary View" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { implicit request =>
      "display a back button linking to the /authorisation-choice page" in {
        val view = createView()
        view.getElementById("back-link") must haveHref(AuthorisationProcedureCodeChoiceController.displayPage)
      }
    }

    "display a back button linking to the /authorisation-choice page" when {
      "AdditionalDeclarationType is 'STANDARD_PRE_LODGED' and" when {
        List(Choice1040, ChoiceOthers).foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '${choice.value}'" in {
            val request = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(choice))
            val view = createView()(request)
            view.getElementById("back-link") must haveHref(AuthorisationProcedureCodeChoiceController.displayPage)
          }
        }
      }
    }

    onOccasional { implicit request =>
      "display a back button linking  to the /other-parties-involved page" in {
        val view = createView()
        view.getElementById("back-link") must haveHref(AdditionalActorsSummaryController.displayPage)
      }
    }

    onClearance { implicit req =>
      "display a back button linking to the /authorisation-choice page" when {
        "EIDR is true" in {
          val request = journeyRequest(req.cacheModel.copy(parties = Parties(isEntryIntoDeclarantsRecords = Yes)))

          val view = createView()(request)
          view.getElementById("back-link") must haveHref(AuthorisationProcedureCodeChoiceController.displayPage)
        }
      }

      "display a back button linking to the /consignee-details page" when {
        "EIDR is false" in {
          val request = journeyRequest(req.cacheModel.copy(parties = Parties(isEntryIntoDeclarantsRecords = No)))

          val view = createView()(request)
          view.getElementById("back-link") must haveHref(ConsigneeDetailsController.displayPage)
        }
      }
    }
  }

  "AuthorisationHolder Summary View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1") must containMessageForElements("declaration.authorisationHolder.table.multiple.heading", "0")
      }

      "display page title for multiple items" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.authorisationHolder.table.multiple.heading", "0")
      }

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.2")
      }

      "display body paragraph" in {
        view.getElementsByClass("govuk-body").first must containMessage("declaration.authorisationHolder.bodyParagraph")
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }

  "AuthorisationHolder Summary View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display one row with data in table" in {
        val view = createView(Seq(authorisationHolder1))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text mustBe messages("declaration.authorisationHolder.table.type")
        view.select("table>thead>tr>th:nth-child(2)").text mustBe messages("declaration.authorisationHolder.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text mustBe messages("site.remove.header")

        // check row
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text must include("ACE")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text mustBe "GB123456543"

        val removeLink = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        removeLink must containMessage("site.remove")
        removeLink must containMessage("declaration.authorisationHolder.table.remove.hint", "ACE-GB123456543")
      }

      "display two rows with data in table" in {
        val view = createView(Seq(authorisationHolder1, authorisationHolder2))

        // check table header
        view.select("table>thead>tr>th:nth-child(1)").text mustBe messages("declaration.authorisationHolder.table.type")
        view.select("table>thead>tr>th:nth-child(2)").text mustBe messages("declaration.authorisationHolder.table.eori")
        view.select("table>thead>tr>th:nth-child(3)").text mustBe messages("site.remove.header")

        // check rows
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(1)").text must include("ACE")
        view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(2)").text mustBe "GB123456543"

        val removeLink1 = view.select(".govuk-table__body > tr:nth-child(1) > td:nth-child(3)").get(0)
        removeLink1 must containMessage("site.remove")
        removeLink1 must containMessage("declaration.authorisationHolder.table.remove.hint", "ACE-GB123456543")

        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(1)").text must include("CVA")
        view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(2)").text mustBe "GB6543253678"

        val removeLink2 = view.select(".govuk-table__body > tr:nth-child(2) > td:nth-child(3)").get(0)
        removeLink2 must containMessage("site.remove")
        removeLink2 must containMessage("declaration.authorisationHolder.table.remove.hint", "CVA-GB6543253678")
      }
    }
  }
}
