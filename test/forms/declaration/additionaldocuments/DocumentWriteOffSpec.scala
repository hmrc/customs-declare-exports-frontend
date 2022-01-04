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

package forms.declaration.additionaldocuments

import base.{TestHelper, UnitSpec}
import forms.declaration.additionaldocuments.DocumentWriteOff._
import play.api.data.FormError
import play.api.libs.json._

class DocumentWriteOffSpec extends UnitSpec {

  "DocumentWriteOff form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Measurement Unit" which {

        "is not 3 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB3456")))
          val expectedErrors = Seq(FormError(measurementUnitKey, "declaration.additionalDocument.measurementUnit.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB!@")))
          val expectedErrors = Seq(FormError(measurementUnitKey, "declaration.additionalDocument.measurementUnit.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Quantity" which {

        "has more than 16 digits in total" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("1234567890.1234567")))
          val expectedErrors = Seq(FormError(documentQuantityKey, "declaration.additionalDocument.documentQuantity.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "has more than 6 decimal places" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("0.1234567")))
          val expectedErrors = Seq(FormError(documentQuantityKey, "declaration.additionalDocument.documentQuantity.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is smaller than zero" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("-1.23")))
          val expectedErrors = Seq(FormError(documentQuantityKey, "declaration.additionalDocument.documentQuantity.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: JsValue, expectedErrors: Seq[FormError]): Unit = {
        val form = DocumentWriteOff.form().bind(input, JsonBindMaxChars)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }
  }

}

object DocumentWriteOffSpec {

  val correctDocumentWriteOff =
    DocumentWriteOff(measurementUnit = Some("ABC"), documentQuantity = Some(BigDecimal("1234567890.123456")))

  val correctDocumentWriteOffJSON = JsObject(Map(measurementUnitKey -> JsString("ABC"), documentQuantityKey -> JsString("1234567890.123456")))

  val incorrectDocumentWriteOff =
    DocumentWriteOff(measurementUnit = Some(TestHelper.createRandomAlphanumericString(6)), documentQuantity = Some(BigDecimal("12345678901234567")))

  val incorrectDocumentWriteOffJSON = JsObject(
    Map(measurementUnitKey -> JsString(TestHelper.createRandomAlphanumericString(6)), documentQuantityKey -> JsString("12345678901234567"))
  )

  val emptyDocumentWriteOff =
    DocumentWriteOff(measurementUnit = None, documentQuantity = None)

  val emptyDocumentWriteOffJSON = JsObject(Map(measurementUnitKey -> JsString(""), documentQuantityKey -> JsString("")))

}
