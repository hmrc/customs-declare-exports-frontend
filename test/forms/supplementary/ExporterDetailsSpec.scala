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
import play.api.libs.json.{JsObject, JsValue}

class ExporterDetailsSpec extends WordSpec with MustMatchers {
  import ExporterDetailsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val exporterDetails = correctExporterDetails
      val countryCode = "PL"
      val expectedExporterDetailsProperties: Map[String, String] = Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.id" -> exporterDetails.details.eori.get,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.name" -> exporterDetails.details.address.get.fullName,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.line" -> exporterDetails.details.address.get.addressLine,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.cityName" -> exporterDetails.details.address.get.townOrCity,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.postcodeId" -> exporterDetails.details.address.get.postCode,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.countryCode" -> countryCode
      )

      exporterDetails.toMetadataProperties() must equal(expectedExporterDetailsProperties)
    }
  }

}

object ExporterDetailsSpec {
  import forms.supplementary.EntityDetailsSpec._

  val correctExporterDetails = ExporterDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctExporterDetailsEORIOnly = ExporterDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctExporterDetailsAddressOnly = ExporterDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val emptyExporterDetails = ExporterDetails(details = EntityDetailsSpec.emptyEntityDetails)

  val correctExporterDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctExporterDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctExporterDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val emptyExporterDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}