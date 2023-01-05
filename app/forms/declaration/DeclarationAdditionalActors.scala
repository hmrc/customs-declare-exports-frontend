/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.DeclarationPage
import forms.MappingHelper._
import forms.common.Eori
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.voa.play.form.ConditionalMappings._

case class DeclarationAdditionalActors(eori: Option[Eori], partyType: Option[String]) {

  import DeclarationAdditionalActors._

  def id: String = s"${partyType.getOrElse("")}-${eori.getOrElse("")}"

  def isDefined: Boolean = eori.isDefined && partyType.isDefined

  def isAllowed: Boolean = partyType.exists(allowedPartyTypes.contains)

  def toJson: JsValue = Json.toJson(this)(format)
}

object DeclarationAdditionalActors extends DeclarationPage {

  def fromJsonString(value: String): Option[DeclarationAdditionalActors] = Json.fromJson(Json.parse(value)).asOpt

  implicit val format: Format[DeclarationAdditionalActors] = Json.format[DeclarationAdditionalActors]

  private val allowedPartyTypes =
    Set(PartyType.Consolidator, PartyType.Manufacturer, PartyType.FreightForwarder, PartyType.WarehouseKeeper)

  val additionalActorsFormGroupId: String = "additionalActors"

  val formId = "DeclarationAdditionalActors"

  val mapping: Mapping[DeclarationAdditionalActors] = Forms.mapping(
    "eoriCS" -> eoriMappingFor(PartyType.Consolidator),
    "eoriMF" -> eoriMappingFor(PartyType.Manufacturer),
    "eoriFW" -> eoriMappingFor(PartyType.FreightForwarder),
    "eoriWH" -> eoriMappingFor(PartyType.WarehouseKeeper),
    "partyType" -> optionalRadio("declaration.partyType.error", allowedPartyTypes.toSeq)
      .transform[Option[String]](choice => Option(choice), choice => choice.getOrElse(""))
  )(form2Model)(model2Form)

  private def eoriMappingFor(partyType: String): Mapping[Option[Eori]] =
    mandatoryIfEqual("partyType", partyType, Eori.mapping())

  private def form2Model: (Option[Eori], Option[Eori], Option[Eori], Option[Eori], Option[String]) => DeclarationAdditionalActors = {
    case (eoriCS, eoriMF, eoriFW, eoriWH, partyType) =>
      val eori = Seq(eoriCS, eoriMF, eoriFW, eoriWH).zip(allowedPartyTypes).find(z => partyType.contains(z._2)).flatMap(_._1)
      DeclarationAdditionalActors(eori, partyType)
  }

  private def model2Form: DeclarationAdditionalActors => Option[(Option[Eori], Option[Eori], Option[Eori], Option[Eori], Option[String])] = { model =>
    model.partyType match {
      case Some(PartyType.Consolidator)     => Option((model.eori, None, None, None, model.partyType))
      case Some(PartyType.Manufacturer)     => Option((None, model.eori, None, None, model.partyType))
      case Some(PartyType.FreightForwarder) => Option((None, None, model.eori, None, model.partyType))
      case Some(PartyType.WarehouseKeeper)  => Option((None, None, None, model.eori, model.partyType))
      case _                                => Option((None, None, None, None, model.partyType))
    }
  }

  def form: Form[DeclarationAdditionalActors] = Form(mapping)

  object PartyType {
    val Consolidator = "CS"
    val Manufacturer = "MF"
    val FreightForwarder = "FW"
    val WarehouseKeeper = "WH"
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.otherPartiesInvolved.common"))
}

object AdditionalActorsSummary extends DeclarationPage
