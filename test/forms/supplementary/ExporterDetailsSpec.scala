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

class ExporterDetailsSpec extends WordSpec with MustMatchers {

  private val eori = "PL213472539481923"
  private val fullName = "Full Name"
  private val addressLine = "Address Line"
  private val townOrCity = "Town or City"
  private val postCode = "AB12 3CD"
  private val country = "United Kingdom"
  private val countryCode = "GB"

  private val exporterDetails = ExporterDetails(
    details = EntityDetails(
      eori = Some(eori),
      address = Some(
        Address(
          fullName = fullName,
          addressLine = addressLine,
          townOrCity = townOrCity,
          postCode = postCode,
          country = country
        )
      )
    )
  )

  private val expectedExporterDetailsProperties: Map[String, String] = Map(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.id" -> eori,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.name" -> fullName,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.line" -> addressLine,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.cityName" -> townOrCity,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.postcodeId" -> postCode,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].consignor.address.countryCode" -> countryCode
  )

  "ExporterDetails on toMetadataProperties" should {
    "convert itself to exporter details properties" in {
      exporterDetails.toMetadataProperties() must equal(expectedExporterDetailsProperties)
    }
  }

}
