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

package services.mapping.goodsshipment

import forms.declaration.{ConsigneeDetails, ConsigneeDetailsSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class ConsigneeBuilderSpec extends WordSpec with Matchers {

  "ConsigneeBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.Consignee instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(ConsigneeDetails.id -> ConsigneeDetailsSpec.correctConsigneeDetailsJSON))
      val consignee = ConsigneeBuilder.build(cacheMap)
      consignee.getID.getValue should be("9GB1234567ABCDEF")
      consignee.getName.getValue should be("Full Name")
      consignee.getAddress.getLine.getValue should be("Address Line")
      consignee.getAddress.getCityName.getValue should be("Town or City")
      consignee.getAddress.getCountryCode.getValue should be("PL")
      consignee.getAddress.getPostcodeID.getValue should be("AB12 34CD")
    }
  }
}
