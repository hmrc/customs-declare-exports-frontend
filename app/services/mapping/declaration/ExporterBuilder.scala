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

package services.mapping.declaration
import forms.declaration.{EntityDetails, ExporterDetails}
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.Exporter
import wco.datamodel.wco.declaration_ds.dms._2._

object ExporterBuilder {

  def build(implicit cacheMap: CacheMap): Exporter =
    cacheMap
      .getEntry[ExporterDetails](ExporterDetails.id)
      .map(data => createExporter(data.details))
      .orNull

  private def createExporter(details: EntityDetails): Exporter = {
    val name = new ExporterNameTextType()

    val exporterId = new ExporterIdentificationIDType()
    val exporterName = new ExporterNameTextType()
    val exporterAddress = new Exporter.Address()

    exporterId.setValue(details.eori.orNull)

    details.address.map(address => {
      exporterName.setValue(address.fullName)

      val line = new AddressLineTextType()
      line.setValue(address.addressLine)

      val city = new AddressCityNameTextType
      city.setValue(address.townOrCity)

      val postcode = new AddressPostcodeIDType()
      postcode.setValue(address.postCode)

      val countryCode = new AddressCountryCodeType
      countryCode.setValue(
        allCountries.find(country => address.country.contains(country.countryName)).map(_.countryCode).getOrElse("")
      )

      exporterAddress.setLine(line)
      exporterAddress.setCityName(city)
      exporterAddress.setCountryCode(countryCode)
      exporterAddress.setPostcodeID(postcode)
    })

    val exporter = new Exporter()
    exporter.setID(exporterId)
    exporter.setName(exporterName)
    exporter.setAddress(exporterAddress)
    exporter
  }
}
