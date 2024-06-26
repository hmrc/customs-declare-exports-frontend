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

package forms.declaration

import forms.common.DeclarationPageBaseSpec
import forms.section2.RepresentativeStatus.StatusCodes.DirectRepresentative
import forms.section2.RepresentativeStatus
import play.api.libs.json.{JsObject, JsString, JsValue}

class RepresentativeStatusSpec extends DeclarationPageBaseSpec {
  import RepresentativeStatusSpec._

  "RepresentativeStatus mapping used for binding data" should {

    "return form with errors" when {

      "provided with unrecognized status code" in {
        val form = RepresentativeStatus.form.bind(representativeStatusJSON("invalid"), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.representative-status.error")
      }

      "provided with missing status code when its required" in {
        val form = RepresentativeStatus.form.bind(representativeStatusJSON(""), JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.representative-status.error")
      }
    }

    "return form without errors" when {
      "provided with valid value for status code" in {
        val form = RepresentativeStatus.form.bind(representativeStatusJSON(DirectRepresentative), JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

  "RepresentativeStatus" when {
    testTariffContentKeys(RepresentativeStatus, "tariff.declaration.representationTypeAgreed")
  }
}

object RepresentativeStatusSpec {
  val correctRepresentativeStatus: RepresentativeStatus = RepresentativeStatus(statusCode = Some(DirectRepresentative))

  def representativeStatusJSON(value: String): JsValue = JsObject(Map("statusCode" -> JsString(value)))
}
