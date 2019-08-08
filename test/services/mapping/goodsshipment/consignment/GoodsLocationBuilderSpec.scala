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

import forms.declaration.GoodsLocationTestData._
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class GoodsLocationBuilderSpec extends WordSpec with Matchers with ExportsDeclarationBuilder {

  "GoodsLocationBuilder" should {

    "correctly map GoodsLocation instance for supplementary journey" when {

      "all data is supplied from form model" in {
        val builder = new GoodsLocationBuilder
        val consignment = new GoodsShipment.Consignment

        builder.buildThenAdd(correctGoodsLocation, consignment)

        validateGoodsLocation(consignment.getGoodsLocation)
      }
    }
  }

  private def validateGoodsLocation(goodsLocation: GoodsShipment.Consignment.GoodsLocation) = {
    goodsLocation.getID.getValue should be(identificationOfLocation)
    goodsLocation.getAddress.getLine.getValue should be(addressLine)
    goodsLocation.getAddress.getCityName.getValue should be(city)
    goodsLocation.getAddress.getPostcodeID.getValue should be(postcode)
    goodsLocation.getAddress.getCountryCode.getValue should be(countryCode)
    goodsLocation.getName.getValue should be(additionalQualifier)
    goodsLocation.getTypeCode.getValue should be(typeOfLocation)
    goodsLocation.getAddress.getTypeCode.getValue should be(qualifierOfIdentification)
  }

}
