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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class PackageInformationSpec extends WordSpec with MustMatchers {
  import PackageInformationSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val packageInformation = correctPackageInformationDecimalValues
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].packagings[0].typeCode" -> packageInformation.typesOfPackages,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].packagings[0].quantity" -> packageInformation.numberOfPackages,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].packagings[0].marksNumbersId" -> packageInformation.shippingMarks,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.goodsMeasure.tariffQuantity" -> packageInformation.supplementaryUnits.get,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.goodsMeasure.netWeightMeasure" -> packageInformation.netMass,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].commodity.goodsMeasure.grossMassMeasure" -> packageInformation.grossMass
      )

      packageInformation.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object PackageInformationSpec {
  val correctPackageInformationDecimalValues = PackageInformation(
    typesOfPackages = "AB",
    numberOfPackages = "12345",
    supplementaryUnits = Some("1234567890.123456"),
    shippingMarks = "ShippingMarks",
    netMass = "12345678.123",
    grossMass = "1234567890.123456"
  )
  val correctPackageInformationIntegerValues = PackageInformation(
    typesOfPackages = "AB",
    numberOfPackages = "12345",
    supplementaryUnits = Some("1234567890"),
    shippingMarks = "ShippingMarks",
    netMass = "12345678",
    grossMass = "1234567890"
  )
  val emptyPackageInformation = PackageInformation(
    typesOfPackages = "",
    numberOfPackages = "",
    supplementaryUnits = None,
    shippingMarks = "",
    netMass = "",
    grossMass = ""
  )

  val correctPackageInformationDecimalValuesJSON: JsValue =
    JsObject(
      Map(
        "typesOfPackages" -> JsString("AB"),
        "numberOfPackages" -> JsString("12345"),
        "supplementaryUnits" -> JsString("1234567890.123456"),
        "shippingMarks" -> JsString("ShippingMarks"),
        "netMass" -> JsString("12345678.123"),
        "grossMass" -> JsString("1234567890.123456")
      )
    )
  val correctPackageInformationIntegerValuesJSON: JsValue =
    JsObject(
      Map(
        "typesOfPackages" -> JsString("AB"),
        "numberOfPackages" -> JsString("12345"),
        "supplementaryUnits" -> JsString("1234567890"),
        "shippingMarks" -> JsString("ShippingMarks"),
        "netMass" -> JsString("12345678"),
        "grossMass" -> JsString("1234567890")
      )
    )
  val emptyPackageInformationJSON: JsValue =
    JsObject(
      Map(
        "typesOfPackages" -> JsString(""),
        "numberOfPackages" -> JsString(""),
        "supplementaryUnits" -> JsString(""),
        "shippingMarks" -> JsString(""),
        "netMass" -> JsString(""),
        "grossMass" -> JsString("")
      )
    )
}
