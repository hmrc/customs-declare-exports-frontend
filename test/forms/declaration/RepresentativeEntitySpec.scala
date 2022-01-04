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

import forms.common.DeclarationPageBaseSpec
import forms.declaration.EntityDetailsSpec._
import play.api.libs.json.{JsObject, JsValue}

class RepresentativeEntitySpec extends DeclarationPageBaseSpec {
  import RepresentativeEntitySpec._

  "RepresentativeEntity mapping used for binding data" should {

    "return form with errors" when {

      "provided with missing eori" in {
        val form = RepresentativeEntity.form().bind(invalidRepresentativeEntityAddressOnlyJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.representative.entity.eori.empty")
      }

      "provided with invalid eori" in {
        val form = RepresentativeEntity.form().bind(invalidRepresentativeEntityInvalidEORIJSON, JsonBindMaxChars)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.error.format")
      }
    }

    "return form without errors" when {
      "provided with valid value for status code" in {
        val form = RepresentativeEntity.form().bind(correctRepresentativeEntityEORIOnlyJSON, JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }

  "RepresentativeEntity" when {
    testTariffContentKeys(RepresentativeEntity, "tariff.declaration.representativesEoriNumber")
  }
}

object RepresentativeEntitySpec {
  val correctRepresentativeEntityEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val invalidRepresentativeEntityInvalidEORIJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val invalidRepresentativeEntityAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
}
