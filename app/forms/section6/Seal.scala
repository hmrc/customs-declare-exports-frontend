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

package forms.section6

import forms.DeclarationPage
import forms.section6.Seal.keyForAmend
import models.AmendmentRow.{forAddedValue, forRemovedValue}
import models.DeclarationMeta.sequenceIdPlaceholder
import models.DeclarationType.DeclarationType
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.ExplicitlySequencedObject
import models.viewmodels.TariffContentKey
import models.{AmendmentOp, DeclarationMeta, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}
import utils.validators.forms.FieldValidator._

case class Seal(sequenceId: Int = sequenceIdPlaceholder, id: String) extends DiffTools[Seal] with ExplicitlySequencedObject[Seal] with AmendmentOp {

  override def createDiff(original: Seal, pointerString: ExportsFieldPointer, sequenceId: Option[Int]): ExportsDeclarationDiff =
    Seq(compareStringDifference(original.id, id, combinePointers(pointerString, sequenceId))).flatten

  override def updateSequenceId(sequenceId: Int): Seal = copy(sequenceId = sequenceId)

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forAddedValue(pointer, messages(keyForAmend), id)

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    forRemovedValue(pointer, messages(keyForAmend), id)
}

object Seal extends DeclarationPage with FieldMapping {

  val keyForAmend = "declaration.summary.container.securitySeal"

  implicit val format: OFormat[Seal] = Json.format[Seal]

  val formId = "Seal"

  val formMapping: Mapping[Seal] = Forms.mapping(
    "id" ->
      text()
        .verifying("declaration.transport.sealId.empty.error", nonEmpty)
        .verifying("declaration.transport.sealId.error.invalid", isEmpty or (isAlphanumeric and noLongerThan(20)))
  )(form2Data)(data2Form)

  def form2Data(id: String): Seal =
    Seal(DeclarationMeta.sequenceIdPlaceholder, id)

  def data2Form(seal: Seal): Option[String] =
    Some(seal.id)

  def form: Form[Seal] = Form(formMapping)
  val sealsAllowed = 9999

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.containers.seals.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))

  override val pointer: ExportsFieldPointer = "seals"
}
