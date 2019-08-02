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

package services.mapping.goodsshipment.consignment
import forms.declaration.BorderTransport
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class DepartureTransportMeansBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {
  "DepartureTransportMeansBuilder" should {

    "correctly map DepartureTransportMeans instance using new model" in {
      val borderModeOfTransportCode = "BCode"
      val meansOfTransportOnDepartureType = "T"
      val meansOfTransportOnDepartureIDNumber = "12345"

      val builder = new DepartureTransportMeansBuilder

      val consignment = new GoodsShipment.Consignment
      builder.buildThenAdd(
        BorderTransport(
          borderModeOfTransportCode,
          meansOfTransportOnDepartureType,
          Some(meansOfTransportOnDepartureIDNumber)
        ),
        consignment
      )

      val departureTransportMeans = consignment.getDepartureTransportMeans
      departureTransportMeans.getID.getValue should be(meansOfTransportOnDepartureIDNumber)
      departureTransportMeans.getIdentificationTypeCode.getValue should be(meansOfTransportOnDepartureType)
      departureTransportMeans.getName should be(null)
      departureTransportMeans.getTypeCode should be(null)
      departureTransportMeans.getModeCode should be(null)
    }
  }
}
