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

package services.mapping.goodsshipment
import forms.declaration.destinationCountries.DestinationCountries
import services.Countries.allCountries
import services.cache.ExportsCacheModel
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.ExportCountry
import wco.datamodel.wco.declaration_ds.dms._2.ExportCountryCountryCodeType

object ExportCountryBuilder {

  def build(implicit cacheMap: CacheMap): GoodsShipment.ExportCountry =
    cacheMap
      .getEntry[DestinationCountries](DestinationCountries.formId)
      .filter(isDefined)
      .map(createExportCountry)
      .orNull

  def buildThenAdd(exportsCacheModel: ExportsCacheModel, goodsShipment: GoodsShipment) {
    exportsCacheModel.locations.destinationCountries.foreach { destinationCountry =>
      goodsShipment.setExportCountry(createExportCountry(destinationCountry))
    }
  }

  private def isDefined(country: DestinationCountries): Boolean = country.countryOfDispatch.nonEmpty

  private def createExportCountry(data: DestinationCountries): GoodsShipment.ExportCountry = {

    val id = new ExportCountryCountryCodeType()
    id.setValue(
      allCountries
        .find(country => data.countryOfDispatch.contains(country.countryCode))
        .map(_.countryCode)
        .getOrElse("")
    )

    val exportCountry = new ExportCountry()
    exportCountry.setID(id)
    exportCountry
  }
}
