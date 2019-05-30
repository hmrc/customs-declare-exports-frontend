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
import forms.common.Address
import forms.declaration.{CarrierDetails, EntityDetails}
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class GoodsLocationBuilderSpec extends WordSpec with Matchers {

  "GoodsLocationBuilder" should {
    "correctly map GoodsLocation instance" when {
      "when only eori is supplied " in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id ->
                Json.toJson(CarrierDetails(details = EntityDetails(Some("9GB1234567ABCDEF"), None)))
            )
          )
        val goodsLocation = GoodsLocationBuilder.build
        goodsLocation.getID.getValue should be("9GB1234567ABCDEF")
        goodsLocation.getAddress should be(null)
        goodsLocation.getName should be(null)
        goodsLocation.getTypeCode should be(null)
      }

      "when only address is supplied " in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              CarrierDetails.id ->
                Json.toJson(
                  CarrierDetails(
                    details = EntityDetails(
                      None,
                      Some(Address("Full Name", "Address Line", "Town or City", "AB12 CD3", "Poland"))
                    )
                  )
                )
            )
          )
        val goodsLocation = GoodsLocationBuilder.build
        goodsLocation.getID should be(null)
        goodsLocation.getAddress.getLine.getValue should be("Address Line")
        goodsLocation.getAddress.getCityName.getValue should be("Town or City")
        goodsLocation.getAddress.getPostcodeID.getValue should be("AB12 CD3")
        goodsLocation.getAddress.getCountryCode.getValue should be("PL")
        goodsLocation.getName.getValue should be("Full Name")
        goodsLocation.getTypeCode should be(null)
      }

    }
  }

}
