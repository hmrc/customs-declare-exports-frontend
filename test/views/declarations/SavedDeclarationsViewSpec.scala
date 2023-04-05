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

package views.declarations

import base.{ExportsTestData, Injector}
import config.featureFlags.DeclarationAmendmentsConfig
import controllers.routes
import forms.Choice
import forms.Choice.AllowedChoiceValues.ContinueDec
import models.declaration.DeclarationStatus
import models.{ExportsDeclaration, Page, Paginated}
import org.jsoup.nodes.Element
import org.mockito.Mockito.when
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.Appendable
import views.declaration.spec.UnitViewSpec
import views.html.declarations.saved_declarations
import views.tags.ViewTest

import java.time.{LocalDateTime, ZoneOffset}

@ViewTest
class SavedDeclarationsViewSpec extends UnitViewSpec with Injector {

  val title: String = "saved.declarations.title"

  val ducr: String = "saved.declarations.ducr"
  val noDucrLabel = "No DUCR added"

  val dateSaved: String = "saved.declarations.dateSaved"

  val page = instanceOf[saved_declarations]

  val mockAmendmentFlag = mock[DeclarationAmendmentsConfig]

  private val decWithoutDucr = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
  )

  private val decWithAmendment = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.AMENDMENT_DRAFT),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
  )

  private def createView(declarations: Seq[ExportsDeclaration] = Seq.empty, pageIx: Int = 1, pageSize: Int = 10, total: Int = 0): Appendable = {
    val data = Paginated(declarations, Page(pageIx, pageSize), total)
    page(data)(request, messages)
  }

  "Saved Declarations View" should {

    "display empty declaration list " in {
      val view = createView()

      view.getElementsByClass("govuk-heading-xl").get(0) must containMessage("saved.declarations.title")

      view.title() must include(view.getElementsByTag("h1").text())

      tableHead(view)(0).text() mustBe messages(ducr)
      tableHead(view)(1).text() mustBe messages("saved.declarations.status")
      tableHead(view)(2).text() mustBe messages(dateSaved)
      tableHead(view)(3).text() mustBe messages("site.remove.header")

      numberOfTableRows(view) mustBe 0

      view.getElementsByClass("ceds-pagination") mustNot be(empty)
    }

    "display created declarations before BST" in {
      val view = createView(declarations = Seq(decWithoutDucr), total = 1)

      view.getElementsByClass("govuk-heading-xl").get(0) must containMessage("saved.declarations.title")

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0) must containMessage("saved.declarations.noDucr")
      tableCell(view)(1, 0) must containMessage("saved.declarations.continue.hidden", noDucrLabel)
      tableCell(view)(1, 1) must containMessage("saved.declarations.draft")
      tableCell(view)(1, 2).text() mustBe "1 January 2019 at 9:45am"
      tableCell(view)(1, 3) must containMessage("site.remove")
      tableCell(view)(1, 3) must containMessage("saved.declarations.remove.hidden", noDucrLabel)

      view.getElementsByClass("ceds-pagination") mustNot be(empty)
    }

    "display created declarations after BST" in {
      val decWithoutDucrAfterBST = ExportsTestData.aDeclaration(
        withStatus(DeclarationStatus.DRAFT),
        withUpdateTime(LocalDateTime.of(2019, 5, 1, 9, 45, 0).toInstant(ZoneOffset.UTC))
      )

      val view = createView(declarations = Seq(decWithoutDucrAfterBST), total = 1)

      view.getElementsByClass("govuk-heading-xl").get(0) must containMessage("saved.declarations.title")

      numberOfTableRows(view) mustBe 1

      tableCell(view)(1, 0) must containMessage("saved.declarations.noDucr")
      tableCell(view)(1, 0) must containMessage("saved.declarations.continue.hidden", noDucrLabel)
      tableCell(view)(1, 1) must containMessage("saved.declarations.draft")
      tableCell(view)(1, 2).text() mustBe "1 May 2019 at 10:45am"
      tableCell(view)(1, 3) must containMessage("site.remove")
      tableCell(view)(1, 3) must containMessage("saved.declarations.remove.hidden", noDucrLabel)

      view.getElementsByClass("ceds-pagination") mustNot be(empty)
    }

    "display created with Amendment and feature flag on" in {
      when(mockAmendmentFlag.isEnabled).thenReturn(true)
      val view = createView(declarations = Seq(decWithAmendment), total = 1)

      numberOfTableRows(view) mustBe 1

      view.getElementsByClass("govuk-heading-xl").get(0) must containMessage("saved.declarations.title.amendments")

      tableCell(view)(1, 0) must containMessage("saved.declarations.noDucr")
      tableCell(view)(1, 0) must containMessage("saved.declarations.continue.hidden", noDucrLabel)
      tableCell(view)(1, 1) must containMessage("saved.declarations.amendment")
      tableCell(view)(1, 2).text() mustBe "1 January 2019 at 9:45am"
      tableCell(view)(1, 3) must containMessage("site.remove")
      tableCell(view)(1, 3) must containMessage("saved.declarations.remove.hidden", noDucrLabel)

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
