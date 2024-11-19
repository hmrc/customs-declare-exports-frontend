/*
 * Copyright 2024 HM Revenue & Customs
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

package views.drafts

import base.Injector
import controllers.routes.ChoiceController
import models.declaration.DeclarationStatus.{AMENDMENT_DRAFT, DRAFT}
import models.{DraftDeclarationData, Page, Paginated}
import org.jsoup.nodes.Element
import org.scalatest.Assertion
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import views.common.UnitViewSpec
import views.html.drafts.saved_declarations
import views.tags.ViewTest

import java.time.{LocalDate, LocalDateTime, ZoneOffset}

@ViewTest
class SavedDeclarationsViewSpec extends UnitViewSpec with Injector {

  val title: String = "draft.declarations.title"

  val ducr: String = "draft.declarations.ducr"
  val noDucrLabel = "No DUCR added"

  val dateSaved: String = "draft.declarations.dateSaved"

  val page = instanceOf[saved_declarations]

  val year = LocalDate.now.getYear

  private val draftWithoutDucr =
    DraftDeclarationData("draftId", None, DRAFT, LocalDateTime.of(year, 1, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))

  private def createView(declarations: Seq[DraftDeclarationData] = List.empty, pageIx: Int = 1, pageSize: Int = 10): Appendable = {
    val data = Paginated(declarations, Page(pageIx, pageSize), declarations.size)
    page(data)(request, messages)
  }

  "Draft Declarations View" should {

    "display the expected title" in {
      val view = createView(List.fill(22)(draftWithoutDucr), 5, 4)
      checkTitle(view, 6, 5)
    }

    "display an empty page on an empty declaration list" in {
      val view = createView()

      checkTitle(view, 1)

      view.getElementsByClass("govuk-heading-xl").first must containMessage("draft.declarations.title")

      view.title() must include(view.getElementsByTag("h1").text())

      view.getElementsByClass("govuk-body").first must containMessage("draft.declarations.paragraph")

      tableHead(view)(0).text() mustBe messages(dateSaved)
      tableHead(view)(1).text() mustBe messages(ducr)
      tableHead(view)(2).text() mustBe messages("draft.declarations.status")
      tableHead(view)(3).text() mustBe messages("site.remove.header")

      numberOfTableRows(view) mustBe 0

      verifyPagination(view)
    }

    "display created declarations before BST" in {
      val view = createView(declarations = List(draftWithoutDucr))

      checkTitle(view, 1)

      view.getElementsByClass("govuk-heading-xl").first must containMessage("draft.declarations.title")
      view.getElementsByClass("govuk-body").first must containMessage("draft.declarations.paragraph")

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0).text() mustBe s"1 January $year at 9:45am"
      tableCell(view)(1, 1) must containMessage("draft.declarations.noDucr")
      tableCell(view)(1, 1) must containMessage("draft.declarations.continue.hidden", noDucrLabel)
      tableCell(view)(1, 2) must containMessage("draft.declarations.draft")
      tableCell(view)(1, 3) must containMessage("site.remove")
      tableCell(view)(1, 3) must containMessage("draft.declarations.remove.hidden", noDucrLabel)

      verifyPagination(view)
    }

    "display created declarations after BST" in {
      val updatedDateTimeAfterBST = LocalDateTime.of(year, 5, 1, 9, 45, 0).toInstant(ZoneOffset.UTC)
      val draftWithoutDucrAfterBST = draftWithoutDucr.copy(updatedDateTime = updatedDateTimeAfterBST)

      val view = createView(declarations = List(draftWithoutDucrAfterBST))

      view.getElementsByClass("govuk-heading-xl").first must containMessage("draft.declarations.title")
      view.getElementsByClass("govuk-body").first must containMessage("draft.declarations.paragraph")

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0).text() mustBe s"1 May $year at 10:45am"
      tableCell(view)(1, 1) must containMessage("draft.declarations.noDucr")
      tableCell(view)(1, 1) must containMessage("draft.declarations.continue.hidden", noDucrLabel)
      tableCell(view)(1, 2) must containMessage("draft.declarations.draft")
      tableCell(view)(1, 3) must containMessage("site.remove")
      tableCell(view)(1, 3) must containMessage("draft.declarations.remove.hidden", noDucrLabel)

      verifyPagination(view)
    }

    "display created with Amendment" in {
      val view = createView(declarations = List(draftWithoutDucr.copy(status = AMENDMENT_DRAFT)))

      numberOfTableRows(view) mustBe 1

      view.getElementsByClass("govuk-heading-xl").first must containMessage("draft.declarations.title.amendments")
      view.getElementsByClass("govuk-body").first must containMessage("draft.declarations.paragraph")

      tableCell(view)(1, 0).text() mustBe s"1 January $year at 9:45am"
      tableCell(view)(1, 1) must containMessage("draft.declarations.noDucr")
      tableCell(view)(1, 1) must containMessage("draft.declarations.continue.hidden", noDucrLabel)
      tableCell(view)(1, 2) must containMessage("draft.declarations.amendment")
      tableCell(view)(1, 3) must containMessage("site.remove")
      tableCell(view)(1, 3) must containMessage("draft.declarations.remove.hidden", noDucrLabel)

      verifyPagination(view)
    }

    "display 'Back' button that links to 'Choice' page with 'Continue saved declarations' selected" in {
      val backButton = createView().getElementById("back-link")
      backButton must containMessage("site.back")
      backButton must haveHref(ChoiceController.displayPage)
    }

    def verifyPagination(view: Appendable): Assertion = {
      view.getElementsByClass("page-summary") mustNot be(empty)
      view.getElementsByClass("govuk-pagination") must be(empty)
    }
  }

  private def numberOfTableRows(view: Html): Int = view.getElementsByClass("govuk-table__row").size() - 1

  private def tableHead(view: Html)(column: Int): Element =
    view
      .select(".govuk-table__head")
      .first
      .getElementsByClass("govuk-table__header")
      .get(column)

  private def tableCell(view: Html)(row: Int, column: Int): Element =
    view
      .select(".govuk-table__row")
      .get(row)
      .getElementsByClass("govuk-table__cell")
      .get(column)

  private def checkTitle(view: Html, pageOf: Int, currentPage: Int = 1): Assertion = {
    val title = view.getElementsByTag("title").first
    title.text mustBe s"${messages("draft.declarations.title")} - Page $currentPage of $pageOf - ${messages("service.name")} - GOV.UK"
  }
}
