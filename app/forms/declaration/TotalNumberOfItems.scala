/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.DeclarationPage
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class TotalNumberOfItems(exchangeRate: Option[String], totalAmountInvoiced: Option[String])

object TotalNumberOfItems extends DeclarationPage {
  implicit val format = Json.format[TotalNumberOfItems]

  val formId = "TotalNumberOfItems"

  val totalAmountInvoicedPattern = "[0-9]{1,14}|[0-9]{1,14}[.][0-9]{1,2}"
  val exchangeRatePattern = "[0-9]{1,12}|[[0-9]{1,7}[.][0-9]{1,5}]{3,13}"

  val mapping = Forms.mapping(
    "exchangeRate" -> optional(
      text()
        .verifying("declaration.exchangeRate.error", isEmpty or ofPattern(exchangeRatePattern))
    ),
    "totalAmountInvoiced" -> optional(
      text()
        .verifying("declaration.totalAmountInvoiced.error", isEmpty or ofPattern(totalAmountInvoicedPattern))
    )
  )(TotalNumberOfItems.apply)(TotalNumberOfItems.unapply)

  def form(): Form[TotalNumberOfItems] = Form(mapping)
}
