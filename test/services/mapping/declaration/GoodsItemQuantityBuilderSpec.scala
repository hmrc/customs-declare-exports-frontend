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

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json._
import services.ExportsItemsCacheIds
import uk.gov.hmrc.http.cache.client.CacheMap

class GoodsItemQuantityBuilderSpec extends WordSpec with Matchers {

  "GoodsItemQuantityBuilder" should {
    "correctly map to the WCO-DEC an empty GovernmentGoodsItemQuantity instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(ExportsItemsCacheIds.itemsId -> GovernmentAgencyGoodsItemSpec.emptyGovernmentGoodsItem)
        )
      val goodsItemQuantityType = GoodsItemQuantityBuilder.build(cacheMap)
      goodsItemQuantityType.getValue.intValue() should be(0)
    }

    "correctly map to the WCO-DEC a single GovernmentGoodsItemQuantity instance" in {
      implicit val cacheMap: CacheMap =
      CacheMap(
      "CacheID",
            Map(ExportsItemsCacheIds.itemsId -> GovernmentAgencyGoodsItemSpec.singleGovernmentGoodsItem)
      )
      val goodsItemQuantityType = GoodsItemQuantityBuilder.build(cacheMap)
      goodsItemQuantityType.getValue.intValue() should be(1)
    }

    "correctly map to the WCO-DEC multiple GovernmentGoodsItemQuantity instances" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(ExportsItemsCacheIds.itemsId -> GovernmentAgencyGoodsItemSpec.multipleGovernmentGoodsItem)
        )
      val goodsItemQuantityType = GoodsItemQuantityBuilder.build(cacheMap)
      goodsItemQuantityType.getValue.intValue() should be(2)
    }
  }
}

object GovernmentAgencyGoodsItemSpec {

  val emptyGovernmentGoodsItem: JsValue =
    JsArray(
      Seq.empty
    )

  val singleGovernmentGoodsItem: JsValue =
    JsArray(
      Seq(
        JsObject(
          Map(
            "sequenceNumeric" -> JsNumber(1),
            "statisticalValueAmount" -> JsObject(Map(
              "currencyId" -> JsString("1"),
              "value" -> JsString("1")
            )),
            "commodity" -> JsObject(Map(
              "dangerousGoods" -> JsArray(),
              "classifications" -> JsArray()
            )),
            "additionalInformations" -> JsArray(),
            "additionalDocuments" -> JsArray(),
            "governmentProcedures" -> JsArray(),
            "packagings" -> JsArray()
          )
        )
      )
    )

  val multipleGovernmentGoodsItem: JsValue =
    JsArray(
      Seq(
        JsObject(
          Map(
            "sequenceNumeric" -> JsNumber(2),
            "statisticalValueAmount" -> JsObject(Map(
              "currencyId" -> JsString("34"),
              "value" -> JsString("34")
            )),
            "commodity" -> JsObject(Map(
              "dangerousGoods" -> JsArray(),
              "classifications" -> JsArray()
            )),
            "additionalInformations" -> JsArray(),
            "additionalDocuments" -> JsArray(),
            "governmentProcedures" -> JsArray(),
            "packagings" -> JsArray()
          )
        ),
        JsObject(
          Map(
            "sequenceNumeric" -> JsNumber(1),
            "statisticalValueAmount" -> JsObject(Map(
              "currencyId" -> JsString("56"),
              "value" -> JsString("56")
            )),
            "commodity" -> JsObject(Map(
              "dangerousGoods" -> JsArray(),
              "classifications" -> JsArray()
            )),
            "additionalInformations" -> JsArray(),
            "additionalDocuments" -> JsArray(),
            "governmentProcedures" -> JsArray(),
            "packagings" -> JsArray()
          )
        )
      )
    )
}