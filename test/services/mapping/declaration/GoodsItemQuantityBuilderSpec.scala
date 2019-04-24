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

package services.mapping.declaration
import forms.declaration.{TotalNumberOfItems, TotalNumberOfItemsSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class GoodsItemQuantityBuilderSpec extends WordSpec with Matchers {

  "GoodsItemQuantityBuilder" should {
    "correctly map to the WCO-DEC GoodsItemQuantity instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(TotalNumberOfItems.formId -> TotalNumberOfItemsSpec.correctTotalNumberOfItemsDecimalValuesJSON)
        )
      val goodsItemQuantityType = GoodsItemQuantityBuilder.build(cacheMap)
      goodsItemQuantityType.getValue.intValue() should be(123)
    }
  }
}
