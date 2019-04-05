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

package forms.declaration

import forms.MetadataPropertiesConvertable
import forms.declaration.TransportInformation.allowedModeOfTransportCodes
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class WarehouseIdentification(
  supervisingCustomsOffice: Option[String],
  identificationNumber: Option[String],
  inlandModeOfTransportCode: Option[String]
) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map(
      "declaration.goodsShipment.warehouse.typeCode" -> identificationNumber.flatMap(_.headOption).fold("")(_.toString),
      "declaration.goodsShipment.warehouse.id" -> identificationNumber.map(_.drop(1).toString).getOrElse(""),
      "declaration.supervisingOffice.id" -> supervisingCustomsOffice.getOrElse(""),
      "declaration.goodsShipment.consignment.arrivalTransportMeans.modeCode" -> inlandModeOfTransportCode.getOrElse("")
    )
}

object WarehouseIdentification {
  implicit val format = Json.format[WarehouseIdentification]

  val formId = "IdentificationOfWarehouse"

  val mapping = Forms.mapping(
    "supervisingCustomsOffice" -> optional(
      text()
        .verifying("supplementary.warehouse.supervisingCustomsOffice.error", isAlphanumeric and hasSpecificLength(8))
    ),
    "identificationNumber" -> optional(
      text().verifying(
        "supplementary.warehouse.identificationNumber.error",
        startsWithCapitalLetter and noShorterThan(2) and noLongerThan(36) and isAlphanumeric
      )
    ),
    "inlandModeOfTransportCode" -> optional(
      text()
        .verifying(
          "supplementary.warehouse.inlandTransportMode.error.incorrect",
          isContainedIn(allowedModeOfTransportCodes)
        )
    )
  )(WarehouseIdentification.apply)(WarehouseIdentification.unapply)

  def form(): Form[WarehouseIdentification] = Form(mapping)
}
