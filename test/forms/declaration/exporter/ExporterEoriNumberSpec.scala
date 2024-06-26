/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration.exporter

import base.FormSpec
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{DeclarationPageBaseSpec, Eori}
import forms.section2.exporter.ExporterEoriNumber.form
import forms.section2.exporter.ExporterEoriNumber
import play.api.data.FormError

class ExporterEoriNumberSpec extends FormSpec with DeclarationPageBaseSpec {

  "Exporter Eori Number form" should {

    "has no errors" when {

      "the answer is 'No'" in {

        val correctModel = ExporterEoriNumber(None, YesNoAnswers.no)

        val result = form.fillAndValidate(correctModel)

        result.errors mustBe empty
      }

      "the answer is 'Yes' and EORI is provided" in {

        val correctModel = ExporterEoriNumber(Some(Eori("GB1234567890123")), YesNoAnswers.yes)

        val result = form.fillAndValidate(correctModel)

        result.errors mustBe empty
      }
    }

    "has errors" when {

      "the answer is 'Yes' and EORI is not provided" in {

        val incorrectData = Map("eori" -> "", "hasEori" -> "Yes")

        val result = form.bind(incorrectData)

        result.errors mustBe Seq(FormError("eori", "declaration.exporterEori.eori.empty"))
      }

      "there is no answer on the question" in {

        val incorrectData = Map.empty[String, String]

        val result = form.bind(incorrectData)

        result.errors mustBe Seq(FormError("hasEori", "declaration.exporterEori.hasEori.empty"))
      }
    }
  }

  "ExporterEoriNumber" when {
    testTariffContentKeys(ExporterEoriNumber, "tariff.declaration.exporterEoriNumber")
  }
}
