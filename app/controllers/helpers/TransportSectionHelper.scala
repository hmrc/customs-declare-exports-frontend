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

package controllers.helpers

import forms.section6.InlandOrBorder.Border
import forms.section6.ModeOfTransportCode.{meaningfulModeOfTransportCodes, FixedTransportInstallations, PostalConsignment, Rail}
import forms.section1.AdditionalDeclarationType._
import forms.section6.{BorderTransport, DepartureTransport, ModeOfTransportCode, TransportCountry}
import models.DeclarationType._
import models.ExportsDeclaration

object TransportSectionHelper {

  val additionalDeclTypesAllowedOnInlandOrBorder = List(
    STANDARD_PRE_LODGED,
    STANDARD_FRONTIER,
    OCCASIONAL_PRE_LODGED,
    OCCASIONAL_FRONTIER,
    SIMPLIFIED_PRE_LODGED,
    SIMPLIFIED_FRONTIER,
    SUPPLEMENTARY_SIMPLIFIED
  )

  val postalOrFTIModeOfTransportCodes = List(Some(FixedTransportInstallations), Some(PostalConsignment))

  val nonPostalOrFTIModeOfTransportCodes =
    meaningfulModeOfTransportCodes.filterNot(code => postalOrFTIModeOfTransportCodes.contains(Some(code)))

  def isPostalOrFTIModeOfTransport(modeOfTransportCode: Option[ModeOfTransportCode]): Boolean =
    postalOrFTIModeOfTransportCodes.contains(modeOfTransportCode)

  def clearCacheOnSkippingTransportPages(declaration: ExportsDeclaration): ExportsDeclaration =
    if (skipTransportPages(declaration))
      declaration
        .updateDepartureTransport(DepartureTransport(None, None))
        .updateBorderTransport(BorderTransport("", ""))
        .updateTransportCountry(TransportCountry(None))
    else if (isRailModeOfTransport(declaration)) declaration.updateTransportCountry(TransportCountry(None))
    else declaration

  val Guernsey = "GG"
  val Jersey = "JE"

  def isGuernseyOrJerseyDestination(declaration: ExportsDeclaration): Boolean =
    declaration.locations.destinationCountry.flatMap(_.code) match {
      case Some(Guernsey) | Some(Jersey) => isStandardOrSupplementary(declaration)
      case _                             => false
    }

  def isRailModeOfTransport(declaration: ExportsDeclaration): Boolean =
    declaration.transportLeavingBorderCode == Some(Rail) && isStandardOrSupplementary(declaration)

  def skipBorderTransport(declaration: ExportsDeclaration): Boolean =
    declaration.isInlandOrBorder(Border) || skipTransportPages(declaration)

  def skipDepartureTransport(declaration: ExportsDeclaration): Boolean =
    isOccasionalOrSimplified(declaration) || skipTransportPages(declaration)

  def skipTransportPages(declaration: ExportsDeclaration): Boolean =
    isPostalOrFTIModeOfTransport(declaration.transportLeavingBorderCode) ||
      declaration.`type` != CLEARANCE && isPostalOrFTIModeOfTransport(declaration.inlandModeOfTransportCode) ||
      isGuernseyOrJerseyDestination(declaration)

  def skipTransportCountry(declaration: ExportsDeclaration): Boolean =
    isRailModeOfTransport(declaration) || skipTransportPages(declaration)
}
