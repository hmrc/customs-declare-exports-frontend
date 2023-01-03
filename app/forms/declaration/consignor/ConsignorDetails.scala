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

package forms.declaration.consignor

import connectors.CodeListConnector
import forms.DeclarationPage
import forms.declaration.EntityDetails
import models.viewmodels.TariffContentKey
import models.DeclarationType.DeclarationType
import play.api.data.{Form, Forms}
import play.api.i18n.Messages
import play.api.libs.json.Json

case class ConsignorDetails(details: EntityDetails)

object ConsignorDetails extends DeclarationPage {
  implicit val format = Json.format[ConsignorDetails]

  val id = "ConsignorDetails"

  def mapping(implicit messages: Messages, codeListConnector: CodeListConnector) =
    Forms.mapping("details" -> EntityDetails.addressMapping)(ConsignorDetails.apply)(ConsignorDetails.unapply)

  def form(implicit messages: Messages, codeListConnector: CodeListConnector): Form[ConsignorDetails] = Form(mapping)

  def from(consignorEoriDetails: ConsignorEoriNumber, savedConsignorDetails: Option[ConsignorDetails]): ConsignorDetails =
    consignorEoriDetails.eori match {
      case None =>
        savedConsignorDetails.flatMap(_.details.address) match {
          case None          => ConsignorDetails(EntityDetails(None, None))
          case Some(address) => ConsignorDetails(EntityDetails(None, Some(address)))
        }
      case Some(_) => ConsignorDetails(EntityDetails(consignorEoriDetails.eori, None))
    }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.consignorEoriNumber.clearance"))
}
