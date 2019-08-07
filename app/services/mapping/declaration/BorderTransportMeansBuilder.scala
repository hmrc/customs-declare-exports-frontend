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
import javax.inject.Inject
import models.ExportsCacheModel
import services.Countries.allCountries
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.declaration_ds.dms._2.{BorderTransportMeansIdentificationIDType, BorderTransportMeansIdentificationTypeCodeType, BorderTransportMeansModeCodeType, BorderTransportMeansRegistrationNationalityCodeType}

class BorderTransportMeansBuilder @Inject()() extends ModifyingBuilder[ExportsCacheModel, Declaration] {
  override def buildThenAdd(model: ExportsCacheModel, t: Declaration): Unit = {
    val transportMeans = new Declaration.BorderTransportMeans()
    val maybeTransport = model.borderTransport.filter(isDefined)
    val maybeDetails = model.transportDetails.filter(isDefined)
    maybeTransport.foreach(appendBorderTransport(_, transportMeans))
    maybeDetails.foreach(appendTransportDetails(_, transportMeans))
    if (maybeDetails.isDefined || maybeTransport.isDefined) {
      t.setBorderTransportMeans(transportMeans)
    }
  }

  private def isDefined(transportDetails: TransportDetails): Boolean =
    transportDetails.meansOfTransportCrossingTheBorderIDNumber.isDefined ||
      transportDetails.meansOfTransportCrossingTheBorderType.nonEmpty ||
      transportDetails.meansOfTransportCrossingTheBorderNationality.nonEmpty

  private def isDefined(borderTransport: BorderTransport): Boolean = borderTransport.borderModeOfTransportCode.nonEmpty

  private def appendTransportDetails(data: TransportDetails, transportMeans: Declaration.BorderTransportMeans): Unit = {
    data.meansOfTransportCrossingTheBorderIDNumber.foreach { value =>
      val id = new BorderTransportMeansIdentificationIDType()
      id.setValue(value)
      transportMeans.setID(id)
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
  }

  private def appendBorderTransport(data: BorderTransport, transportMeans: Declaration.BorderTransportMeans): Unit = {
    val modeCode = new BorderTransportMeansModeCodeType()
    modeCode.setValue(data.borderModeOfTransportCode)
    transportMeans.setModeCode(modeCode)
  }
}
