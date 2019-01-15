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

package forms.supplementary

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator.noLongerThan

case class WarehouseIdentification(typeCode: Option[String], id: Option[String])

object WarehouseIdentification {
  implicit val format = Json.format[WarehouseIdentification]

  val formId = "IdentificationOfWarehouse"

  val mapping = Forms.mapping(
    "typeCode" -> optional(
      text().verifying("supplementary.warehouse.typeCode.error", noLongerThan(_, 35))
    ), "identificationNumber" -> optional(
      text().verifying("supplementary.warehouse.identificationNumber.error", noLongerThan(_, 35))
    )
  )(WarehouseIdentification.apply)(WarehouseIdentification.unapply)

  def form(): Form[WarehouseIdentification] = Form(mapping)

  def toMetadataProperties(holder: DeclarationHolder): Map[String, String] =
    Map(
      "declaration.goodsShipment.warehouse.typeCode" -> holder.authorisationTypeCode.getOrElse(""),
      "declaration.goodsShipment.warehouse.ID" -> holder.eori.getOrElse("")
    )
}