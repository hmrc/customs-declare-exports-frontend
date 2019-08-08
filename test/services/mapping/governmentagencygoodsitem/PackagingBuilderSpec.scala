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

package services.mapping.governmentagencygoodsitem

import org.scalatest.{Matchers, WordSpec}
import services.GoodsItemCachingData
import services.cache.ExportsItemBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class PackagingBuilderSpec extends WordSpec with Matchers with GoodsItemCachingData with ExportsItemBuilder {

  "PackageBuilder" should {

    "build then add" when {
      "empty list" in {
        val model = anItem(withoutPackageInformation())
        val wcoItem = new GoodsShipment.GovernmentAgencyGoodsItem()

        builder.buildThenAdd(model, wcoItem)

        wcoItem.getPackaging shouldBe empty
      }

      "populated list" in {
        val model = anItem(withPackageInformation(Some("types"), Some(123), Some("marks")))
        val wcoItem = new GoodsShipment.GovernmentAgencyGoodsItem()

        builder.buildThenAdd(model, wcoItem)

        wcoItem.getPackaging should have(size(1))
        wcoItem.getPackaging.get(0).getSequenceNumeric.intValue shouldBe 0
        wcoItem.getPackaging.get(0).getTypeCode.getValue shouldBe "types"
        wcoItem.getPackaging.get(0).getQuantityQuantity.getValue.intValue shouldBe 123
        wcoItem.getPackaging.get(0).getMarksNumbersID.getValue shouldBe "marks"
      }
    }
  }

  private def builder = new PackagingBuilder()
}
