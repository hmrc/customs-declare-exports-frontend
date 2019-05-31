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
import forms.declaration.{GoodsLocation, GoodsLocationSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class GoodsLocationBuilderSpec extends WordSpec with Matchers {

  "GoodsLocationBuilder" should {
    "correctly map GoodsLocation instance for supplementary journey " when {
      "all data is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              GoodsLocation.formId ->
                GoodsLocationSpec.correctGoodsLocationJSON
            )
          )
        val goodsLocation = GoodsLocationBuilder.build
        goodsLocation.getID.getValue should be("9GB1234567ABCDEF")
        goodsLocation.getAddress.getLine.getValue should be("Address Line")
        goodsLocation.getAddress.getCityName.getValue should be("Town or City")
        goodsLocation.getAddress.getPostcodeID.getValue should be("AB12 CD3")
        goodsLocation.getAddress.getCountryCode.getValue should be("PL")
        goodsLocation.getName.getValue should be("LOC")
        goodsLocation.getTypeCode.getValue should be("T")
      }
    }
  }
}
