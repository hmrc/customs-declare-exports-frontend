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

import models.declaration.governmentagencygoodsitem.Packaging
import org.scalatest.{Matchers, WordSpec}
import services.GoodsItemCachingData

class PackagingBuilderSpec
    extends WordSpec with Matchers with GovernmentAgencyGoodsItemMocks with GoodsItemCachingData {

  "PackageBuilder" should {
    "map correctly to wco Packaging" in {

      val packagingInformation = Packaging(Some(1), Some("123"), Some(12), Some("R"))

      val items = PackagingBuilder.build(Seq(packagingInformation))

      items.size() should be(1)
      items.get(0).getQuantityQuantity.getValue shouldBe BigDecimal(packagingInformation.quantity.get).bigDecimal
      items.get(0).getMarksNumbersID.getValue shouldBe packagingInformation.marksNumbersId.get
      items.get(0).getTypeCode.getValue shouldBe packagingInformation.typeCode.get
    }
  }

}
