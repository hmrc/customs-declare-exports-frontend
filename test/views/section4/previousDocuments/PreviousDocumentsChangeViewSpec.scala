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

package views.section4.previousDocuments

import base.Injector
import controllers.section4.routes.PreviousDocumentsSummaryController
import forms.section4.Document
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.twirl.api.Html
import services.{DocumentType, DocumentTypeService}
import utils.ListItem
import views.declaration.spec.UnitViewSpec
import views.html.section4.previousDocuments.previous_documents_change

class PreviousDocumentsChangeViewSpec extends UnitViewSpec with Injector {

  val mockDocumentTypeService = mock[DocumentTypeService]
  when(mockDocumentTypeService.allDocuments()(any())).thenReturn(List(DocumentType("DocumentReference", "355")))

  private val page = instanceOf[previous_documents_change]
  private val document = Document("750", "reference", Some("3"))
  private val form = Document.form(mockDocumentTypeService)(messages).fill(document)

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
