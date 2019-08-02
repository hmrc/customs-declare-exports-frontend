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

import forms.common.AddressSpec
import forms.declaration.{ConsigneeDetails, EntityDetails}
import org.scalatest.{Matchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class ConsigneeBuilderSpec extends WordSpec with Matchers {


  "ConsigneeBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.Consignee instance" when {
      "only eori is supplied " in {
        val builder = new ConsigneeBuilder

        val goodsShipment = new GoodsShipment
        val details = ConsigneeDetails(EntityDetails(Some("9GB1234567ABCDEF"), None))

        builder.buildThenAdd(details, goodsShipment)

        goodsShipment.getConsignee.getID.getValue should be("9GB1234567ABCDEF")
        goodsShipment.getConsignee.getName should be(null)
        goodsShipment.getConsignee.getAddress should be(null)
      }

      "only address is supplied " in {
        val builder = new ConsigneeBuilder

        val goodsShipment = new GoodsShipment
        val details = ConsigneeDetails(EntityDetails(None, Some(AddressSpec.correctAddress)))

        builder.buildThenAdd(details, goodsShipment)

        goodsShipment.getConsignee.getID should be(null)
        goodsShipment.getConsignee.getName.getValue should be("Full Name")
        goodsShipment.getConsignee.getAddress.getLine.getValue should be("Address Line")
        goodsShipment.getConsignee.getAddress.getCityName.getValue should be("Town or City")
        goodsShipment.getConsignee.getAddress.getCountryCode.getValue should be("PL")
        goodsShipment.getConsignee.getAddress.getPostcodeID.getValue should be("AB12 34CD")
      }

      "empty data is supplied " in {
        val builder = new ConsigneeBuilder

        val goodsShipment = new GoodsShipment
        val details = ConsigneeDetails(EntityDetails(None, None))

        builder.buildThenAdd(details, goodsShipment)

        goodsShipment.getConsignee should be(null)
      }

      "'address.fullname' is not supplied" in {
        val builder = new ConsigneeBuilder

        val goodsShipment = new GoodsShipment
        val details = ConsigneeDetails(EntityDetails(None, Some(AddressSpec.addressWithEmptyFullname)))

        builder.buildThenAdd(details, goodsShipment)

        goodsShipment.getConsignee.getID should be(null)
        goodsShipment.getConsignee.getName should be(null)
        goodsShipment.getConsignee.getAddress.getLine.getValue should be("Address Line")
        goodsShipment.getConsignee.getAddress.getCityName.getValue should be("Town or City")
        goodsShipment.getConsignee.getAddress.getCountryCode.getValue should be("PL")
        goodsShipment.getConsignee.getAddress.getPostcodeID.getValue should be("AB12 34CD")
      }
    }
  }
}
