/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.declaration.Document.AllowedValues.RelatedDocument
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentSpec extends WordSpec with MustMatchers {

  "Document mapping used for binding data" should {

    "return form with errors" when {

      "provided with unknown document category" in {
        val documentInputData = JsObject(
          Map(
            "documentCategory" -> JsString("Unknown category"),
            "documentType" -> JsString("MCR"),
            "documentReference" -> JsString("DocumentReference"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )
        val form = Document.form.bind(documentInputData)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.previousDocuments.documentCategory.error.incorrect")
      }
    }

    "return form without errors" when {
      "provided with valid input" in {

        val correctPreviousDocumentsJSON: JsValue = JsObject(
          Map(
            "documentCategory" -> JsString(RelatedDocument),
            "documentType" -> JsString("MCR"),
            "documentReference" -> JsString("DocumentReference"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )

        val form = Document.form.bind(correctPreviousDocumentsJSON)

        form.errors mustBe empty
      }

      "provided document reference with - and /" in {

        val correctJson = JsObject(
          Map(
            "documentCategory" -> JsString(RelatedDocument),
            "documentType" -> JsString("MCR"),
            "documentReference" -> JsString("GB/239355053000-PATYR8987"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )
        val form = Document.form.bind(correctJson)

        form.errors mustBe empty
      }

      "provided document reference with :" in {

        val correctJson = JsObject(
          Map(
            "documentCategory" -> JsString(RelatedDocument),
            "documentType" -> JsString("MCR"),
            "documentReference" -> JsString("A:12345645"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )
        val form = Document.form.bind(correctJson)

        form.errors mustBe empty
      }
    }
  }
}
