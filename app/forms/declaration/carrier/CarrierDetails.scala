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

package forms.declaration.carrier

import forms.DeclarationPage
import forms.declaration.EntityDetails
import models.DeclarationType.{CLEARANCE, DeclarationType}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class CarrierDetails(details: EntityDetails)

object CarrierDetails extends DeclarationPage {
  implicit val format = Json.format[CarrierDetails]

  val id = "CarrierDetails"

  val defaultMapping = Forms.mapping("details" -> EntityDetails.addressMapping)(CarrierDetails.apply)(CarrierDetails.unapply)
  val optionalMapping = Forms.mapping("details" -> EntityDetails.optionalAddressMapping)(CarrierDetails.apply)(CarrierDetails.unapply)

  def form(declarationType: DeclarationType): Form[CarrierDetails] = declarationType match {
    case CLEARANCE => Form(optionalMapping)
    case _         => Form(defaultMapping)
  }

  def from(carrierEoriDetails: CarrierEoriNumber, savedCarrierDetails: Option[CarrierDetails]): CarrierDetails =
    carrierEoriDetails.eori match {
      case None =>
        savedCarrierDetails.flatMap(_.details.address) match {
          case None          => CarrierDetails(EntityDetails(None, None))
          case Some(address) => CarrierDetails(EntityDetails(None, Some(address)))
        }
      case Some(_) => CarrierDetails(EntityDetails(carrierEoriDetails.eori, None))
    }
}
