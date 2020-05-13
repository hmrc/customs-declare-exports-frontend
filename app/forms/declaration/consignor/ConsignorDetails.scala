/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.declaration.consignor

import forms.DeclarationPage
import forms.common.Address
import forms.declaration.EntityDetails
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json

case class ConsignorDetails(details: EntityDetails)

object ConsignorDetails extends DeclarationPage {
  implicit val format = Json.format[ConsignorDetails]

  val id = "ConsignorDetails"

  val consignorMapping: Mapping[EntityDetails] =
    Forms.mapping("address" -> Address.mapping)(address => EntityDetails(None, Some(address)))(entityDetails => entityDetails.address)

  val mapping = Forms.mapping("details" -> consignorMapping)(ConsignorDetails.apply)(ConsignorDetails.unapply)

  def form(): Form[ConsignorDetails] = Form(mapping)

  def from(consignorEoriDetails: ConsignorEoriNumber, savedConsignorDetails: Option[ConsignorDetails]): ConsignorDetails =
    consignorEoriDetails.eori match {
      case None =>
        savedConsignorDetails.flatMap(_.details.address) match {
          case None          => ConsignorDetails(EntityDetails(None, None))
          case Some(address) => ConsignorDetails(EntityDetails(None, Some(address)))
        }
      case Some(_) => ConsignorDetails(EntityDetails(consignorEoriDetails.eori, None))
    }
}
