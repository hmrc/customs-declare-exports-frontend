/*
 * Copyright 2021 HM Revenue & Customs
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
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class TotalNumberOfItems(exchangeRate: Option[String], totalAmountInvoiced: Option[String])

object TotalNumberOfItems extends DeclarationPage {
  implicit val format = Json.format[TotalNumberOfItems]

  val formId = "TotalNumberOfItems"
  val exchangeRate = "exchangeRate"
  val totalAmountInvoiced = "totalAmountInvoiced"

  val rateFieldErrorKey = "declaration.exchangeRate.error"
  val invoiceFieldErrorKey = "declaration.totalAmountInvoiced.error"

  val totalAmountInvoicedPattern = Seq("[0-9]{0,16}[.]{0,1}", "[0-9]{0,15}[.][0-9]{1}", "[0-9]{0,14}[.][0-9]{1,2}").mkString("|")

  val exchangeRatePattern = Seq(
    "[0-9]{0,12}[.]{0,1}",
    "[0-9]{0,11}[.][0-9]{1}",
    "[0-9]{0,10}[.][0-9]{1,2}",
    "[0-9]{0,9}[.][0-9]{1,3}",
    "[0-9]{0,8}[.][0-9]{1,4}",
    "[0-9]{0,7}[.][0-9]{1,5}"
  ).mkString("|")

  val removeCommasFirst = (validator: String => Boolean) => (input: String) => validator(input.replaceAll(",", ""))
  val notJustCommas = (input: String) => !input.forall(_.equals(','))

  //We allow the user to enter commas when specifying these optional numerical values but we strip out the commas with `removeCommasFirst` before validating
  //the number of digits. To prevent the validation from allowing an invalid value like ",,,," we also must use the `notJustCommas`
  //function to specifically guard against this.
  val mapping = Forms.mapping(
    exchangeRate -> optional(
      text()
        .verifying(rateFieldErrorKey, isEmpty or (notJustCommas and removeCommasFirst(ofPattern(exchangeRatePattern))))
    ),
    totalAmountInvoiced -> optional(
      text()
        .verifying(invoiceFieldErrorKey, isEmpty or (notJustCommas and removeCommasFirst(ofPattern(totalAmountInvoicedPattern))))
    )
  )(TotalNumberOfItems.apply)(TotalNumberOfItems.unapply)

  def form(): Form[TotalNumberOfItems] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.totalNumbersOfItems.1.common"), TariffContentKey("tariff.declaration.totalNumbersOfItems.2.common"))
}
