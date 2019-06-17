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
import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.declaration.Seal
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class TransportEquipmentBuilderSpec extends WordSpec with Matchers {

  "TransportEquipmentBuilder" should {
    "correctly map TransportEquipment instance for Standard journey " when {
      "all data is supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(Seal.formId -> Json.toJson(Seq(Seal("first"), Seal("second")))))

        val transportEquipments = TransportEquipmentBuilder.build()(cacheMap, Choice(AllowedChoiceValues.StandardDec))

        transportEquipments.size() should be(1)
        transportEquipments.get(0).getSeal.size() should be(2)
        transportEquipments.get(0).getSequenceNumeric.intValue() should be(2)

        transportEquipments.get(0).getSeal.get(0).getID.getValue should be("first")
        transportEquipments.get(0).getSeal.get(0).getSequenceNumeric.intValue() should be(1)
        transportEquipments.get(0).getSeal.get(1).getID.getValue should be("second")
        transportEquipments.get(0).getSeal.get(1).getSequenceNumeric.intValue() should be(2)
      }
    }
  }
}
