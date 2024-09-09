/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.section2.EntityDetails
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareStringDifference, ExportsDeclarationDiff}

case class RepresentativeDetails(details: Option[EntityDetails], statusCode: Option[String], representingOtherAgent: Option[String])
    extends DiffTools[RepresentativeDetails] {

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
}

object RepresentativeDetails extends FieldMapping {
  implicit val format: OFormat[RepresentativeDetails] = Json.format[RepresentativeDetails]

  val pointer: ExportsFieldPointer = "representativeDetails"
  val statusCodePointer: ExportsFieldPointer = "statusCode"

  def apply(): RepresentativeDetails = new RepresentativeDetails(None, None, None)
}
