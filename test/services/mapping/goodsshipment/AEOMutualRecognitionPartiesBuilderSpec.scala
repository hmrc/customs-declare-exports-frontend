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
import forms.declaration.DeclarationAdditionalActors
import models.declaration.{DeclarationAdditionalActorsData, DeclarationAdditionalActorsDataSpec}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import uk.gov.hmrc.http.cache.client.CacheMap

class AEOMutualRecognitionPartiesBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "AEOMutualRecognitionPartiesBuilder " should {
    "correctly map to a WCO-DEC GoodsShipment.AEOMutualRecognitionParties instance" when {
      "all data has been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(
              DeclarationAdditionalActors.formId -> DeclarationAdditionalActorsDataSpec.correctAdditionalActorsDataJSON
            )
          )
        val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
        actors.size should be(1)
        actors.get(0).getID.getValue should be("eori1")
        actors.get(0).getRoleCode.getValue should be("CS")
      }

      "'eori' has not been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(DeclarationAdditionalActors.formId -> setupCacheData(eori = "")))
        val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
        actors.size should be(1)
        actors.get(0).getID should be(null)
        actors.get(0).getRoleCode.getValue should be("CS")
      }

      "'partyType' has not been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(DeclarationAdditionalActors.formId -> setupCacheData(partyType = "")))
        val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
        actors.size should be(1)
        actors.get(0).getID.getValue should be("eori1")
        actors.get(0).getRoleCode should be(null)
      }

      "handle no documents when mapping to WCO-DEC GoodsShipment.AEOMutualRecognitionParties" in {
        implicit val cacheMap: CacheMap = mock[CacheMap]
        when(cacheMap.getEntry[DeclarationAdditionalActorsData](DeclarationAdditionalActors.formId))
          .thenReturn(None)

        val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
        actors.isEmpty shouldBe true
      }

      "handle empty documents when mapping to WCO-DEC GoodsShipment.AEOMutualRecognitionParties" in {
        implicit val cacheMap: CacheMap =
          CacheMap(
            "CacheID",
            Map(DeclarationAdditionalActors.formId -> DeclarationAdditionalActorsDataSpec.emptyAdditionalActorsDataJSON)
          )
        val actors = AEOMutualRecognitionPartiesBuilder.build(cacheMap)
        actors.isEmpty shouldBe true
      }
    }
  }

  private def setupCacheData(eori: String = "eori1", partyType: String = "CS") = {
    val objectJson: JsValue = JsObject(Map("eori" -> JsString(eori), "partyType" -> JsString(partyType)))

    val objectsJSONList = JsObject(Map("actors" -> JsArray(Seq(objectJson))))
    objectsJSONList
  }

}
