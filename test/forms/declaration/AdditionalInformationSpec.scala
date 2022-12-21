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

package forms.declaration

import base.FormSpec
import forms.common.DeclarationPageBaseSpec
import forms.declaration.AdditionalInformation.codeForGVMS

class AdditionalInformationSpec extends FormSpec with DeclarationPageBaseSpec {

  val form = AdditionalInformation.form

  "AdditionalInformation form" should {

    "has no errors" when {

      "all fields contains correct data" in {
        val result = form.fillAndValidate(AdditionalInformation("00400", "description"))
        result.hasErrors must be(false)
      }

      "description contains new-line" in {
        val result = form.fillAndValidate(AdditionalInformation("00400", "some\ndescription"))
        result.hasErrors must be(false)
      }

    }

    "has errors" when {

      "no code is entered" in {
        val result = form.fillAndValidate(AdditionalInformation("", "description"))
        result.errors must contain("declaration.additionalInformation.code.empty")
      }

      "an invalid code is entered" in {
        val result = form.fillAndValidate(AdditionalInformation("123456", "description"))
        result.errors must contain("declaration.additionalInformation.code.error")
      }

      "an invalid code is entered (RRS01)" in {
        val result = form.fillAndValidate(AdditionalInformation(codeForGVMS, "description"))
        result.errors must contain("declaration.additionalInformation.code.error.rrs01")
      }

      "an invalid code is entered (LIC99)" in {
        val result = form.fillAndValidate(AdditionalInformation("LIC99", "description"))
        result.errors must contain("declaration.additionalInformation.code.error.lic99")
      }

      "no description is entered" in {
        val result = form.fillAndValidate(AdditionalInformation("12345", ""))
        result.errors must contain("declaration.additionalInformation.description.empty")
      }

      "an invalid description is entered" in {
        val result = form.fillAndValidate(AdditionalInformation("12345", "description!"))
        result.errors must contain("declaration.additionalInformation.description.error")
      }
    }
  }

  "AdditionalInformation" when {
    testTariffContentKeys(AdditionalInformation, "tariff.declaration.item.additionalInformation")
  }

  "AdditionalInformationRequired" when {
    testTariffContentKeys(AdditionalInformationRequired, "tariff.declaration.item.isAdditionalInformationRequired")
  }
}
