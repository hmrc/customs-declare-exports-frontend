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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class TotalNumberOfItems(totalAmountInvoiced: Option[String], exchangeRate: Option[String], totalPackage: String)

object TotalNumberOfItems {
  implicit val format = Json.format[TotalNumberOfItems]

  val formId = "TotalNumberOfItems"

  val totalAmountInvoicedPattern = "[0-9]{1,14}|[0-9]{1,14}[.][0-9]{1,2}"
  val exchangeRatePattern = "[0-9]{1,12}|[[0-9]{1,7}[.][0-9]{1,5}]{3,13}"

  val mapping = Forms.mapping(
    "totalAmountInvoiced" -> optional(
      text()
        .verifying("supplementary.totalAmountInvoiced.error", isEmpty or ofPattern(totalAmountInvoicedPattern))
    ),
    "exchangeRate" -> optional(
      text()
        .verifying("supplementary.exchangeRate.error", isEmpty or ofPattern(exchangeRatePattern))
    ),
    "totalPackage" -> text()
      .verifying("supplementary.totalPackageQuantity.empty", nonEmpty)
      .verifying("supplementary.totalPackageQuantity.error", isEmpty or (isNumeric and noLongerThan(8)))
  )(TotalNumberOfItems.apply)(TotalNumberOfItems.unapply)

  def form(): Form[TotalNumberOfItems] = Form(mapping)
}
