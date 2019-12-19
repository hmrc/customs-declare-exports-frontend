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
import forms.declaration.officeOfExit.OfficeOfExit
import models.ExportsDeclaration
import play.api.libs.json.Json

case class Locations(
  originationCountry: Option[String] = None,
  destinationCountry: Option[String] = None,
  hasRoutingCountries: Option[Boolean] = None,
  routingCountries: Seq[String] = Seq.empty,
  goodsLocation: Option[GoodsLocation] = None,
  officeOfExit: Option[OfficeOfExit] = None,
  supervisingCustomsOffice: Option[SupervisingCustomsOffice] = None,
  warehouseIdentification: Option[WarehouseIdentification] = None,
  inlandModeOfTransportCode: Option[InlandModeOfTransportCode] = None
)

object Locations {
  val id = "Locations"

  implicit val format = Json.format[Locations]

  def apply(cacheData: ExportsDeclaration): Locations = Locations(
    originationCountry = cacheData.locations.originationCountry,
    destinationCountry = cacheData.locations.destinationCountry,
    hasRoutingCountries = cacheData.locations.hasRoutingCountries,
    routingCountries = cacheData.locations.routingCountries,
    goodsLocation = cacheData.locations.goodsLocation,
    officeOfExit = cacheData.locations.officeOfExit,
    supervisingCustomsOffice = cacheData.locations.supervisingCustomsOffice,
    warehouseIdentification = cacheData.locations.warehouseIdentification,
    inlandModeOfTransportCode = cacheData.locations.inlandModeOfTransportCode
  )
}
