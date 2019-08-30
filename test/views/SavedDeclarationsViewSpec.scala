/*
 * Copyright 2019 HM Revenue & Customs
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

import base.ExportsTestData
import base.ExportsTestData._
import forms.Ducr
import forms.declaration.ConsignmentReferences
import helpers.views.declaration.CommonMessages
import models.{DeclarationStatus, ExportsDeclaration, Page, Paginated}
import org.jsoup.nodes.Element
import play.twirl.api.{Html, HtmlFormat}
import views.declaration.spec.ViewSpec
import views.html.saved_declarations
import views.tags.ViewTest

@ViewTest
class SavedDeclarationsViewSpec extends ViewSpec with CommonMessages {

  val title: String = "saved.declarations.title"
  val ducr: String = "saved.declarations.ducr"
  val dateSaved: String = "saved.declarations.dateSaved"

  private val savedDeclarationsPage = app.injector.instanceOf[saved_declarations]
  private def decWithDucr(index: Int = 1) = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withConsignmentReferences(ConsignmentReferences(Ducr(s"DUCR-XXXX-$index"), "LRN-1234")),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC))
  )
  private val decWithoutDucr = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
  )

  private def createView(
    declarations: Seq[ExportsDeclaration] = Seq.empty,
    page: Int = 1,
    pageSize: Int = 10,
    total: Int = 0
  ) = {
    val data = Paginated(declarations, Page(page, pageSize), total)
    savedDeclarationsPage(data)
  }

  "Saved Declarations View" should {

    "display empty declaration list " in {
      val view = createView()

      view.title() must be(messages(title))
      view.getElementsByTag("h1").text() must be(messages(title))

      tableCell(view)(0, 0).text() must be(messages(ducr))
      tableCell(view)(0, 1).text() must be(messages(dateSaved))

      numberOfTableRows(view) must be(0)

      view.getElementById("pagination-none").text() must be("Showing no saved declarations")
    }

    "display declarations" in {
      val view = createView(declarations = Seq(decWithoutDucr), total = 1)

      numberOfTableRows(view) must be(1)

      tableCell(view)(1, 0).text() must be("No DUCR added")
      tableCell(view)(1, 1).text() must be("1 Jan 2019 at 09:45")
      tableCell(view)(1, 2).text() must be("Remove")

      view.getElementById("pagination-one").text() must be("Showing 1 saved declaration")
    }

    "display pagination controls" in {
      val decs = (1 to 8).map(decWithDucr(_))
      val view = createView(declarations = decs, page = 2, pageSize = 10, total = 28)

      numberOfTableRows(view) must be(8)

      tableCell(view)(1, 0).text() must be("DUCR-XXXX-1")
      tableCell(view)(1, 2).text() must be("Remove")
      tableCell(view)(8, 2).text() must be("Remove")
      tableCell(view)(8, 0).text() must be("DUCR-XXXX-8")

      view.getElementById("pagination-some").text() must be("Showing 11 - 18 of 28 saved declarations")

      view.getElementById("pagination-page_back").attr("href") must be("/customs-declare-exports/saved-declarations")
      view.getElementById("pagination-page_1").attr("href") must be("/customs-declare-exports/saved-declarations")
      view.getElementById("pagination-page_current").text() must be("2")
      view.getElementById("pagination-page_3").attr("href") must be(
        "/customs-declare-exports/saved-declarations?page=3"
      )
      view.getElementById("pagination-page_next").attr("href") must be(
        "/customs-declare-exports/saved-declarations?page=3"
      )
    }

  }

  private def numberOfTableRows(view: Html) = view.getElementsByClass("table-row").size() - 1

  private def tableCell(view: Html)(row: Int, column: Int): Element =
    view
      .select(".table-row")
      .get(row)
      .getElementsByClass("table-cell")
      .get(column)
}
