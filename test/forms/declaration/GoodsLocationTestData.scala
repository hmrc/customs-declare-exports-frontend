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

import base.TestHelper
import play.api.libs.json.{JsObject, JsString, JsValue}

object GoodsLocationTestData {
  val identificationOfLocation = "LOC"
  val country = "United Kingdom"
  val addressLine = "Street and Number"
  val postcode = "Postcode"
  val city = "City"
  val typeOfLocation = "T"
  val qualifierOfIdentification = "Y"
  val additionalIdentifier = "9GB1234567ABCDEF"

  val correctGoodsLocation = GoodsLocation(
    country = country,
    typeOfLocation = typeOfLocation,
    qualifierOfIdentification = qualifierOfIdentification,
    identificationOfLocation = Some(identificationOfLocation),
    additionalIdentifier = Some(additionalIdentifier),
    addressLine = Some(addressLine),
    postCode = Some(postcode),
    city = Some(city)
  )
  val emptyGoodsLocation = GoodsLocation(
    country = "",
    typeOfLocation = "",
    qualifierOfIdentification = "",
    identificationOfLocation = None,
    additionalIdentifier = None,
    addressLine = None,
    postCode = None,
    city = None
  )

  var countryCode: String = "GB"

  val correctGoodsLocationJSON: JsValue = JsObject(
    Map(
      "identificationOfLocation" -> JsString(identificationOfLocation),
      "addressLine" -> JsString(addressLine),
      "city" -> JsString(city),
      "postCode" -> JsString(postcode),
      "country" -> JsString(country),
      "additionalIdentifier" -> JsString(additionalIdentifier),
      "typeOfLocation" -> JsString(typeOfLocation),
      "qualifierOfIdentification" -> JsString(qualifierOfIdentification)
    )
  )
  val emptyGoodsLocationJSON: JsValue = JsObject(
    Map(
      "country" -> JsString(""),
      "typeOfLocation" -> JsString(""),
      "qualifierOfIdentification" -> JsString(""),
      "identificationOfLocation" -> JsString(""),
      "additionalIdentifier" -> JsString(""),
      "addressLine" -> JsString(""),
      "postCode" -> JsString(""),
      "city" -> JsString("")
    )
  )
  val incorrectGoodsLocationJSON: JsValue = JsObject(
    Map(
      "country" -> JsString(TestHelper.createRandomAlphanumericString(3)),
      "typeOfLocation" -> JsString(TestHelper.createRandomAlphanumericString(2)),
      "qualifierOfIdentification" -> JsString(TestHelper.createRandomAlphanumericString(2)),
      "identificationOfLocation" -> JsString(TestHelper.createRandomAlphanumericString(36)),
      "additionalIdentifier" -> JsString(TestHelper.createRandomAlphanumericString(4)),
      "addressLine" -> JsString(TestHelper.createRandomAlphanumericString(71)),
      "postCode" -> JsString(TestHelper.createRandomAlphanumericString(10)),
      "city" -> JsString(TestHelper.createRandomAlphanumericString(36))
    )
  )
}
