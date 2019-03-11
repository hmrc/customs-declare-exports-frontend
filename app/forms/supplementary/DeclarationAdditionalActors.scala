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
import play.api.libs.json.{JsValue, Json}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfNot

case class DeclarationAdditionalActors(eori: Option[String], partyType: Option[String]) {
  implicit val writes = Json.writes[DeclarationAdditionalActors]

  def isDefined: Boolean = eori.isDefined && partyType.isDefined

  def toJson: JsValue = Json.toJson(this)
}

object DeclarationAdditionalActors {

  def fromJson(value: JsValue): DeclarationAdditionalActors = Json.fromJson(value).get

  implicit val format = Json.format[DeclarationAdditionalActors]

  private val allowedPartyTypes =
    Set(PartyType.Consolidator, PartyType.Manufacturer, PartyType.FreightForwarder, PartyType.WarehouseKeeper)

  val formId = "DeclarationAdditionalActors"

  val eoriPattern = "[0-9a-zA-Z]{1,17}"

  val mapping = Forms.mapping(
    "eori" -> optional(text().verifying("supplementary.eori.error", _.matches(eoriPattern))),
    "partyType" -> mandatoryIfNot(
      "eori",
      "",
      text().verifying("supplementary.partyType.error", input => allowedPartyTypes(input))
    )
  )(DeclarationAdditionalActors.apply)(DeclarationAdditionalActors.unapply)

  def form(): Form[DeclarationAdditionalActors] = Form(mapping)

  object PartyType {
    val Consolidator = "CS"
    val Manufacturer = "MF"
    val FreightForwarder = "FW"
    val WarehouseKeeper = "WH"
  }
}
