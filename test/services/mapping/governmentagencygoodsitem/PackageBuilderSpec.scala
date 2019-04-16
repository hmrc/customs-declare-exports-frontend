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
import uk.gov.hmrc.http.cache.client.CacheMap

class PackageBuilderSpec extends WordSpec with Matchers with GovernmentAgencyGoodsItemMocks {

  "PackageBuilder" should {
    "map correctly to wco Packaging" in {
      implicit val cacheMap = mock[CacheMap]
      setUpPackageInformation()

      val mappedItems = PackageBuilder.build.getOrElse(Seq.empty)

      mappedItems.length should be(1)
      val wcoPackaging =  mappedItems.head
      wcoPackaging.getQuantityQuantity.getValue shouldBe BigDecimal(packageQuantity).bigDecimal
      wcoPackaging.getMarksNumbersID.getValue shouldBe shippingMarksValue
      wcoPackaging.getTypeCode.getValue shouldBe packageTypeValue

    }
  }

}
