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

package forms.section4

import forms.DeclarationPage
import forms.mappings.MappingHelper.requiredRadio
import forms.section4.NatureOfTransaction.keyForAmend
import models.AmendmentRow.{forAddedValue, forAmendedValue, forRemovedValue, safeMessage}
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class NatureOfTransaction(natureType: String) extends Ordered[NatureOfTransaction] with Amendment {

  def value: String = natureType

  private def toUserValue(value: String)(implicit messages: Messages): String =
    safeMessage(s"declaration.summary.transaction.natureOfTransaction.$value", value)

  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    forAddedValue(pointer, messages(keyForAmend), toUserValue(value))

  def valueAmended(newValue: Amendment, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAmendedValue(pointer, messages(keyForAmend), toUserValue(value), toUserValue(newValue.value))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forRemovedValue(pointer, messages(keyForAmend), toUserValue(value))

  override def compare(y: NatureOfTransaction): Int = natureType.compareTo(y.natureType)
}

object NatureOfTransaction extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[NatureOfTransaction] = Json.format[NatureOfTransaction]

  val pointerBase: String = "natureOfTransaction"
  override val pointer: ExportsFieldPointer = s"$pointerBase.natureType"

  private val keyForAmend = "declaration.summary.transaction.natureOfTransaction"

  val formId = "TransactionType"

  val Sale = "1"
  val BusinessPurchase = "1_A"
  val Return = "2"
  val Donation = "3"
  val Processing = "4"
  val Processed = "5"
  val Military = "7"
  val Construction = "8"
  val HouseRemoval = "9_A"
  val Other = "9"

  val allowedTypes: Set[String] =
    Set(Sale, BusinessPurchase, Return, Donation, Processing, Processed, Military, Construction, HouseRemoval, Other)

  val mapping: Mapping[NatureOfTransaction] = Forms.mapping(
    "natureType" -> requiredRadio("declaration.natureOfTransaction.empty")
      .verifying("declaration.natureOfTransaction.error", isContainedIn(allowedTypes))
  )(NatureOfTransaction.apply)(NatureOfTransaction.unapply)

  def form: Form[NatureOfTransaction] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.natureOfTransaction.common"))
}
