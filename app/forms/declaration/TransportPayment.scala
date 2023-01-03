/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Mapping}
import play.api.data.Forms.mapping
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class TransportPayment(paymentMethod: String)

object TransportPayment extends DeclarationPage {

  implicit val formats: OFormat[TransportPayment] = Json.format[TransportPayment]

  val cash = "A"
  val creditCard = "B"
  val cheque = "C"
  val other = "D"
  val eFunds = "H"
  val accHolder = "Y"
  val notPrePaid = "Z"
  val notAvailable = "_"

  val validPaymentMethods = Set(cash, creditCard, cheque, other, eFunds, accHolder, notPrePaid, notAvailable)

  val formMapping: Mapping[TransportPayment] = mapping(
    "paymentMethod" ->
      requiredRadio("declaration.transportInformation.transportPayment.paymentMethod.error.empty")
        .verifying("declaration.transportInformation.transportPayment.paymentMethod.error.empty", isContainedIn(validPaymentMethods))
  )(TransportPayment.apply)(TransportPayment.unapply)

  def form: Form[TransportPayment] = Form(formMapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.transportPayment.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
