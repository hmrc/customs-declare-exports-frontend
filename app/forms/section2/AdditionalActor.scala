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

package forms.section2

import forms.DeclarationPage
import forms.common.Eori
import forms.mappings.MappingHelper._
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ImplicitlySequencedObject
import models.declaration.Parties.partiesPrefix
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, FieldMapping}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Format, JsValue, Json}
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, compareStringDifference, ExportsDeclarationDiff}
import uk.gov.voa.play.form.ConditionalMappings._

case class AdditionalActor(eori: Option[Eori], partyType: Option[String])
    extends DiffTools[AdditionalActor] with ImplicitlySequencedObject with AmendmentOp {

  import AdditionalActor._

  def createDiff(original: AdditionalActor, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareDifference(original.eori, eori, combinePointers(pointerString, eoriPointer, sequenceId)),
      compareStringDifference(original.partyType, partyType, combinePointers(pointerString, partyTypePointer, sequenceId))
    ).flatten

  def id: String = s"${partyType.getOrElse("")}-${eori.getOrElse("")}"

  def isDefined: Boolean = eori.isDefined && partyType.isDefined

  def toJson: JsValue = Json.toJson(this)(format)

  def getLeafPointersIfAny(pointer: ExportsFieldPointer): Seq[ExportsFieldPointer] =
    Seq(pointer)
}

object AdditionalActor extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "actors"
  val eoriPointer: ExportsFieldPointer = "eori"
  val partyTypePointer: ExportsFieldPointer = "partyType"

  lazy val keyForEori = s"${partiesPrefix}.actors.eori"
  lazy val keyForPartyType = s"${partiesPrefix}.actors.type"

  implicit val format: Format[AdditionalActor] = Json.format[AdditionalActor]

  object PartyType {
    val Consolidator = "CS"
    val Manufacturer = "MF"
    val FreightForwarder = "FW"
    val WarehouseKeeper = "WH"
  }

  import PartyType._

  private val allowedPartyTypes =
    Set(Consolidator, Manufacturer, FreightForwarder, WarehouseKeeper)

  val additionalActorsFormGroupId: String = "additionalActors"

  val mapping: Mapping[AdditionalActor] = Forms.mapping(
    "eoriCS" -> eoriMappingFor(Consolidator),
    "eoriMF" -> eoriMappingFor(Manufacturer),
    "eoriFW" -> eoriMappingFor(FreightForwarder),
    "eoriWH" -> eoriMappingFor(WarehouseKeeper),
    "partyType" -> optionalRadio("declaration.partyType.error", allowedPartyTypes.toSeq)
      .transform[Option[String]](choice => Option(choice), choice => choice.getOrElse(""))
  )(form2Model)(model2Form)

  private def eoriMappingFor(partyType: String): Mapping[Option[Eori]] =
    mandatoryIfEqual("partyType", partyType, Eori.mapping())

  private def form2Model: (Option[Eori], Option[Eori], Option[Eori], Option[Eori], Option[String]) => AdditionalActor = {
    case (eoriCS, eoriMF, eoriFW, eoriWH, partyType) =>
      val eori = Seq(eoriCS, eoriMF, eoriFW, eoriWH).zip(allowedPartyTypes).find(z => partyType.contains(z._2)).flatMap(_._1)
      AdditionalActor(eori, partyType)
  }

  private def model2Form: AdditionalActor => Option[(Option[Eori], Option[Eori], Option[Eori], Option[Eori], Option[String])] = { model =>
    model.partyType match {
      case Some(Consolidator)     => Option((model.eori, None, None, None, model.partyType))
      case Some(Manufacturer)     => Option((None, model.eori, None, None, model.partyType))
      case Some(FreightForwarder) => Option((None, None, model.eori, None, model.partyType))
      case Some(WarehouseKeeper)  => Option((None, None, None, model.eori, model.partyType))
      case _                      => Option((None, None, None, None, model.partyType))
    }
  }

  def form: Form[AdditionalActor] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.otherPartiesInvolved.common"))
}

object AdditionalActorsSummary extends DeclarationPage
