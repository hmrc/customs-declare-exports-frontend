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

package forms.supplementary

import base.TestHelper
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class GoodsLocationSpec extends WordSpec with MustMatchers {
  import GoodsLocationSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val goodsLocation = correctGoodsLocation
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.consignment.goodsLocation.name" -> goodsLocation.identificationOfLocation.get,
        "declaration.goodsShipment.consignment.goodsLocation.id" -> goodsLocation.additionalIdentifier.get,
        "declaration.goodsShipment.consignment.goodsLocation.typeCode" -> goodsLocation.typeOfLocation.get,
        "declaration.goodsShipment.consignment.goodsLocation.address.typeCode" -> goodsLocation.qualifierOfIdentification.get,
        "declaration.goodsShipment.consignment.goodsLocation.address.cityName" -> goodsLocation.city.get,
        "declaration.goodsShipment.consignment.goodsLocation.address.countryCode" -> "GB",
        "declaration.goodsShipment.consignment.goodsLocation.address.line" -> goodsLocation.streetAndNumber.get,
        "declaration.goodsShipment.consignment.goodsLocation.address.postcodeId" -> goodsLocation.postCode.get
      )

      goodsLocation.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }
}

object GoodsLocationSpec {
  val correctGoodsLocation = GoodsLocation(
    country = Some("United Kingdom"),
    typeOfLocation = Some("T"),
    qualifierOfIdentification = Some("Q"),
    identificationOfLocation = Some("LOC"),
    additionalIdentifier = Some("Additional Identifier"),
    streetAndNumber = Some("Street and Number"),
    postCode = Some("Postcode"),
    city = Some("City")
  )
  val emptyGoodsLocation = GoodsLocation(
    country = None,
    typeOfLocation = None,
    qualifierOfIdentification = None,
    identificationOfLocation = None,
    additionalIdentifier = None,
    streetAndNumber = None,
    postCode = None,
    city = None
  )

  val correctGoodsLocationJSON: JsValue = JsObject(
    Map(
      "country" -> JsString("United Kingdom"),
      "typeOfLocation" -> JsString("T"),
      "qualifierOfIdentification" -> JsString("Q"),
      "identificationOfLocation" -> JsString("LOC"),
      "additionalIdentifier" -> JsString("Additional identifier"),
      "streetAndNumber" -> JsString("Street and number"),
      "postCode" -> JsString("Postcode"),
      "city" -> JsString("City")
    )
  )
  val emptyGoodsLocationJSON: JsValue = JsObject(
    Map(
      "country" -> JsString(""),
      "typeOfLocation" -> JsString(""),
      "qualifierOfIdentification" -> JsString(""),
      "identificationOfLocation" -> JsString(""),
      "additionalIdentifier" -> JsString(""),
      "streetAndNumber" -> JsString(""),
      "postCode" -> JsString(""),
      "city" -> JsString("")
    )
  )
  val incorrectGoodsLocationJSON: JsValue = JsObject(
    Map(
      "country" -> JsString(TestHelper.createRandomString(3)),
      "typeOfLocation" -> JsString(TestHelper.createRandomString(2)),
      "qualifierOfIdentification" -> JsString(TestHelper.createRandomString(2)),
      "identificationOfLocation" -> JsString(TestHelper.createRandomString(4)),
      "additionalIdentifier" -> JsString(TestHelper.createRandomString(33)),
      "streetAndNumber" -> JsString(TestHelper.createRandomString(71)),
      "postCode" -> JsString(TestHelper.createRandomString(10)),
      "city" -> JsString(TestHelper.createRandomString(36))
    )
  )

}
