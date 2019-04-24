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

class TotalNumberOfItemsSpec extends WordSpec with MustMatchers {
  import TotalNumberOfItemsSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val totalNumberOfItems = correctTotalNumberOfItemsDecimalValues
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsItemQuantity" -> totalNumberOfItems.itemsQuantity,
        "declaration.invoiceAmount.value" -> totalNumberOfItems.totalAmountInvoiced,
        "declaration.invoiceAmount.currencyId" -> "GBP",
        "declaration.currencyExchanges[0].rateNumeric" -> totalNumberOfItems.exchangeRate,
        "declaration.totalPackageQuantity" -> totalNumberOfItems.totalPackage
      )

      totalNumberOfItems.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object TotalNumberOfItemsSpec {
  val correctTotalNumberOfItemsDecimalValues = TotalNumberOfItems(
    itemsQuantity = "123",
    totalAmountInvoiced = "12312312312312.12",
    exchangeRate = "1212121.12345",
    totalPackage = "123"
  )
  val correctTotalNumberOfItemsIntegerValues = TotalNumberOfItems(
    itemsQuantity = "123",
    totalAmountInvoiced = "12312312312312",
    exchangeRate = "123123123123",
    totalPackage = "123"
  )
  val emptyTotalNumberOfItems =
    TotalNumberOfItems(itemsQuantity = "", totalAmountInvoiced = "", exchangeRate = "", totalPackage = "")

  val correctTotalNumberOfItemsDecimalValuesJSON: JsValue = JsObject(
    Map(
      "itemsQuantity" -> JsString("123"),
      "totalAmountInvoiced" -> JsString("1212312.12"),
      "exchangeRate" -> JsString("1212121.12345"),
      "totalPackage" -> JsString("123")
    )
  )
  val correctTotalNumberOfItemsIntegerValuesJSON: JsValue = JsObject(
    Map(
      "itemsQuantity" -> JsString("123"),
      "totalAmountInvoiced" -> JsString("12312312312312"),
      "exchangeRate" -> JsString("123123123123"),
      "totalPackage" -> JsString("123")
    )
  )
  val emptyTotalNumberOfItemsJSON: JsValue = JsObject(
    Map(
      "itemsQuantity" -> JsString(""),
      "totalAmountInvoiced" -> JsString(""),
      "exchangeRate" -> JsString(""),
      "totalPackage" -> JsString("")
    )
  )
}
