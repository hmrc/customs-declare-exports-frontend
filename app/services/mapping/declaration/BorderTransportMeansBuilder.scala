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

import forms.declaration.{BorderTransport, TransportDetails}
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

  def build(implicit cacheMap: CacheMap): Declaration.BorderTransportMeans = {

    val borderTransport = cacheMap
      .getEntry[BorderTransport](BorderTransport.formId)

    cacheMap
      .getEntry[TransportDetails](TransportDetails.formId)
      .filter(data => isDefined(data))
      .map(data => createBorderTransportMeans(data, borderTransport))
      .orNull
  }

  private def isDefined(transportDetails: TransportDetails): Boolean =
    transportDetails.meansOfTransportCrossingTheBorderIDNumber.isDefined ||
      transportDetails.meansOfTransportCrossingTheBorderType.nonEmpty ||
      transportDetails.meansOfTransportCrossingTheBorderNationality.nonEmpty

  private def createBorderTransportMeans(
    data: TransportDetails,
    borderTransport: Option[BorderTransport]
  ): Declaration.BorderTransportMeans = {
    val transportMeans = new Declaration.BorderTransportMeans()

    data.meansOfTransportCrossingTheBorderIDNumber.foreach { value =>
      val id = new BorderTransportMeansIdentificationIDType()
      id.setValue(value)
      transportMeans.setID(id)
    }

    borderTransport
      .filter(transport => transport.borderModeOfTransportCode.nonEmpty)
      .map(transport => {
        val modeCode = new BorderTransportMeansModeCodeType()
        modeCode.setValue(transport.borderModeOfTransportCode)
        transportMeans.setModeCode(modeCode)
      })

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
