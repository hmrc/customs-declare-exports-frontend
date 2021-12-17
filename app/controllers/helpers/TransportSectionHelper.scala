/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.declaration.ModeOfTransportCode
import forms.declaration.ModeOfTransportCode.{FixedTransportInstallations, PostalConsignment}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._

object TransportSectionHelper {

  val altAdditionalTypesOnTransportSection = List(STANDARD_PRE_LODGED, STANDARD_FRONTIER, SUPPLEMENTARY_SIMPLIFIED)

  val postalOrFTIModeOfTransportCodes = List(Some(FixedTransportInstallations), Some(PostalConsignment))

  def isPostalOrFTIModeOfTransport(modeOfTransportCode: Option[ModeOfTransportCode]): Boolean =
    postalOrFTIModeOfTransportCodes.contains(modeOfTransportCode)
}
