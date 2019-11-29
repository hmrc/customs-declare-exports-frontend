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

import services.cache.ExportsTestData
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.related_documents

class RelatedDocumentsViewSpec extends UnitViewSpec with ExportsTestData {

  "Related documents" should {

    "display row with No value" when {

      "documents are empty" in {

        val view = related_documents(Seq.empty)(messages, journeyRequest())

        view.getElementById("previous-documents-label").text() mustBe messages("declaration.summary.transaction.previousDocuments")
        view.getElementById("previous-documents").text() mustBe messages("site.no")
      }
    }

    "diplay documents" when {

      "documents exists" in {

      }
    }
  }
}
