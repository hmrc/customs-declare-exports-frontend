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

package services.mapping.goodsshipment.consignment
import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.declaration.{CarrierDetails, CarrierDetailsSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class ConsignmentCarrierBuilderSpec extends WordSpec with Matchers {

  "ConsignmentCarrierBuilderSpec" should {
    "correctly map Declaration.Consignment.Carrier instance for supplementary journey " when {
      "all data is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id ->
                CarrierDetailsSpec.correctCarrierDetailsJSON
            )
          )
        val carrier = ConsignmentCarrierBuilder.build()(cacheMap, Choice(AllowedChoiceValues.SupplementaryDec))
        carrier should be(null)
      }
    }
    "correctly map Declaration.Consignment.Carrier instance for standard journey " when {
      "all data is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id ->
                CarrierDetailsSpec.correctCarrierDetailsJSON
            )
          )
        val carrier = ConsignmentCarrierBuilder.build()(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        carrier.getID.getValue should be("9GB1234567ABCDEF")
        carrier.getAddress.getLine.getValue should be("Address Line")
        carrier.getAddress.getCityName.getValue should be("Town or City")
        carrier.getAddress.getPostcodeID.getValue should be("AB12 34CD")
        carrier.getAddress.getCountryCode.getValue should be("PL")
        carrier.getName.getValue should be("Full Name")
      }
      "only EORI is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id ->
                CarrierDetailsSpec.correctCarrierDetailsEORIOnlyJSON
            )
          )
        val carrier = ConsignmentCarrierBuilder.build()(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        carrier.getID.getValue should be("9GB1234567ABCDEF")
        carrier.getAddress should be(null)
      }
      "only address is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id ->
                CarrierDetailsSpec.correctCarrierDetailsAddressOnlyJSON
            )
          )
        val carrier = ConsignmentCarrierBuilder.build()(cacheMap, Choice(AllowedChoiceValues.StandardDec))
        carrier.getID should be(null)
        carrier.getAddress.getLine.getValue should be("Address Line")
        carrier.getAddress.getCityName.getValue should be("Town or City")
        carrier.getAddress.getPostcodeID.getValue should be("AB12 34CD")
        carrier.getAddress.getCountryCode.getValue should be("PL")
        carrier.getName.getValue should be("Full Name")
      }
    }
  }
}
