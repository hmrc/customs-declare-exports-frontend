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

package views

import java.time.{LocalDateTime, ZoneOffset}

import base.{ExportsTestData, Injector}
import controllers.routes
import forms.Choice.AllowedChoiceValues.ContinueDec
import forms.declaration.ConsignmentReferences
import forms.{Choice, Ducr, Lrn}
import models.{DeclarationStatus, ExportsDeclaration, Page, Paginated}
import org.jsoup.nodes.Element
import play.twirl.api.Html
import views.declaration.spec.{UnitViewSpec, ViewMatchers}
import views.html.saved_declarations
import views.tags.ViewTest

@ViewTest
class SavedDeclarationsViewSpec extends UnitViewSpec with Injector with ViewMatchers {
  val title: String = "saved.declarations.title"

  val ducr: String = "saved.declarations.ducr"
  private val noDucrLabel = "No DUCR added"

  val dateSaved: String = "saved.declarations.dateSaved"
  private val savedDeclarationsPage = instanceOf[saved_declarations]

  private val decWithoutDucr = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
  )

  private def decWithDucr(index: Int = 1) = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withConsignmentReferences(ConsignmentReferences(Ducr(s"DUCR-XXXX-$index"), Lrn("LRN-1234"))),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC))
  )

  private def createView(declarations: Seq[ExportsDeclaration] = Seq.empty, page: Int = 1, pageSize: Int = 10, total: Int = 0) = {
    val data = Paginated(declarations, Page(page, pageSize), total)
    savedDeclarationsPage(data)(request, messages)
  }
  "Saved Declarations View" should {

    "display empty declaration list " in {
      val view = createView()

      view.title() must include(view.getElementsByTag("h1").text())

      tableHead(view)(0).text() mustBe messages(ducr)
      tableHead(view)(1).text() mustBe messages(dateSaved)

      numberOfTableRows(view) mustBe 0

      view.getElementsByClass("ceds-pagination") mustNot be(empty)
    }

    "display created declarations before BST" in {
      val view = createView(declarations = Seq(decWithoutDucr), total = 1)

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0).text() mustBe s"${messages("saved.declarations.noDucr")} ${messages("saved.declarations.continue.hidden", noDucrLabel)}"
      tableCell(view)(1, 1).text() mustBe "1 January 2019 at 09:45"
      tableCell(view)(1, 2).text() mustBe s"${messages("site.remove")} ${messages("saved.declarations.remove.hidden", noDucrLabel)}"

      view.getElementsByClass("ceds-pagination") mustNot be(empty)
    }

    "display created declarations after BST" in {
      val decWithoutDucrAfterBST = ExportsTestData.aDeclaration(
        withStatus(DeclarationStatus.DRAFT),
        withUpdateTime(LocalDateTime.of(2019, 5, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
      )

      val view = createView(declarations = Seq(decWithoutDucrAfterBST), total = 1)

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0).text() mustBe s"${messages("saved.declarations.noDucr")} ${messages("saved.declarations.continue.hidden", noDucrLabel)}"
      tableCell(view)(1, 1).text() mustBe "1 May 2019 at 10:45"
      tableCell(view)(1, 2).text() mustBe s"${messages("site.remove")} ${messages("saved.declarations.remove.hidden", noDucrLabel)}"

      view.getElementsByClass("ceds-pagination") mustNot be(empty)
    }

    "display 'Back' button that links to 'Choice' page with 'Continue saved declarations' selected" in {
      val backButton = createView().getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(ContinueDec))))
    }

  }

  private def numberOfTableRows(view: Html) = view.getElementsByClass("govuk-table__row").size() - 1

  private def tableHead(view: Html)(column: Int): Element =
    view
      .select(".govuk-table__head")
      .first()
      .getElementsByClass("govuk-table__header")
      .get(column)

  private def tableCell(view: Html)(row: Int, column: Int): Element =
    view
      .select(".govuk-table__row")
      .get(row)
      .getElementsByClass("govuk-table__cell")
      .get(column)
}
