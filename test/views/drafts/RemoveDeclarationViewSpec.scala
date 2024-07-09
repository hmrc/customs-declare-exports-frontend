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

package views.drafts

import base.{ExportsTestData, Injector}
import forms.RemoveDraftDeclaration.form
import forms.section1.ConsignmentReferences
import forms.{Ducr, Lrn, RemoveDraftDeclaration}
import models.ExportsDeclaration
import models.declaration.DeclarationStatus
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import views.html.drafts.remove_declaration
import views.common.UnitViewSpec
import views.tags.ViewTest

import java.time.{LocalDateTime, ZoneOffset}

@ViewTest
class RemoveDeclarationViewSpec extends UnitViewSpec with Injector {

  val title: String = "draft.declarations.remove.title"
  val ducr: String = "draft.declarations.ducr"
  val dateSaved: String = "draft.declarations.dateSaved"

  val page = instanceOf[remove_declaration]

  def decWithDucr(index: Int = 1): ExportsDeclaration = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withConsignmentReferences(ConsignmentReferences(Some(Ducr(s"DUCR-XXXX-$index")), Some(Lrn("LRN-1234")))),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC))
  )

  def createView(dec: ExportsDeclaration, frm: Form[RemoveDraftDeclaration] = form): Document =
    page(declaration = dec, frm)(request, messages)

  "Remove Saved Declarations View" should {

    "display same page title as header" in {
      val viewWithMessage = createView(decWithDucr())
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display declaration about to be removed" in {
      val view = createView(decWithDucr())

      view.getElementsByTag("h1").text() mustBe messages(title)

      tableHeader(view)(0).text() mustBe messages(ducr)
      tableHeader(view)(1).text() mustBe messages(dateSaved)
      tableCell(view)(0, 0).text() mustBe "DUCR-XXXX-1"
      tableCell(view)(0, 1).text() mustBe "1 January 2019 at 10:00am"

      numberOfTableRows(view) mustBe 1

      view.getElementById("submit").text() mustBe messages("draft.declarations.remove.submitButton")
    }

    "display errors if no option has been chosen" in {

      val view = createView(decWithDucr(), form.bind(Map[String, String]()))

      view must haveGovukGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#Yes")

      view must containErrorElementWithMessageKey("draft.declarations.remove.option.error.empty")
    }
  }

  private def numberOfTableRows(view: Document) = view.getElementsByClass("govuk-table__row").size() - 1

  private def tableHeader(view: Document)(column: Int): Element =
    view
      .getElementsByClass("govuk-table__head")
      .first()
      .getElementsByClass("govuk-table__header")
      .get(column)

  private def tableCell(view: Document)(row: Int, column: Int): Element =
    view
      .getElementsByClass("govuk-table__body")
      .first()
      .getElementsByClass("govuk-table__row")
      .get(row)
      .getElementsByClass("govuk-table__cell")
      .get(column)
}
