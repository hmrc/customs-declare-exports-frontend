/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section6

import forms.DeclarationPage
import forms.section3.LocationOfGoods
import forms.section6.ModeOfTransportCode.RoRo
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.requests.JourneyRequest
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.{of, optional}
import play.api.data.format.Formatter
import play.api.data.validation._
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._
import utils.validators.forms.FieldValidator._

case class TransportLeavingTheBorder(code: Option[ModeOfTransportCode] = None) extends Ordered[TransportLeavingTheBorder] with Amendment {

  override def compare(that: TransportLeavingTheBorder): Int =
    (code, that.code) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }

  def getCodeValue: String = code.getOrElse(ModeOfTransportCode.Empty).value

  def value: String = getCodeValue
}

object TransportLeavingTheBorder extends DeclarationPage with FieldMapping {

  implicit val format: OFormat[TransportLeavingTheBorder] = Json.format[TransportLeavingTheBorder]

  val pointer: String = "borderModeOfTransportCode.code"

  def form(implicit request: JourneyRequest[_]): Form[TransportLeavingTheBorder] =
    Form(mapping(request.isType(CLEARANCE), request.cacheModel.locations.goodsLocation.map(_.toForm)))

  val errorKey = "declaration.transport.leavingTheBorder.error"

  private def maybeRoRoRequired(isClearance: Boolean, maybeLocationOfGoods: Option[LocationOfGoods]): Constraint[Option[ModeOfTransportCode]] =
    Constraint[Option[ModeOfTransportCode]]("constraint.maybe.roro.required") { code =>
      def validateWhenNotGVM: ValidationResult = {
        val key = if (isClearance) s"$errorKey.empty.optional" else s"$errorKey.empty"
        if (isSome(code)) Valid else Invalid(ValidationError(key))
      }

      maybeLocationOfGoods.fold(validateWhenNotGVM) { locationOfGoods =>
        if (!locationOfGoods.code.endsWith(LocationOfGoods.suffixForGVMS)) validateWhenNotGVM
        else if (code.contains(RoRo)) Valid
        else Invalid(ValidationError(s"$errorKey.roro.required", locationOfGoods.code))
      }
    }

  def mapping(isClearance: Boolean, maybeLocationOfGoods: Option[LocationOfGoods]): Mapping[TransportLeavingTheBorder] = {
    val constraint = maybeRoRoRequired(isClearance, maybeLocationOfGoods)
    val formatter: String => Formatter[ModeOfTransportCode] =
      if (isClearance) ModeOfTransportCode.formatterForClearance else ModeOfTransportCode.formatter

    Forms.mapping("transportLeavingTheBorder" -> optional(of(formatter(s"$errorKey.incorrect"))).verifying(constraint))(
      TransportLeavingTheBorder.apply
    )(TransportLeavingTheBorder.unapply)
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.transportLeavingTheBorder.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
