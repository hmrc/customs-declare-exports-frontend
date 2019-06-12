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

class CarrierDetailsSpec extends WordSpec with MustMatchers {
  import CarrierDetailsSpec._

  "Method toMetadataProperties" should {
    "map correctly" in {

      val carrierDetails = correctCarrierDetails
      val metadata = MetaData.fromProperties(carrierDetails.toMetadataProperties())

      metadata.declaration mustBe defined
      metadata.declaration.get.goodsShipment mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.id mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.id.get must be(
        carrierDetails.details.eori.get
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.name mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.name.get must be(
        carrierDetails.details.address.get.fullName
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.line mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.line.get must be(
        carrierDetails.details.address.get.addressLine
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.cityName mustBe defined
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.cityName.get must be(
        carrierDetails.details.address.get.townOrCity
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.postcodeId must be(
        defined
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.postcodeId.get must be(
        carrierDetails.details.address.get.postCode
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.countryCode must be(
        defined
      )
      metadata.declaration.get.goodsShipment.get.consignment.get.goodsLocation.get.address.get.countryCode.get must be(
        allCountries
          .find(country => carrierDetails.details.address.get.country.contains(country.countryName))
          .map(_.countryCode)
          .getOrElse("")
      )
    }
  }
}

object CarrierDetailsSpec {
  import forms.declaration.EntityDetailsSpec._

  val correctCarrierDetails = CarrierDetails(details = EntityDetailsSpec.correctEntityDetails)
  val correctCarrierDetailsEORIOnly = CarrierDetails(details = EntityDetailsSpec.correctEntityDetailsEORIOnly)
  val correctCarrierDetailsAddressOnly = CarrierDetails(details = EntityDetailsSpec.correctEntityDetailsAddressOnly)
  val incorrectCarrierDetails = CarrierDetails(details = EntityDetailsSpec.incorrectEntityDetails)
  val emptyCarrierDetails = CarrierDetails(details = EntityDetailsSpec.emptyEntityDetails)

  val correctCarrierDetailsJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsJSON))
  val correctCarrierDetailsEORIOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsEORIOnlyJSON))
  val correctCarrierDetailsAddressOnlyJSON: JsValue = JsObject(Map("details" -> correctEntityDetailsAddressOnlyJSON))
  val incorrectCarrierDetailsJSON: JsValue = JsObject(Map("details" -> incorrectEntityDetailsJSON))
  val emptyCarrierDetailsJSON: JsValue = JsObject(Map("details" -> emptyEntityDetailsJSON))
}
