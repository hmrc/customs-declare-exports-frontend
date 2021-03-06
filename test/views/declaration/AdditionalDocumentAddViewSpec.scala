/*
 * Copyright 2021 HM Revenue & Customs
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

package views.declaration

import base.Injector
import controllers.declaration.routes
import forms.declaration.additionaldocuments.AdditionalDocument
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.additionalDocuments.additional_document_add
import views.tags.ViewTest

@ViewTest
class AdditionalDocumentAddViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val mode = Mode.Normal

  private val form: Form[AdditionalDocument] = AdditionalDocument.form()
  private val additionalDocumentAddPage = instanceOf[additional_document_add]
  private def createView(form: Form[AdditionalDocument] = form)(implicit request: JourneyRequest[_]): Document =
    additionalDocumentAddPage(mode, itemId, form, None)(request, messages)

  "additional_document_add view on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      // TODO. CEDS-3255.
      // If auth code from List1 "display 'Back' button that links to 'Additional Information Required' page when no documents present" in {
      "display 'Back' button that links to 'Additional Documents Required' page when no documents present" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        // TODO. CEDS-3255.
        // If auth code from List1 backButton must haveHref(routes.AdditionalInformationRequiredController.displayPage(mode, itemId)) else
        backButton must haveHref(routes.AdditionalDocumentsRequiredController.displayPage(mode, itemId))
      }
    }
  }

  // TODO. CEDS-3255.
  // If auth code from List1
  /*
  "additional_document_add view on empty page with cached Additional Information" should {
    onEveryDeclarationJourney(withItem(anItem(withItemId(itemId), withAdditionalInformation("1234", "Description")))) { implicit request =>
      val view = createView()

      "display 'Back' button that links to 'Additional Information' page when additional info present" in {

        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.AdditionalInformationController.displayPage(mode, itemId))
      }
    }
  }*/
}
