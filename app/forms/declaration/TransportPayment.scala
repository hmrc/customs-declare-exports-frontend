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
import forms.declaration.TransportPayment.keyForAmend
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue, safeMessage}
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.mapping
import play.api.data.{Form, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.isContainedIn

case class TransportPayment(paymentMethod: String) extends Ordered[TransportPayment] with Amendment {

  def value: String = paymentMethod

  private def toUserValue(value: String)(implicit messages: Messages): String =
    safeMessage(s"$keyForAmend.$value", value)

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAddedValue(pointer, messages(keyForAmend), toUserValue(value))

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(keyForAmend), toUserValue(value), toUserValue(newValue.value))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forRemovedValue(pointer, messages(keyForAmend), toUserValue(value))

  override def compare(that: TransportPayment): Int = paymentMethod.compare(that.paymentMethod)
}

object TransportPayment extends DeclarationPage with FieldMapping {

  override val pointer: ExportsFieldPointer = "transportPayment.paymentMethod"

  private val keyForAmend = "declaration.summary.transport.payment"

  implicit val formats: OFormat[TransportPayment] = Json.format[TransportPayment]

  val cash = "A"
  val creditCard = "B"
  val cheque = "C"
  val other = "D"
  val eFunds = "H"
  val accHolder = "Y"
  val notPrePaid = "Z"
  val notAvailable = "_"

  private val validPaymentMethods = Set(cash, creditCard, cheque, other, eFunds, accHolder, notPrePaid, notAvailable)

  private val prefix = "declaration.transportInformation.transportPayment.paymentMethod"

  val formMapping: Mapping[TransportPayment] = mapping(
    "paymentMethod" ->
      requiredRadio(s"$prefix.error.empty")
        .verifying(s"$prefix.error.empty", isContainedIn(validPaymentMethods))
  )(TransportPayment.apply)(TransportPayment.unapply)

  def form: Form[TransportPayment] = Form(formMapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.transportPayment.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
