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
import forms.declaration.LocationOfGoods.suffixForGVMS
import forms.declaration.ModeOfTransportCode.RoRo
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.requests.JourneyRequest
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.{of, optional}
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError, ValidationResult}
import play.api.libs.json._
import utils.validators.forms.FieldValidator._

case class TransportLeavingTheBorder(code: Option[ModeOfTransportCode] = None) {
  def getCodeValue: String = code.getOrElse(ModeOfTransportCode.Empty).value
}

object TransportLeavingTheBorder extends DeclarationPage {

  implicit val format = Json.format[TransportLeavingTheBorder]

  def form(implicit request: JourneyRequest[_]): Form[TransportLeavingTheBorder] =
    Form(mapping(request.isType(CLEARANCE), request.cacheModel.locations.goodsLocation.map(_.toForm)))

  val errorKey = "declaration.transport.leavingTheBorder.error"

  private def maybeRoRoRequired(isClearance: Boolean, maybeLocationOfGoods: Option[LocationOfGoods]): Constraint[Option[ModeOfTransportCode]] =
    Constraint[Option[ModeOfTransportCode]]("constraint.maybe.roro.required") { code =>
      def validateWhenNotGVM: ValidationResult = {
        val key = if (isClearance) s"$errorKey.empty.optional" else s"$errorKey.empty"
        if (isPresent(code)) Valid else Invalid(ValidationError(key))
      }

      maybeLocationOfGoods.fold(validateWhenNotGVM) { locationOfGoods =>
        if (!locationOfGoods.code.endsWith(suffixForGVMS)) validateWhenNotGVM
        else if (code.exists(_ == RoRo)) Valid
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
