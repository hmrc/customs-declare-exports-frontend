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
import services.Countries.allCountries
import uk.gov.hmrc.wco.dec.MetaData

class ConsigneeDetailsSpec extends WordSpec with MustMatchers {
  import ConsigneeDetailsSpec._

  "Method toMetadataProperties" should {
    "map correctly" in {

      val consigneeDetails = correctConsigneeDetails
      val metadata = MetaData.fromProperties(consigneeDetails.toMetadataProperties())

      metadata.declaration must be(defined)
      metadata.declaration.get.goodsShipment must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.id must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.id.get must be(consigneeDetails.details.eori.get)
      metadata.declaration.get.goodsShipment.get.consignee.get.name must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.name.get must be(
        consigneeDetails.details.address.get.fullName
      )
      metadata.declaration.get.goodsShipment.get.consignee.get.address must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.line must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.line.get must be(
        consigneeDetails.details.address.get.addressLine
      )
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.cityName must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.cityName.get must be(
        consigneeDetails.details.address.get.townOrCity
      )
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.postcodeId must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.postcodeId.get must be(
        consigneeDetails.details.address.get.postCode
      )
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.countryCode must be(defined)
      metadata.declaration.get.goodsShipment.get.consignee.get.address.get.countryCode.get must be(
        allCountries
          .find(country => consigneeDetails.details.address.get.country.contains(country.countryName))
          .map(_.countryCode)
          .getOrElse("")
      )
    }
  }

}

object ConsigneeDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

  val correctConsigneeDetails = ConsigneeDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctConsigneeDetailsEORIOnly = ConsigneeDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctConsigneeDetailsAddressOnly = ConsigneeDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val incorrectConsigneeDetails = ConsigneeDetails(details = EntityDetailsSpec.incorrectEntityDetails)
  val emptyConsigneeDetails = ConsigneeDetails(details = EntityDetailsSpec.emptyEntityDetails)

  val correctConsigneeDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctConsigneeDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctConsigneeDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val incorrectConsigneeDetailsJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val emptyConsigneeDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))

  val entityDetailsWithEmptyFullNameJSON: JsValue = JsObject(Map("details" -> EntityDetailsSpec.entityDetailsWithEmptyFullNameJSON))
}
