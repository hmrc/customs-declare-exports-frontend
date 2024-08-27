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

package forms.section1

import models.DeclarationType._
import play.api.libs.json.{Format, JsString, Reads, Writes}

object AdditionalDeclarationType extends Enumeration {

  type AdditionalDeclarationType = Value

  implicit val format: Format[AdditionalDeclarationType.Value] =
    Format(
      Reads(
        _.validate[String]
          .filter(`type` => AdditionalDeclarationType.values.exists(_.toString == `type`))
          .map(AdditionalDeclarationType.from(_).get)
      ),
      Writes(value => JsString(value.toString))
    )

  val SUPPLEMENTARY_SIMPLIFIED = Value("Y")
  val SUPPLEMENTARY_EIDR = Value("Z")
  val STANDARD_FRONTIER = Value("A")
  val STANDARD_PRE_LODGED = Value("D")
  val SIMPLIFIED_FRONTIER = Value("C")
  val SIMPLIFIED_PRE_LODGED = Value("F")
  val OCCASIONAL_FRONTIER = Value("B")
  val OCCASIONAL_PRE_LODGED = Value("E")
  val CLEARANCE_FRONTIER = Value("J")
  val CLEARANCE_PRE_LODGED = Value("K")

  val allAdditionalDeclarationTypes = List(
    STANDARD_FRONTIER,
    STANDARD_PRE_LODGED,
    SUPPLEMENTARY_EIDR,
    SUPPLEMENTARY_SIMPLIFIED,
    SIMPLIFIED_FRONTIER,
    SIMPLIFIED_PRE_LODGED,
    OCCASIONAL_FRONTIER,
    OCCASIONAL_PRE_LODGED,
    CLEARANCE_FRONTIER,
    CLEARANCE_PRE_LODGED
  )

  def from(string: String): Option[AdditionalDeclarationType] = AdditionalDeclarationType.values.find(_.toString == string)

  def declarationType(additionalDeclarationType: AdditionalDeclarationType): DeclarationType =
    additionalDeclarationType match {
      case STANDARD_FRONTIER | STANDARD_PRE_LODGED       => STANDARD
      case SUPPLEMENTARY_EIDR | SUPPLEMENTARY_SIMPLIFIED => SUPPLEMENTARY
      case SIMPLIFIED_FRONTIER | SIMPLIFIED_PRE_LODGED   => SIMPLIFIED
      case OCCASIONAL_FRONTIER | OCCASIONAL_PRE_LODGED   => OCCASIONAL
      case CLEARANCE_FRONTIER | CLEARANCE_PRE_LODGED     => CLEARANCE
    }

  val arrivedTypes = List(STANDARD_FRONTIER, SIMPLIFIED_FRONTIER, OCCASIONAL_FRONTIER, CLEARANCE_FRONTIER)
  val preLodgedTypes = List(STANDARD_PRE_LODGED, SIMPLIFIED_PRE_LODGED, OCCASIONAL_PRE_LODGED, CLEARANCE_PRE_LODGED)

  def isArrived(additionalDeclarationType: Option[AdditionalDeclarationType]): Boolean =
    additionalDeclarationType.fold(false)(isArrived)

  def isArrived(additionalDeclarationType: AdditionalDeclarationType): Boolean =
    arrivedTypes.contains(additionalDeclarationType)

  def isPreLodged(additionalDeclarationType: Option[AdditionalDeclarationType]): Boolean =
    additionalDeclarationType.fold(false)(isPreLodged)

  def isPreLodged(additionalDeclarationType: AdditionalDeclarationType): Boolean =
    preLodgedTypes.contains(additionalDeclarationType)
}
