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

import forms.declaration.{ConsignmentReferences, ConsignmentReferencesSpec}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class UCRBuilderSpec extends WordSpec with Matchers {

  "UCRBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.UCR instance" when {
      "ducr supplied" in {

        implicit val cacheMap =
          CacheMap(
            "CacheID",
            Map(ConsignmentReferences.id -> ConsignmentReferencesSpec.correctConsignmentReferencesJSON)
          )
        val ucrObject = UCRBuilder.build(cacheMap)
        ucrObject.getID should be(null)
        ucrObject.getTraderAssignedReferenceID.getValue should be("8GB123456789012-1234567890QWERTYUIO")
      }

      "ducr not supplied" in {
        implicit val cacheMap =
          CacheMap(
            "CacheID",
            Map(ConsignmentReferences.id -> ConsignmentReferencesSpec.emptyConsignmentReferencesJSON)
          )
        val ucrObject = UCRBuilder.build(cacheMap)
        ucrObject should be(null)
      }
    }
  }
}
