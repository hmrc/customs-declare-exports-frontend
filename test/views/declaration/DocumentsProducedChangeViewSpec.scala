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

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.Date._
import forms.declaration.DocumentsProducedSpec._
import forms.declaration.additionaldocuments.DocumentWriteOff._
import forms.declaration.additionaldocuments.DocumentWriteOffSpec._
import forms.declaration.additionaldocuments.DocumentsProduced
import forms.declaration.additionaldocuments.DocumentsProduced._
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.documentsProduced.documents_produced_change
import views.tags.ViewTest

@ViewTest
class DocumentsProducedChangeViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector with OptionValues {

  private val itemId = "a7sc78"
  private val documentId = "1.2131231"
  private val mode = Mode.Normal

  private val form: Form[DocumentsProduced] = DocumentsProduced.form()
  private val documentsProducedAddPage = instanceOf[documents_produced_change]

  private def createView(form: Form[DocumentsProduced] = form)(implicit request: JourneyRequest[_]): Document =
    documentsProducedAddPage(mode, itemId, documentId, form, None)(request, messages)

  "Documents Produced View on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "display 'Back' button that links to summary page" in {

        val backButton = createView().getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton must haveHref(routes.DocumentsProducedController.displayPage(mode, itemId))
      }
    }
  }
}
