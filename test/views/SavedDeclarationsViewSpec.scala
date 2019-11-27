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
import controllers.routes
import forms.Choice.AllowedChoiceValues.ContinueDec
import forms.declaration.ConsignmentReferences
import forms.{Choice, Ducr, Lrn}
import helpers.views.declaration.CommonMessages
import models.{DeclarationStatus, ExportsDeclaration, Page, Paginated}
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.saved_declarations
import views.tags.ViewTest

@ViewTest
class SavedDeclarationsViewSpec extends UnitViewSpec with CommonMessages with Stubs {

  val title: String = "saved.declarations.title"
  val ducr: String = "saved.declarations.ducr"
  val dateSaved: String = "saved.declarations.dateSaved"

  private val savedDeclarationsPage = new saved_declarations(mainTemplate)
  private val decWithoutDucr = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
  )

  private def decWithDucr(index: Int = 1) = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withConsignmentReferences(ConsignmentReferences(Ducr(s"DUCR-XXXX-$index"), Lrn("LRN-1234"))),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC))
  )

  private def createView(
    declarations: Seq[ExportsDeclaration] = Seq.empty,
    page: Int = 1,
    pageSize: Int = 10,
    total: Int = 0,
    messages: Messages = stubMessages()
  ) = {
    val data = Paginated(declarations, Page(page, pageSize), total)
    savedDeclarationsPage(data)(request, messages)
  }

  "Saved Declarations View" should {

    "display empty declaration list " in {
      val messages = realMessagesApi.preferred(request)
      val view = createView(messages = messages)

      view.title() must include(view.getElementsByTag("h1").text())

      tableCell(view)(0, 0).text() mustBe messages(ducr)
      tableCell(view)(0, 1).text() mustBe messages(dateSaved)

      numberOfTableRows(view) mustBe 0

      view
        .getElementById("pagination-none")
        .text() mustBe s"Showing no ${messages("saved.declarations.pagination.plural")}"
    }

    "display declarations" in {
      val view = createView(declarations = Seq(decWithoutDucr), total = 1)

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0).text() mustBe messages("saved.declarations.noDucr")
      tableCell(view)(1, 1).text() mustBe "1 Jan 2019 at 09:45"
      tableCell(view)(1, 2).text() mustBe messages("site.remove")

      view.getElementById("pagination-one").text() mustBe "Showing 1 saved.declarations.pagination.singular"
    }

    "display pagination controls" in {
      val decs = (1 to 8).map(decWithDucr(_))
      val view = createView(declarations = decs, page = 2, pageSize = 10, total = 28)

      numberOfTableRows(view) mustBe 8

      tableCell(view)(1, 0).text() mustBe "DUCR-XXXX-1"
      tableCell(view)(1, 2).text() mustBe messages("site.remove")
      tableCell(view)(8, 2).text() mustBe messages("site.remove")
      tableCell(view)(8, 0).text() mustBe "DUCR-XXXX-8"

      view.getElementById("pagination-some").text() mustBe "Showing 11 - 18 of 28 saved.declarations.pagination.plural"

      view.getElementById("pagination-page_back").attr("href") mustBe routes.SavedDeclarationsController
        .displayDeclarations()
        .url
      view.getElementById("pagination-page_1").attr("href") mustBe routes.SavedDeclarationsController
        .displayDeclarations()
        .url
      view.getElementById("pagination-page_current").text() mustBe "2"
      view.getElementById("pagination-page_3").attr("href") mustBe
        routes.SavedDeclarationsController.displayDeclarations(3).url
      view.getElementById("pagination-page_next").attr("href") mustBe
        routes.SavedDeclarationsController.displayDeclarations(3).url

    }

    "display 'Back' button that links to 'Choice' page with 'Continue saved declarations' selected" in {
      val backButton = createView().getElementById("back-link")

      backButton must containText("site.back")
      backButton must haveHref(routes.ChoiceController.displayPage(Some(Choice(ContinueDec))))
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
