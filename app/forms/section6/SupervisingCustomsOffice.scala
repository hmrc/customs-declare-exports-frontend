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
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools.compareOptionalString
import utils.validators.forms.FieldValidator._

case class SupervisingCustomsOffice(supervisingCustomsOffice: Option[String] = None) extends Ordered[SupervisingCustomsOffice] with Amendment {

  override def compare(that: SupervisingCustomsOffice): Int =
    compareOptionalString(supervisingCustomsOffice, that.supervisingCustomsOffice)

  def value: String = supervisingCustomsOffice.getOrElse("")
}

object SupervisingCustomsOffice extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "warehouseIdentification.supervisingCustomsOffice"

  val keyForAmend = "declaration.summary.transport.supervisingOffice"

  implicit val format: OFormat[SupervisingCustomsOffice] = Json.format[SupervisingCustomsOffice]

  val fieldId = "supervisingCustomsOffice"

  val formId = "SupervisingCustomsOffice"

  val mapping = Forms
    .mapping(
      fieldId -> optional(
        text()
          .verifying("declaration.warehouse.supervisingCustomsOffice.error", isAlphanumeric and hasSpecificLength(8))
      )
    )(SupervisingCustomsOffice.apply)(SupervisingCustomsOffice.unapply)

  def form: Form[SupervisingCustomsOffice] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.supervisingCustomsOffice.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
