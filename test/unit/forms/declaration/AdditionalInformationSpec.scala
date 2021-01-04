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

package unit.forms.declaration

import forms.declaration.AdditionalInformation
import unit.base.FormSpec

class AdditionalInformationSpec extends FormSpec {

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

      "code missing" in {
        val result = form.fillAndValidate(AdditionalInformation("", "description"))
        result.errors must contain("declaration.additionalInformation.code.empty")
      }

      "code invalid" in {
        val result = form.fillAndValidate(AdditionalInformation("123456", "description"))
        result.errors must contain("declaration.additionalInformation.code.error")
      }

      "description missing" in {
        val result = form.fillAndValidate(AdditionalInformation("12345", ""))
        result.errors must contain("declaration.additionalInformation.description.empty")
      }

      "description invalid" in {
        val result = form.fillAndValidate(AdditionalInformation("12345", "description!"))
        result.errors must contain("declaration.additionalInformation.description.error")
      }
    }
  }
}
