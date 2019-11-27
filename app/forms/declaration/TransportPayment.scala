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
import forms.DeclarationPage
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class TransportPayment(paymentMethod: Option[String] = None) {

  def extractPaymentType: Option[String] = paymentMethod.map {
    case TransportPayment.cash       => "Payment in cash"
    case TransportPayment.creditCard => "Payment by credit card"
    case TransportPayment.cheque     => "Payment by cheque"
    case TransportPayment.other      => "Other (e.g. Direct debit to cash account)"
    case TransportPayment.eFunds     => "Electronic funds transfer"
    case TransportPayment.accHolder  => "Account holder with carrier"
    case TransportPayment.notPrePaid => "Not pre-paid"
    case _                           => "Incorrect"
  }
}

object TransportPayment extends DeclarationPage {

  implicit val formats: OFormat[TransportPayment] = Json.format[TransportPayment]

  val cash = "A"
  val creditCard = "B"
  val cheque = "C"
  val other = "D"
  val eFunds = "H"
  val accHolder = "Y"
  val notPrePaid = "Z"

  val validPaymentMethods = Set(cash, creditCard, cheque, other, eFunds, accHolder, notPrePaid)

  val formMapping: Mapping[TransportPayment] = mapping(
    "paymentMethod" -> optional(
      text()
        .verifying("standard.transportDetails.paymentMethod.error", isContainedIn(validPaymentMethods))
    )
  )(TransportPayment.apply)(TransportPayment.unapply)

  def form(): Form[TransportPayment] = Form(formMapping)

}
