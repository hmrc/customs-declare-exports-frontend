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

package views.declaration.addtionalDocuments

import base.Injector
import controllers.declaration.routes
import forms.declaration.additionaldocuments.AdditionalDocument
import models.Mode
import models.declaration.ExportDeclarationTestData.declaration
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_document_change
import views.tags.ViewTest

@ViewTest
class AdditionalDocumentChangeViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val documentId = "1.2131231"
  private val mode = Mode.Normal

  private val form: Form[AdditionalDocument] = AdditionalDocument.form(declaration)
  private val additionalDocumentChangePage = instanceOf[additional_document_change]

  private def createView(implicit request: JourneyRequest[_]): Document =
    additionalDocumentChangePage(mode, itemId, documentId, form)(request, messages)

  "additional_document_change view on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "display 'Back' button that links to summary page" in {
        val backButton = createView.getElementById("back-link")
        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalDocumentsController.displayPage(mode, itemId))
      }
    }
  }
}
