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

import forms.declaration.TransportInformation
import services.Countries.allCountries
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.declaration_ds.dms._2.{
  BorderTransportMeansIdentificationIDType,
  BorderTransportMeansIdentificationTypeCodeType,
  BorderTransportMeansModeCodeType,
  BorderTransportMeansRegistrationNationalityCodeType
}

object BorderTransportMeansBuilder {

  def build(implicit cacheMap: CacheMap): Declaration.BorderTransportMeans =
    cacheMap
      .getEntry[TransportInformation](TransportInformation.id)
      .filter(isDefined)
      .map(createBorderTransportMeans)
      .orNull

  private def isDefined(transportInformation: TransportInformation): Boolean =
    transportInformation.meansOfTransportCrossingTheBorderIDNumber.isDefined ||
      transportInformation.meansOfTransportCrossingTheBorderType.nonEmpty ||
      transportInformation.borderModeOfTransportCode.nonEmpty

  private def createBorderTransportMeans(data: TransportInformation): Declaration.BorderTransportMeans = {
    val transportMeans = new Declaration.BorderTransportMeans()

    if (data.meansOfTransportCrossingTheBorderIDNumber.isDefined) {
      val id = new BorderTransportMeansIdentificationIDType()
      id.setValue(data.meansOfTransportCrossingTheBorderIDNumber.getOrElse(""))
      transportMeans.setID(id)
    }

    if (data.borderModeOfTransportCode.nonEmpty) {
      val modeCode = new BorderTransportMeansModeCodeType()
      modeCode.setValue(data.borderModeOfTransportCode)
      transportMeans.setModeCode(modeCode)
    }

    if (data.meansOfTransportCrossingTheBorderType.nonEmpty) {
      val identificationTypeCode = new BorderTransportMeansIdentificationTypeCodeType()
      identificationTypeCode.setValue(data.meansOfTransportCrossingTheBorderType)
      transportMeans.setIdentificationTypeCode(identificationTypeCode)
    }

    if (data.meansOfTransportCrossingTheBorderNationality.isDefined) {
      val registrationNationalityCode = new BorderTransportMeansRegistrationNationalityCodeType()
      registrationNationalityCode.setValue(
        allCountries
          .find(country => data.meansOfTransportCrossingTheBorderNationality.contains(country.countryName))
          .map(_.countryCode)
          .getOrElse("")
      )
      transportMeans.setRegistrationNationalityCode(registrationNationalityCode)
    }

    transportMeans
  }
}
