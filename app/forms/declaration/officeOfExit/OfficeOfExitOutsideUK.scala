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
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class OfficeOfExitOutsideUK(officeId: String)

object OfficeOfExitOutsideUK extends DeclarationPage {
  implicit val format = Json.format[OfficeOfExitOutsideUK]

  val formId = "OfficeOfExitOutsideUk"

  val mapping = Forms.mapping(
    "officeId" -> text()
      .verifying("declaration.officeOfExitOutsideUk.empty", nonEmpty)
      .verifying("declaration.officeOfExitOutsideUk.format", isEmpty or isValidOfficeOfExit)
  )(OfficeOfExitOutsideUK.apply)(OfficeOfExitOutsideUK.unapply)

  def form(): Form[OfficeOfExitOutsideUK] = Form(OfficeOfExitOutsideUK.mapping)

  def apply(officeOfExit: OfficeOfExit): OfficeOfExitOutsideUK =
    officeOfExit.isUkOfficeOfExit match {
      case Some(AllowedUKOfficeOfExitAnswers.yes) => OfficeOfExitOutsideUK("")
      case Some(AllowedUKOfficeOfExitAnswers.no)  => OfficeOfExitOutsideUK(officeOfExit.officeId.getOrElse(""))
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.officeOfExitOutsideUk.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
