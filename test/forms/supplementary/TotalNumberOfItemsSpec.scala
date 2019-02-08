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

package forms.supplementary
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class TotalNumberOfItemsSpec extends WordSpec with MustMatchers {
  import TotalNumberOfItemsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val totalNumberOfItems = correctTotalNumberOfItemsDecimalValues
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsItemQuantity" -> totalNumberOfItems.itemsNo,
        "declaration.invoiceAmount" -> totalNumberOfItems.totalAmountInvoiced.get,
        "declaration.currencyExchanges[0].rateNumeric" -> totalNumberOfItems.exchangeRate.get,
        "declaration.totalPackageQuantity" -> totalNumberOfItems.totalPackage
      )

      totalNumberOfItems.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object TotalNumberOfItemsSpec {
  val correctTotalNumberOfItemsDecimalValues = TotalNumberOfItems(
    itemsNo = "123",
    totalAmountInvoiced = Some("12312312312312.12"),
    exchangeRate = Some("1212121.12345"),
    totalPackage = "123"
  )
  val correctTotalNumberOfItemsIntegerValues = TotalNumberOfItems(
    itemsNo = "123",
    totalAmountInvoiced = Some("12312312312312"),
    exchangeRate = Some("123123123123"),
    totalPackage = "123"
  )
  val emptyTotalNumberOfItems =
    TotalNumberOfItems(itemsNo = "", totalAmountInvoiced = None, exchangeRate = None, totalPackage = "")

  val correctTotalNumberOfItemsDecimalValuesJSON: JsValue = JsObject(
    Map(
      "items" -> JsString("123"),
      "totalAmountInvoiced" -> JsString("12312312312312.12"),
      "exchangeRate" -> JsString("1212121.12345"),
      "totalPackage" -> JsString("123")
    )
  )
  val correctTotalNumberOfItemsIntegerValuesJSON: JsValue = JsObject(
    Map(
      "items" -> JsString("123"),
      "totalAmountInvoiced" -> JsString("12312312312312"),
      "exchangeRate" -> JsString("123123123123"),
      "totalPackage" -> JsString("123")
    )
  )
  val emptyTotalNumberOfItemsJSON: JsValue = JsObject(
    Map(
      "items" -> JsString(""),
      "totalAmountInvoiced" -> JsString(""),
      "exchangeRate" -> JsString(""),
      "totalPackage" -> JsString("")
    )
  )
}
