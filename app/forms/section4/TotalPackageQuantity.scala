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

package forms.section4

import forms.DeclarationPage
import models.DeclarationType.{DeclarationType, _}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}

case class TotalPackageQuantity(totalPackage: Option[String])

object TotalPackageQuantity extends DeclarationPage {
  implicit val format: OFormat[TotalPackageQuantity] = Json.format[TotalPackageQuantity]

  val formId = "TotalPackageQuantity"

  def applyRequired(value: String): TotalPackageQuantity = TotalPackageQuantity(if (value.isEmpty) None else Some(value))

  def unapplyRequired(value: TotalPackageQuantity): Option[String] = value.totalPackage

  import utils.validators.forms.FieldValidator._

  private val optionalMapping = Forms.mapping(
    "totalPackage" -> optional(
      text()
        .verifying("declaration.totalPackageQuantity.error", isNumeric and noLongerThan(8))
    )
  )(TotalPackageQuantity.apply)(TotalPackageQuantity.unapply)

  private val requiredMapping = Forms.mapping(
    "totalPackage" -> text()
      .verifying("declaration.totalPackageQuantity.empty", nonEmpty)
      .verifying("declaration.totalPackageQuantity.error", isEmpty or (isNumeric and noLongerThan(8)))
  )(TotalPackageQuantity.applyRequired)(TotalPackageQuantity.unapplyRequired)

  def form(declarationType: DeclarationType): Form[TotalPackageQuantity] = declarationType match {
    case STANDARD | SUPPLEMENTARY => Form(requiredMapping)
    case CLEARANCE                => Form(optionalMapping)
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.totalPackageQuantity.common"))
}
