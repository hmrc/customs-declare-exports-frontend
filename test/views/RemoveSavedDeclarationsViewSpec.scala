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
import forms.declaration.ConsignmentReferences
import forms.{Ducr, RemoveDraftDeclaration}
import helpers.views.declaration.CommonMessages
import models.{DeclarationStatus, ExportsDeclaration}
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import play.twirl.api.Html
import views.html.remove_declaration
import views.tags.ViewTest
import views.declaration.spec.AppViewSpec

@ViewTest
class RemoveSavedDeclarationsViewSpec extends AppViewSpec with CommonMessages {

  val title: String = "saved.declarations.remove.title"
  val ducr: String = "saved.declarations.ducr"
  val dateSaved: String = "saved.declarations.dateSaved"

  private val removeSavedDeclarationsPage = app.injector.instanceOf[remove_declaration]

  private def decWithDucr(index: Int = 1) = ExportsTestData.aDeclaration(
    withStatus(DeclarationStatus.DRAFT),
    withConsignmentReferences(ConsignmentReferences(Ducr(s"DUCR-XXXX-$index"), "LRN-1234")),
    withUpdateTime(LocalDateTime.of(2019, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC))
  )

  private def createView(dec: ExportsDeclaration, form: Form[RemoveDraftDeclaration] = RemoveDraftDeclaration.form) =
    removeSavedDeclarationsPage(declaration = dec, form)

  "Remove Saved Declarations View" should {

    "display declaration about to be removed" in {
      val view = createView(decWithDucr())

      view.title() must be(messages(title))
      view.getElementsByTag("h1").text() must be(messages(title))

      tableCell(view)(0, 0).text() must be(messages(ducr))
      tableCell(view)(0, 1).text() must be(messages(dateSaved))
      tableCell(view)(1, 0).text() must be(messages("DUCR-XXXX-1"))
      tableCell(view)(1, 1).text() must be(messages("1 Jan 2019 at 10:00"))

      numberOfTableRows(view) must be(1)

      view.getElementById("submit").text() must be("Continue")
    }

    "display errors if no option has been chosen" in {

      val view = createView(decWithDucr(), RemoveDraftDeclaration.form.bind(Map[String, String]()))

      view.getElementById("error-summary-heading").text() must be(messages("error.summary.title"))

      view.getElementById("error-message-remove-input").text() must be(
        messages("saved.declarations.remove.option.error.empty")
      )
    }
  }

  private def numberOfTableRows(view: Html) = view.getElementsByClass("table-row").size() - 1

  private def tableCell(view: Html)(row: Int, column: Int): Element =
    view
      .getElementsByClass("table-row")
      .get(row)
      .getElementsByClass("table-cell")
      .get(column)
}
