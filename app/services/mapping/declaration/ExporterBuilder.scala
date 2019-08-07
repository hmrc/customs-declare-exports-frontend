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
import forms.common.Address
import forms.declaration.{EntityDetails, ExporterDetails}
import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.Exporter
import wco.datamodel.wco.declaration_ds.dms._2._

class ExporterBuilder @Inject()() extends ModifyingBuilder[ExportsCacheModel, Declaration] {
  override def buildThenAdd(model: ExportsCacheModel, declaration: Declaration): Unit =
    model.parties.exporterDetails
      .filter(isDefined)
      .map(_.details)
      .map(createExporter)
      .foreach(declaration.setExporter)

  private def isDefined(exporterDetails: ExporterDetails): Boolean =
    exporterDetails.details.eori.isDefined || exporterDetails.details.address.isDefined

  private def createExporter(details: EntityDetails): Exporter = {
    val exporter = new Exporter()

    details.eori.foreach(eori => {
      val exporterIdentificationIDType = new ExporterIdentificationIDType
      exporterIdentificationIDType.setValue(eori)
      exporter.setID(exporterIdentificationIDType)
    })

    details.address.foreach(address => {
      val exporterNameTextType = new ExporterNameTextType
      exporterNameTextType.setValue(address.fullName)
      exporter.setName(exporterNameTextType)
      exporter.setAddress(mapAddress(address))
    })

    exporter
  }

  private def mapAddress(address: Address): Exporter.Address = {
    val declarantAddress = new Exporter.Address

    val addressLineTextType = new AddressLineTextType
    addressLineTextType.setValue(address.addressLine)

    val addressCityNameTextType = new AddressCityNameTextType
    addressCityNameTextType.setValue(address.townOrCity)

    val addressPostcodeIDType = new AddressPostcodeIDType
    addressPostcodeIDType.setValue(address.postCode)

    val addressCountryCodeType = new AddressCountryCodeType
    addressCountryCodeType.setValue(
      services.Countries.allCountries
        .find(country => address.country.contains(country.countryName))
        .map(_.countryCode)
        .getOrElse("")
    )

    declarantAddress.setLine(addressLineTextType)
    declarantAddress.setCityName(addressCityNameTextType)
    declarantAddress.setPostcodeID(addressPostcodeIDType)
    declarantAddress.setCountryCode(addressCountryCodeType)

    declarantAddress
  }

}
