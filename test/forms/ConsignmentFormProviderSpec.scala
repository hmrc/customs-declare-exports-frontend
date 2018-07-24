/*
 * Copyright 2018 HM Revenue & Customs
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

package forms

import forms.behaviours.OptionFieldBehaviours
import play.api.data.FormError

class ConsignmentFormProviderSpec extends OptionFieldBehaviours {

  val form = new ConsignmentFormProvider()()

  val correctMucr = "A:GBP23"
  val correctDucr = "5GB123456789000-123ABC456DEFIIIIIII"

  val mucrFormatError = FormError("", "error.mucr.format")
  val ducrEmptyError = FormError("", "error.ducr.empty")
  val ducrFormatError = FormError("", "error.ducr.format")

  "Consignment form provider" should {
    "bind valid data" in {
      val result = form.bind(
        Map(
          "choice" -> "singleShipment",
          "mucrConsolidation" -> correctMucr,
          "ducrConsolidation" -> correctDucr,
          "ducrSingleShipment" -> correctDucr
        )
      )

      result.apply("choice").value.map(_ shouldBe "singleShipment")
      result.apply("mucrConsolidation").value.map(_ shouldBe correctMucr)
      result.apply("ducrConsolidation").value.map(_ shouldBe correctDucr)
      result.apply("ducrSingleShipment").value.map(_ shouldBe correctDucr)
    }

    "return error if there is no choice" in {
      val data = Map(
        "choice" -> "",
        "mucrConsolidation" -> "",
        "ducrConsolidation" -> "",
        "ducrSingleShipment" -> ""
      )

      val expectedError = Seq(ducrEmptyError)

      checkForError(form, data, expectedError)
    }

    "return error if ducr number is missing for consolidation" in {
      val data = Map(
        "choice" -> "consolidation",
        "mucrConsolidation" -> "",
        "ducrConsolidation" -> "",
        "ducrSingleShipment" -> ""
      )

      val expectedError = Seq(ducrEmptyError)

      checkForError(form, data, expectedError)
    }

    "return error if ducr number is missing for single shipment" in {
      val data = Map(
        "choice" -> "singleShipment",
        "mucrConsolidation" -> "",
        "ducrConsolidation" -> "",
        "ducrSingleShipment" -> ""
      )

      val expectedError = Seq(ducrEmptyError)

      checkForError(form, data, expectedError)
    }

    "return error if mucr number is incorrect for consolidation" in {
      val data = Map(
        "choice" -> "consolidation",
        "mucrConsolidation" -> "incorrectMucrNumber",
        "ducrConsolidation" -> correctDucr,
        "ducrSingleShipment" -> ""
      )

      val expectedError = Seq(mucrFormatError)

      checkForError(form, data, expectedError)
    }

    "return error if ducr number is incorrect for consolidation" in {
      val data = Map(
        "choice" -> "consolidation",
        "mucrConsolidation" -> correctMucr,
        "ducrConsolidation" -> "incorrectDucrNumber",
        "ducrSingleShipment" -> ""
      )

      val expectedError = Seq(ducrFormatError)

      checkForError(form, data, expectedError)
    }

    "return error if ducr number is incorrect for single shipment" in {
      val data = Map(
        "choice" -> "singleShipment",
        "mucrConsolidation" -> "",
        "ducrConsolidation" -> "",
        "ducrSingleShipment" -> "incorrectDucrNumber"
      )

      val expectedError = Seq(ducrFormatError)

      checkForError(form, data, expectedError)
    }
  }
}
