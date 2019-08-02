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

import forms.declaration.ConsignmentReferencesSpec
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class UCRBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "UCRBuilder" should {

    "correctly map new model to the WCO-DEC GoodsShipment.UCR instance" when {
      "ducr supplied" in {

        val builder = new UCRBuilder

        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(ConsignmentReferencesSpec.correctConsignmentReferences, goodsShipment)

        val ucrObject = goodsShipment.getUCR
        ucrObject.getID should be(null)
        ucrObject.getTraderAssignedReferenceID.getValue should be(ConsignmentReferencesSpec.exemplaryDucr)
      }

      "ducr not supplied" in {

        val builder = new UCRBuilder

        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(ConsignmentReferencesSpec.correctConsignmentReferencesNoDucr, goodsShipment)

        goodsShipment.getUCR should be(null)
      }

    }
  }
}
