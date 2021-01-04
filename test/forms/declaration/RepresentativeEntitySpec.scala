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

import forms.declaration.EntityDetailsSpec._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsValue}

class RepresentativeEntitySpec extends WordSpec with MustMatchers {
  import RepresentativeEntitySpec._

  "RepresentativeEntity mapping used for binding data" should {

    "return form with errors" when {

      "provided with missing eori" in {
        val form = RepresentativeEntity.form().bind(invalidRepresentativeEntityAddressOnlyJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.empty")
      }

      "provided with invalid eori" in {
        val form = RepresentativeEntity.form().bind(invalidRepresentativeEntityInvalidEORIJSON)

        form.hasErrors must be(true)
        form.errors.length must equal(1)
        form.errors.head.message must equal("declaration.eori.error.format")
      }
    }

    "return form without errors" when {
      "provided with valid value for status code" in {
        val form = RepresentativeEntity.form().bind(correctRepresentativeEntityEORIOnlyJSON)

        form.hasErrors must be(false)
      }
    }

  }

}

object RepresentativeEntitySpec {
  val correctRepresentativeEntity =
    RepresentativeEntity(details = EntityDetailsSpec.correctEntityDetails)

  val correctRepresentativeEntityJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))

  val correctRepresentativeEntityEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val invalidRepresentativeEntityInvalidEORIJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val invalidRepresentativeEntityAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
}
