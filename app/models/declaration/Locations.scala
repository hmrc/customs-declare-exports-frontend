/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.declaration._
import forms.declaration.destinationCountries.{DestinationCountries, DestinationCountriesSupplementary}
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitSupplementary}
import uk.gov.hmrc.http.cache.client.CacheMap

case class Locations(
  destinationCountries: Option[DestinationCountriesSupplementary] = None,
  goodsLocation: Option[GoodsLocation] = None,
  warehouseIdentification: Option[WarehouseIdentification] = None,
  officeOfExit: Option[OfficeOfExitSupplementary] = None
) extends SummaryContainer {

  def isEmpty: Boolean =
    destinationCountries.isEmpty &&
      goodsLocation.isEmpty &&
      warehouseIdentification.isEmpty &&
      officeOfExit.isEmpty
}

object Locations {
  val id = "Locations"

  def apply(cacheMap: CacheMap): Locations = Locations(
    destinationCountries = cacheMap.getEntry[DestinationCountriesSupplementary](DestinationCountries.formId),
    goodsLocation = cacheMap.getEntry[GoodsLocation](GoodsLocation.formId),
    warehouseIdentification = cacheMap.getEntry[WarehouseIdentification](WarehouseIdentification.formId),
    officeOfExit = cacheMap.getEntry[OfficeOfExitSupplementary](OfficeOfExit.formId)
  )
}
