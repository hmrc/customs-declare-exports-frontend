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

package forms.declaration

import base.TestHelper
import forms.declaration.DocumentsProducedSpec.correctDocumentsProducedJSON
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class DocumentSpec extends WordSpec with MustMatchers {
  import DocumentSpec._

  "Document mapping used for binding data" should {

    "return form with errors" when {
      "provided with empty document category" in {
        val documentInputData = JsObject(
          Map(
            "documentType" -> JsString("ABC"),
            "documentReference" -> JsString("DocumentReference"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )
        val form = Document.form().bind(documentInputData)

        form.hasErrors mustBe true
        form.errors.length mustEqual 1
        form.errors.head.message mustEqual "supplementary.previousDocuments.documentCategory.error.empty"
      }

      "provided with unknown document category" in {
        val documentInputData = JsObject(
          Map(
            "documentCategory" -> JsString("Unknown category"),
            "documentType" -> JsString("ABC"),
            "documentReference" -> JsString("DocumentReference"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )
        val form = Document.form().bind(documentInputData)

        form.hasErrors mustBe true
        form.errors.length mustEqual 1
        form.errors.head.message mustEqual "supplementary.previousDocuments.documentCategory.error.incorrect"
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = Document.form().bind(correctPreviousDocumentsJSON)

        form.hasErrors mustBe false
      }
    }

  }

}

object DocumentSpec {
  import forms.declaration.Document.AllowedValues.TemporaryStorage

  val correctPreviousDocument = Document(
    documentCategory = TemporaryStorage,
    documentType = "ABC",
    documentReference = "DocumentReference",
    goodsItemIdentifier = Some("123")
  )
  val emptyPreviousDocument =
    Document(documentCategory = "", documentType = "", documentReference = "", goodsItemIdentifier = None)

  val incorrectPreviousDocuments = Document(
    documentCategory = "Incorrect category",
    documentType = "Incorrect type",
    documentReference = TestHelper.createRandomAlphanumericString(36),
    goodsItemIdentifier = Some("Incorrect identifier")
  )
  val mandatoryPreviousDocuments = Document(
    documentCategory = TemporaryStorage,
    documentType = "ABC",
    documentReference = "DocumentReference",
    goodsItemIdentifier = None
  )

  val correctPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString(TemporaryStorage),
      "documentType" -> JsString("ABC"),
      "documentReference" -> JsString("DocumentReference"),
      "goodsItemIdentifier" -> JsString("123")
    )
  )

  val emptyPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString(""),
      "documentType" -> JsString(""),
      "documentReference" -> JsString(""),
      "goodsItemIdentifier" -> JsString("")
    )
  )
  val mandatoryPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString(TemporaryStorage),
      "documentType" -> JsString("ABC"),
      "documentReference" -> JsString("DocumentReference"),
      "goodsItemIdentifier" -> JsString("")
    )
  )
  val incorrectPreviousDocumentsJSON: JsValue = JsObject(
    Map(
      "documentCategory" -> JsString("Incorrect category"),
      "documentType" -> JsString("Incorrect type"),
      "documentReference" -> JsString(TestHelper.createRandomAlphanumericString(36)),
      "goodsItemIdentifier" -> JsString("Incorrect identifier")
    )
  )

  val correctPreviousDocumentsJSONList = JsObject(Map("documents" -> JsArray(Seq(correctPreviousDocumentsJSON))))
  val emptyPreviousDocumentsJSONList = JsObject(Map("documents" -> JsArray(Seq(emptyPreviousDocumentsJSON))))

}
