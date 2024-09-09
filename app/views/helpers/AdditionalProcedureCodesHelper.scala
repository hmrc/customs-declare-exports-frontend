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

package views.helpers

import controllers.helpers.TransportSectionHelper.{Guernsey, Jersey}
import models.DeclarationType.CLEARANCE
import models.requests.JourneyRequest

import javax.inject.Singleton

@Singleton
class AdditionalProcedureCodesHelper {

  def hintText()(implicit request: JourneyRequest[_]): Option[String] =
    request.cacheModel.locations.destinationCountry.map(_.code.getOrElse("")) match {
      case Some(Jersey) | Some(Guernsey) if request.declarationType.equals(CLEARANCE) && request.cacheModel.isNotEntryIntoDeclarantsRecords =>
        Some("declaration.additionalProcedureCodes.jersey.clearanceNonEidr.hint")
      case Some(Jersey) | Some(Guernsey) => Some("declaration.additionalProcedureCodes.jersey.hint")
      case _                             => None
    }
}
