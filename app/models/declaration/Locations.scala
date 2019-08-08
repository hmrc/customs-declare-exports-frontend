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
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.officeOfExit.OfficeOfExit
import models.ExportsDeclaration
import play.api.libs.json.Json

case class Locations(
  destinationCountries: Option[DestinationCountries] = None,
  goodsLocation: Option[GoodsLocation] = None,
  warehouseIdentification: Option[WarehouseIdentification] = None,
  officeOfExit: Option[OfficeOfExit] = None
) extends SummaryContainer {

  def isEmpty: Boolean =
    destinationCountries.isEmpty &&
      goodsLocation.isEmpty &&
      warehouseIdentification.isEmpty &&
      officeOfExit.isEmpty
}

object Locations {
  val id = "Locations"

  implicit val format = Json.format[Locations]

  def apply(cacheData: ExportsDeclaration): Locations = Locations(
    destinationCountries = cacheData.locations.destinationCountries,
    goodsLocation = cacheData.locations.goodsLocation,
    warehouseIdentification = cacheData.locations.warehouseIdentification,
    officeOfExit = cacheData.locations.officeOfExit
  )
}
