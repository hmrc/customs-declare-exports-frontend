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
import forms.declaration.{TransportCodes, WarehouseIdentification, WarehouseIdentificationSpec}
import org.scalatest.{Matchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class ArrivalTransportMeansBuilderSpec extends WordSpec with Matchers {

  "ArrivalTransportMeansBuilder" should {
    "correctly map ArrivalTransportMeans instance" in {
      val builder = new ArrivalTransportMeansBuilder

      var model: WarehouseIdentification = WarehouseIdentificationSpec.correctWarehouseIdentification
      var consignment: GoodsShipment.Consignment = new GoodsShipment.Consignment
      builder.buildThenAdd(model, consignment)

      consignment.getArrivalTransportMeans.getID should be(null)
      consignment.getArrivalTransportMeans.getIdentificationTypeCode should be(null)
      consignment.getArrivalTransportMeans.getName should be(null)
      consignment.getArrivalTransportMeans.getTypeCode should be(null)
      consignment.getArrivalTransportMeans.getModeCode.getValue should be(TransportCodes.Maritime)
    }
  }
}
