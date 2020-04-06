/*
 * Copyright 2020 HM Revenue & Customs
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

import models.DeclarationType._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsValue}

class DeclarantDetailsSpec extends WordSpec with MustMatchers {

  import DeclarantDetailsSpec._

  Seq(SUPPLEMENTARY, STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE).map { decType =>
    "Declarant details mapping used for binding data" should {

      s"return form with errors for $decType journey" when {

        "provided with empty input" in {

          val form = DeclarantDetails.form(decType).bind(emptyDeclarantDetailsJSON)

          form.hasErrors mustBe true
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.declarant.eori.empty")
        }
      }
    }
  }

  Seq(SUPPLEMENTARY, STANDARD, OCCASIONAL, SIMPLIFIED, CLEARANCE).map { decType =>
    "Declarant details mapping used for binding data" should {

      s"return form with errors for $decType journey" when {

        "provided with an invalid EORI" in {

          val form = DeclarantDetails.form(decType).bind(invalidDeclarantDetailsEORIOnlyJSON)

          form.hasErrors mustBe true
          form.errors.length must equal(1)
          form.errors.head.message must equal("declaration.declarant.eori.error.format")
        }
      }

      s"return form without errors for $decType journey" when {

        "provided with valid input" in {

          val form = DeclarantDetails.form(decType).bind(correctDeclarantDetailsJSON)

          form.hasErrors mustBe false
        }
      }
    }
  }
}

object DeclarantDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

  val correctDeclarantDetails = DeclarantDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctDeclarantDetailsEORIOnly = DeclarantDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)

  val correctDeclarantDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctDeclarantDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val invalidDeclarantDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val emptyDeclarantDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
