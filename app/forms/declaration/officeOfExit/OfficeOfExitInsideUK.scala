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

package forms.declaration.officeOfExit

import forms.DeclarationPage
import forms.Mapping.requiredRadio
import forms.declaration.officeOfExit.AllowedUKOfficeOfExitAnswers.allowedCodes
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.text
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class OfficeOfExitInsideUK(officeId: Option[String], isUkOfficeOfExit: String)

object OfficeOfExitInsideUK extends DeclarationPage {
  implicit val format: OFormat[OfficeOfExitInsideUK] = Json.format[OfficeOfExitInsideUK]

  private val ukOfficeOfExit = "isUkOfficeOfExit"
  private val officeId = "officeId"

  val formId = "OfficeOfExit"

  val mapping: Mapping[OfficeOfExitInsideUK] = Forms.mapping(
    officeId -> mandatoryIfEqual(
      ukOfficeOfExit,
      AllowedUKOfficeOfExitAnswers.yes,
      text()
        .verifying("declaration.officeOfExit.empty", nonEmpty)
        .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
        .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric)
    ),
    ukOfficeOfExit -> requiredRadio("declaration.officeOfExit.isUkOfficeOfExit.empty")
      .verifying("declaration.officeOfExit.isUkOfficeOfExit.empty", isContainedIn(allowedCodes))
  )(OfficeOfExitInsideUK.apply)(OfficeOfExitInsideUK.unapply)

  def form(): Form[OfficeOfExitInsideUK] = Form(OfficeOfExitInsideUK.mapping)

  def apply(officeOfExit: OfficeOfExit): OfficeOfExitInsideUK =
    officeOfExit.isUkOfficeOfExit match {
      case Some(AllowedUKOfficeOfExitAnswers.yes) =>
        OfficeOfExitInsideUK(officeId = officeOfExit.officeId, isUkOfficeOfExit = AllowedUKOfficeOfExitAnswers.yes)
      case _ => OfficeOfExitInsideUK(officeId = None, isUkOfficeOfExit = AllowedUKOfficeOfExitAnswers.no)
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.officeOfExit.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
