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

import forms.declaration.PackageInformation
import forms.section6.Seal

trait ExplicitlySequencedObject[T] {
  val sequenceId: Int
  def updateSequenceId(sequenceId: Int): T
}

trait EsoKeyProvider[T] {
  val seqIdKey: String
}

object EsoKeyProvider {
  implicit val packageInformationKeyProvider: EsoKeyProvider[PackageInformation] = new EsoKeyProvider[PackageInformation] {
    val seqIdKey: String = "PackageInformation"
  }
  implicit val routingCountryKeyProvider: EsoKeyProvider[RoutingCountry] = new EsoKeyProvider[RoutingCountry] {
    val seqIdKey: String = "RoutingCountries"
  }
  implicit val sealKeyProvider: EsoKeyProvider[Seal] = new EsoKeyProvider[Seal] {
    val seqIdKey: String = "Seals"
  }
  implicit val containerKeyProvider: EsoKeyProvider[Container] = new EsoKeyProvider[Container] {
    val seqIdKey: String = "Containers"
  }
  implicit val exportItemKeyProvider: EsoKeyProvider[ExportItem] = new EsoKeyProvider[ExportItem] {
    val seqIdKey: String = "ExportItems"
  }
}
