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

case class TransportPayment(paymentMethod: Option[String] = None)

object TransportPayment extends DeclarationPage {

  implicit val formats: OFormat[TransportPayment] = Json.format[TransportPayment]

  // TODO - replace with enum?
  val cash = "A"
  val creditCard = "B"
  val cheque = "C"
  val other = "D"
  val eFunds = "H"
  val accHolder = "Y"
  val notPrePaid = "Z"

  val paymentMethods = Map(
    cash -> "standard.transportDetails.paymentMethod.cash",
    creditCard -> "standard.transportDetails.paymentMethod.creditCard",
    cheque -> "standard.transportDetails.paymentMethod.cheque",
    other -> "standard.transportDetails.paymentMethod.other",
    eFunds -> "standard.transportDetails.paymentMethod.eFunds",
    accHolder -> "standard.transportDetails.paymentMethod.accHolder",
    notPrePaid -> "standard.transportDetails.paymentMethod.notPrePaid"
  )

  val formMapping: Mapping[TransportPayment] = mapping(
    "paymentMethod" -> optional(
      text()
        .verifying("standard.transportDetails.paymentMethod.error", isContainedIn(paymentMethods.keys))
    )
  )(TransportPayment.apply)(TransportPayment.unapply)

  def form(): Form[TransportPayment] = Form(formMapping)


}
