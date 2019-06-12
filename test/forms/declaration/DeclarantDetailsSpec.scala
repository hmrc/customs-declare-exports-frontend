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

package forms.declaration

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsValue}

class DeclarantDetailsSpec extends WordSpec with MustMatchers {
  import DeclarantDetailsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val declarantDetails = correctDeclarantDetails
      val countryCode = "PL"
      val expectedDeclarantDetailsProperties: Map[String, String] = Map(
        "declaration.declarant.id" -> declarantDetails.details.eori.get,
        "declaration.declarant.name" -> declarantDetails.details.address.get.fullName,
        "declaration.declarant.address.line" -> declarantDetails.details.address.get.addressLine,
        "declaration.declarant.address.cityName" -> declarantDetails.details.address.get.townOrCity,
        "declaration.declarant.address.postcodeId" -> declarantDetails.details.address.get.postCode,
        "declaration.declarant.address.countryCode" -> countryCode
      )

      declarantDetails.toMetadataProperties() mustEqual expectedDeclarantDetailsProperties
    }
  }

}

object DeclarantDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

  val correctDeclarantDetails = DeclarantDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctDeclarantDetailsEORIOnly = DeclarantDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctDeclarantDetailsAddressOnly = DeclarantDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val incorrectDeclarantDetails = DeclarantDetails(details = EntityDetailsSpec.incorrectEntityDetails)
  val emptyDeclarantDetails = DeclarantDetails(details = EntityDetailsSpec.emptyEntityDetails)

  val correctDeclarantDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctDeclarantDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctDeclarantDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val incorrectDeclarantDetailsJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val emptyDeclarantDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))

}
