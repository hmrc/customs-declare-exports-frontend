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

import play.api.libs.json.{JsObject, JsString, JsValue}

object TotalNumberOfItemsSpec {
  val correctTotalNumberOfItemsDecimalValues =
    TotalNumberOfItems(Some("12312312312312.12"), Some("1212121.12345"), "123")
  val correctTotalNumberOfItemsDecimalValuesJSON: JsValue = JsObject(
    Map("totalAmountInvoiced" -> JsString("1212312.12"), "exchangeRate" -> JsString("1212121.12345"), "totalPackage" -> JsString("123"))
  )
}
