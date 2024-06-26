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

package forms.declaration

import forms.common.Address
import forms.section2.ConsigneeDetails
import forms.section2.carrier.CarrierDetails
import forms.section2.consignor.ConsignorDetails
import forms.section2.exporter.ExporterDetails

abstract class Details {
  val details: EntityDetails

  def updateAddress(newAddress: Option[Address]): Details =
    this match {
      case exp: ExporterDetails   => exp.copy(details = exp.details.copy(address = newAddress))
      case car: CarrierDetails    => car.copy(details = car.details.copy(address = newAddress))
      case con: ConsigneeDetails  => con.copy(details = con.details.copy(address = newAddress))
      case cons: ConsignorDetails => cons.copy(details = cons.details.copy(address = newAddress))
    }
}
