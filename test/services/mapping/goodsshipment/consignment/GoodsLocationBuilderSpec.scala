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
import forms.declaration.GoodsLocation
import forms.declaration.GoodsLocationTestData.correctGoodsLocationJSON
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class GoodsLocationBuilderSpec extends WordSpec with Matchers {

  "GoodsLocationBuilder" should {
    
    "correctly map GoodsLocation instance for supplementary journey" when {
     
      "all data is supplied" in {
        
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(GoodsLocation.formId -> correctGoodsLocationJSON))
        
        val goodsLocation = GoodsLocationBuilder.build
        goodsLocation.getID.getValue shouldBe "LOC"
        goodsLocation.getAddress.getLine.getValue shouldBe "Address Line"
        goodsLocation.getAddress.getCityName.getValue shouldBe "Town or City"
        goodsLocation.getAddress.getPostcodeID.getValue shouldBe "AB12 CD3"
        goodsLocation.getAddress.getCountryCode.getValue shouldBe "PL"
        goodsLocation
          .getName
          .getValue shouldBe "9GB1234567ABCDEF"
        goodsLocation.getTypeCode.getValue shouldBe "T"
        goodsLocation.getAddress.getTypeCode shouldBe "Y"
      }
    }
  }
}
