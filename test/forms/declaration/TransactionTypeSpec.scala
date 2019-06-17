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

object TransactionTypeSpec {
  val correctTransactionType = TransactionType(documentTypeCode = "1", identifier = Some("1"))

  val correctTransactionTypeJSON: JsValue = JsObject(
    Map("documentTypeCode" -> JsString("1"), "identifier" -> JsString("1"))
  )
  val emptyTransactionTypeJSON: JsValue = JsObject(
    Map("documentTypeCode" -> JsString(""), "identifier" -> JsString(""))
  )
  val incorrectTransactionTypeJSON: JsValue = JsObject(
    Map("documentTypeCode" -> JsString("123"), "identifier" -> JsString("123"))
  )
}
