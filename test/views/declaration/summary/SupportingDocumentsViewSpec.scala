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

package views.declaration.summary

import forms.declaration.additionaldocuments.DocumentsProduced
import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.supporting_documents

class SupportingDocumentsViewSpec extends UnitViewSpec with ExportsTestData {

  "Supporting documents view" should {

    "be empty" when {

      "there is no documents" in {

        supporting_documents(Seq.empty)(messages, journeyRequest()).text() mustBe empty
      }
    }

    "display all supporting documents" in {

      val documents = Seq(
        DocumentsProduced(Some("typ1"), Some("identifier1"), None, None, None, None, None),
        DocumentsProduced(Some("typ2"), Some("identifier2"), None, None, None, None, None)
      )
      val view = supporting_documents(documents)(messages, journeyRequest())

      view.getElementById("supporting-documents").text() mustBe messages("declaration.summary.items.item.supportingDocuments")
      view.getElementById("supporting-documents-code").text() mustBe messages("declaration.summary.items.item.supportingDocuments.code")
      view.getElementById("supporting-documents-information").text() mustBe messages("declaration.summary.items.item.supportingDocuments.information")
      view.getElementById("supporting-document-0-code").text() mustBe "typ1"
      view.getElementById("supporting-document-0-information").text() mustBe "identifier1"
      view.getElementById("supporting-document-1-code").text() mustBe "typ2"
      view.getElementById("supporting-document-1-information").text() mustBe "identifier2"
    }
  }
}
