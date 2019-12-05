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
import forms.declaration.additionaldocuments.DocumentWriteOff._
import helpers.views.declaration.DocumentsProducedMessages
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class DocumentWriteOffSpec extends WordSpec with MustMatchers with DocumentsProducedMessages {

  "DocumentWriteOff form with mapping used to bind data" should {

    "return form with errors" when {

      "provided with Measurement Unit" which {

        "is longer than 5 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB3456")))
          val expectedErrors = Seq(FormError(measurementUnitKey, measurementUnitLengthError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is shorter than 3 characters" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB")))
          val expectedErrors = Seq(FormError(measurementUnitKey, measurementUnitLengthError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains special characters different than hash" in {

          val input = JsObject(Map(measurementUnitKey -> JsString("AB!@")))
          val expectedErrors = Seq(FormError(measurementUnitKey, measurementUnitSpecialCharactersError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with Document Quantity" which {

        "has more than 16 digits in total" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("1234567890.1234567")))
          val expectedErrors = Seq(FormError(documentQuantityKey, documentQuantityPrecisionError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "has more than 6 decimal places" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("0.1234567")))
          val expectedErrors = Seq(FormError(documentQuantityKey, documentQuantityScaleError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is smaller than zero" in {

          val input = JsObject(Map(documentQuantityKey -> JsString("-1.23")))
          val expectedErrors = Seq(FormError(documentQuantityKey, documentQuantityError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with correct Measurement Unit but no Document Quantity" in {

        val input = JsObject(Map(measurementUnitKey -> JsString("AB34")))
        val expectedErrors = Seq(FormError("", measurementUnitAndQuantityError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with correct Document Quantity but no Measurement Unit" in {

        val input = JsObject(Map(documentQuantityKey -> JsString("123.45")))
        val expectedErrors = Seq(FormError("", measurementUnitAndQuantityError))

        testFailedValidationErrors(input, expectedErrors)
      }

      def testFailedValidationErrors(input: JsValue, expectedErrors: Seq[FormError]): Unit = {
        val form = DocumentWriteOff.form().bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }
  }

  "Document Write Off mapping" should {

    "allow hash character" in {

      val input = JsObject(Map(measurementUnitKey -> JsString("AB#12"), documentQuantityKey -> JsString("0.123")))
      val form = DocumentWriteOff.form().bind(input)

      form.errors mustBe empty
    }
  }
}

object DocumentWriteOffSpec {

  val correctDocumentWriteOff =
    DocumentWriteOff(measurementUnit = Some("AB12"), documentQuantity = Some(BigDecimal("1234567890.123456")))

  val correctDocumentWriteOffJSON = JsObject(Map(measurementUnitKey -> JsString("AB12"), documentQuantityKey -> JsString("1234567890.123456")))

  val incorrectDocumentWriteOff =
    DocumentWriteOff(measurementUnit = Some(TestHelper.createRandomAlphanumericString(6)), documentQuantity = Some(BigDecimal("12345678901234567")))

  val incorrectDocumentWriteOffJSON = JsObject(
    Map(measurementUnitKey -> JsString(TestHelper.createRandomAlphanumericString(6)), documentQuantityKey -> JsString("12345678901234567"))
  )

  val emptyDocumentWriteOff =
    DocumentWriteOff(measurementUnit = None, documentQuantity = None)

  val emptyDocumentWriteOffJSON = JsObject(Map(measurementUnitKey -> JsString(""), documentQuantityKey -> JsString("")))

}
