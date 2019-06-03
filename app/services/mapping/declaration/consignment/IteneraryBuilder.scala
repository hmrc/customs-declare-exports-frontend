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

package services.mapping.declaration.consignment
import forms.Choice
import forms.declaration.destinationCountries.{DestinationCountries, DestinationCountriesStandard}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.Consignment.Itinerary
import wco.datamodel.wco.declaration_ds.dms._2.ItineraryRoutingCountryCodeType

import scala.collection.JavaConverters._

object IteneraryBuilder {

  def build(implicit cacheMap: CacheMap, choice: Choice): java.util.List[Declaration.Consignment.Itinerary] =
    cacheMap
      .getEntry[DestinationCountriesStandard](DestinationCountries.formId)
      .map(
        data =>
          data.countriesOfRouting.zipWithIndex
            .map(data => createItenerary(data._2, data._1))
      )
      .getOrElse(Seq.empty)
      .toList
      .asJava

  def createItenerary(index: Integer, country: String): Itinerary = {
    val itenerary = new Declaration.Consignment.Itinerary()
    itenerary.setSequenceNumeric(new java.math.BigDecimal(index))

    val countryCodeType = new ItineraryRoutingCountryCodeType()
    countryCodeType.setValue(country)
    itenerary.setRoutingCountryCode(countryCodeType)
    itenerary
  }
}
