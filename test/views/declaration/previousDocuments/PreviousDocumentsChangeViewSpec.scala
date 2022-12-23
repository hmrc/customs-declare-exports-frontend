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

package views.declaration.previousDocuments

import base.Injector
import controllers.declaration.routes.PreviousDocumentsSummaryController
import forms.declaration.Document
import models.requests.JourneyRequest
import play.twirl.api.Html
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.declaration.previousDocuments.previous_documents_change

class PreviousDocumentsChangeViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[previous_documents_change]
  private val document = Document("750", "reference", Some("3"))
  private val form = Document.form.fill(document)

  private def createView(implicit request: JourneyRequest[_]): Html =
    page(ListItem.createId(0, document), form)(request, messages)

  "Previous Documents Change page" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView

      "display section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.4")
      }

      "display 'Back' button that links to 'Previous Documents Summary' page" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage("site.backToPreviousQuestion")
        backButton must haveHref(PreviousDocumentsSummaryController.displayPage)
      }
    }
  }
}
