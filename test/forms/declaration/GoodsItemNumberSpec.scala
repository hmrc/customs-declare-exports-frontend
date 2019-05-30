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

package forms.declaration
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class GoodsItemNumberSpec extends WordSpec with MustMatchers {
  import GoodsItemNumberSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val goodsItemNumber = correctGoodsItemNumber
      val expectedMetadataProperties: Map[String, String] =
        Map(
          "declaration.goodsShipment.governmentAgencyGoodsItems[0].sequenceNumeric" -> goodsItemNumber.goodItemNumber,
          "declaration.goodsItemQuantity" -> goodsItemNumber.goodItemNumber
        )

      goodsItemNumber.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }
}

object GoodsItemNumberSpec {
  val correctGoodsItemNumber = GoodsItemNumber(goodItemNumber = "123")
  val emptyGoodsItemNumber = GoodsItemNumber(goodItemNumber = "")

  val correctGoodsItemNumberJSON: JsValue = JsObject(Map("goodItemNumber" -> JsString("123")))
  val emptyGoodsItemNumberJSON: JsValue = JsObject(Map("goodItemNumber" -> JsString("")))
}
