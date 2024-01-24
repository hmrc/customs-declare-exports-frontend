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

package models.declaration

import forms.declaration.EntityDetails
import models.AmendmentRow.{forAddedValue, forRemovedValue}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.AmendmentRow.safeMessage
import models.declaration.Parties.partiesPrefix
import models.declaration.RepresentativeDetails.keyForAmend
import models.{AmendmentOp, FieldMapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class RepresentativeDetails(details: Option[EntityDetails], statusCode: Option[String], representingOtherAgent: Option[String])
    extends DiffTools[RepresentativeDetails] with AmendmentOp {

  // representingOtherAgent field is not used to generate WCO XML
  override def createDiff(
    original: RepresentativeDetails,
    pointerString: ExportsFieldPointer,
    sequenceId: Option[Int] = None
  ): ExportsDeclarationDiff =
    Seq(
      createDiffOfOptions(original.details, details, combinePointers(pointerString, sequenceId)),
      compareStringDifference(original.statusCode, statusCode, combinePointers(pointerString, RepresentativeDetails.statusCodePointer, sequenceId))
    ).flatten

  private def toUserValue(value: String)(implicit messages: Messages): String =
    safeMessage(s"${partiesPrefix}.representative.type.$value", value)

  def valueAdded(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.fold("")(_.valueAdded(pointer)) +
      statusCode.fold("")(code => forAddedValue(pointer, messages(keyForAmend), toUserValue(code)))

  def valueRemoved(pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    details.fold("")(_.valueRemoved(pointer)) +
      statusCode.fold("")(code => forRemovedValue(pointer, messages(keyForAmend), toUserValue(code)))
}

object RepresentativeDetails extends FieldMapping {
  implicit val format: OFormat[RepresentativeDetails] = Json.format[RepresentativeDetails]

  val pointer: ExportsFieldPointer = "representativeDetails"
  val statusCodePointer: ExportsFieldPointer = "statusCode"

  private lazy val keyForAmend = s"${partiesPrefix}.representative.type"

  def apply(): RepresentativeDetails = new RepresentativeDetails(None, None, None)
}
