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

package forms.declaration.additionaldocuments

import base.TestHelper
import forms.declaration.additionaldocuments.DocumentIdentifierAndPart.{documentIdentifierKey, documentPartKey}
import helpers.views.declaration.DocumentsProducedMessages
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentIdentifierAndPartSpec extends WordSpec with MustMatchers with DocumentsProducedMessages {

  "DocumentIdentifierAndPart form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Document Identifier" which {

        "is longer than 30 characters" in {

          val input = JsObject(Map(documentIdentifierKey -> JsString(TestHelper.createRandomAlphanumericString(31))))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, documentIdentifierError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentIdentifierKey -> JsString("12#$")))
          val expectedErrors =
            Seq(FormError(documentIdentifierKey, documentIdentifierError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Part" which {

        "is longer than 5 characters" in {

          val input = JsObject(Map(documentPartKey -> JsString(TestHelper.createRandomAlphanumericString(6))))
          val expectedErrors = Seq(FormError(documentPartKey, documentPartError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(documentPartKey -> JsString("12#$")))
          val expectedErrors = Seq(FormError(documentPartKey, documentPartError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with correct Document Identifier but no Document Part" in {

        val input = JsObject(Map(documentIdentifierKey -> JsString("ABCD1234")))
        val expectedErrors = Seq(FormError("", documentIdentifierAndPartError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with correct Document Part but no Document Identifier" in {

        val input = JsObject(Map(documentPartKey -> JsString("ABC45")))
        val expectedErrors = Seq(FormError("", documentIdentifierAndPartError))

        testFailedValidationErrors(input, expectedErrors)
      }

      def testFailedValidationErrors(input: JsValue, expectedErrors: Seq[FormError]): Unit = {
        val form = DocumentIdentifierAndPart.form().bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }
  }
}

object DocumentIdentifierAndPartSpec {

  val correctDocumentIdentifierAndPart: DocumentIdentifierAndPart =
    DocumentIdentifierAndPart(documentIdentifier = Some("ABCDEF1234567890"), documentPart = Some("ABC12"))

  val correctDocumentIdentifierAndPartJSON: JsValue = JsObject(
    Map(documentIdentifierKey -> JsString("ABCDEF1234567890"), documentPartKey -> JsString("ABC12"))
  )

  val incorrectDocumentIdentifierAndPart: DocumentIdentifierAndPart = DocumentIdentifierAndPart(
    documentIdentifier = Some(TestHelper.createRandomAlphanumericString(31)),
    documentPart = Some(TestHelper.createRandomAlphanumericString(6))
  )

  val incorrectDocumentIdentifierAndPartJSON: JsValue = JsObject(
    Map(
      documentIdentifierKey -> JsString(TestHelper.createRandomAlphanumericString(31)),
      documentPartKey -> JsString(TestHelper.createRandomAlphanumericString(6))
    )
  )

  val emptyDocumentIdentifierAndPart: DocumentIdentifierAndPart =
    DocumentIdentifierAndPart(documentIdentifier = None, documentPart = None)

  val emptyDocumentIdentifierAndPartJSON: JsValue = JsObject(
    Map(documentIdentifierKey -> JsString(""), documentPartKey -> JsString(""))
  )
}
