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

package forms.declaration

import forms.common.DeclarationPageBaseSpec
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentSpec extends DeclarationPageBaseSpec {

  "Document mapping used for binding data" should {

    "return form without errors" when {
      "provided with valid input" in {

        val correctPreviousDocumentsJSON: JsValue = JsObject(
          Map("documentType" -> JsString("MCR"), "documentReference" -> JsString("DocumentReference"), "goodsItemIdentifier" -> JsString("123"))
        )

        val form = Document.form.bind(correctPreviousDocumentsJSON, JsonBindMaxChars)

        form.errors mustBe empty
      }

      "provided document reference with - and /" in {

        val correctJson = JsObject(
          Map(
            "documentType" -> JsString("MCR"),
            "documentReference" -> JsString("GB/239355053000-PATYR8987"),
            "goodsItemIdentifier" -> JsString("123")
          )
        )
        val form = Document.form.bind(correctJson, JsonBindMaxChars)

        form.errors mustBe empty
      }

      "provided document reference with :" in {

        val correctJson = DocumentSpec.json("MCR", "A:12345645", "123")
        val form = Document.form.bind(correctJson, JsonBindMaxChars)

        form.errors mustBe empty
      }
    }
  }

  "Document" when {
    testTariffContentKeys(Document, "tariff.declaration.addPreviousDocument")
  }

  "DocumentChangeOrRemove" when {
    testTariffContentKeysNoSpecialisation(DocumentChangeOrRemove, "tariff.declaration.previousDocuments.remove", getClearanceTariffKeys)
  }
}

object DocumentSpec {

  def json(`type`: String, reference: String, identifier: String) = JsObject(
    Map("documentType" -> JsString(`type`), "documentReference" -> JsString(reference), "goodsItemIdentifier" -> JsString(identifier))
  )
}
