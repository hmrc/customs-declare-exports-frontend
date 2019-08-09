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

import forms.declaration.TransportCodes._
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class WarehouseIdentification(
  supervisingCustomsOffice: Option[String],
  identificationType: Option[String],
  identificationNumber: Option[String],
  inlandModeOfTransportCode: Option[String]
)

object WarehouseIdentification {
  implicit val format = Json.format[WarehouseIdentification]

  val formId = "IdentificationOfWarehouse"

  val mapping = Forms
    .mapping(
      "supervisingCustomsOffice" -> optional(
        text()
          .verifying("supplementary.warehouse.supervisingCustomsOffice.error", isAlphanumeric and hasSpecificLength(8))
      ),
      "identificationType" -> optional(
        text().verifying("supplementary.warehouse.identificationType.error", isContainedIn(IdentifierType.all))
      ),
      "identificationNumber" -> optional(
        text().verifying(
          "supplementary.warehouse.identificationNumber.error",
          noShorterThan(1) and noLongerThan(35) and isAlphanumeric
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
    .verifying("supplementary.warehouse.identificationNumberNoType.error", typeSelectedWhenNumberIsPopulated)
    .verifying("supplementary.warehouse.identificationTypeNoNumber.error", idNumberIsPopulatedWhenIDTypeIsSelected)

  private def typeSelectedWhenNumberIsPopulated: WarehouseIdentification => Boolean =
    warehouseIdentification =>
      warehouseIdentification.identificationNumber.isEmpty || warehouseIdentification.identificationType.exists(
        _.nonEmpty
    )

  private def idNumberIsPopulatedWhenIDTypeIsSelected: WarehouseIdentification => Boolean =
    warehouseIdentification =>
      warehouseIdentification.identificationType.isEmpty || warehouseIdentification.identificationNumber.exists(
        _.nonEmpty
    )

  def form(): Form[WarehouseIdentification] = Form(mapping)

  object IdentifierType {
    val PUBLIC_CUSTOMS_1 = "R"
    val PUBLIC_CUSTOMS_2 = "S"
    val PUBLIC_CUSTOMS_3 = "T"
    val PRIVATE_CUSTOMS = "U"
    val NON_CUSTOMS = "Y"
    val FREE_ZONE = "Z"

    lazy val all: Seq[String] =
      Seq(PUBLIC_CUSTOMS_1, PUBLIC_CUSTOMS_2, PUBLIC_CUSTOMS_3, PRIVATE_CUSTOMS, NON_CUSTOMS, FREE_ZONE)
  }
}
