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

import forms.declaration.WarehouseIdentification
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class WarehouseBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "WarehouseBuilder" should {
    "correctly map to the WCO-DEC Warehouse instance" when {
      "identificationNumber is supplied" in {
        implicit val cacheMap =
          CacheMap(
            "CacheID",
            Map(
              WarehouseIdentification.formId -> Json
                .toJson(WarehouseIdentification(Some("GBWKG001"), Some("R"), Some("1234567GB"), Some("2")))
            )
          )
        val warehouse = WarehouseBuilder.build(cacheMap)
        warehouse.getID.getValue should be("1234567GB")
        warehouse.getTypeCode.getValue should be("R")
      }

      "identificationType is not supplied" in {
        implicit val cacheMap =
          CacheMap(
            "CacheID",
            Map(
              WarehouseIdentification.formId -> Json
                .toJson(WarehouseIdentification(Some("GBWKG001"), None, Some("1234567GB"), Some("2")))
            )
          )
        WarehouseBuilder.build(cacheMap) should be(null)
      }

      "identificationNumber is not supplied" in {
        implicit val cacheMap =
          CacheMap(
            "CacheID",
            Map(
              WarehouseIdentification.formId -> Json
                .toJson(WarehouseIdentification(Some("GBWKG001"), Some("R"), None, Some("2")))
            )
          )
        WarehouseBuilder.build(cacheMap) should be(null)
      }

      "Nothing is supplied" in {
        implicit val cacheMap: CacheMap = mock[CacheMap]
        when(cacheMap.getEntry[WarehouseIdentification](WarehouseIdentification.formId))
          .thenReturn(None)
        WarehouseBuilder.build(cacheMap) should be(null)
      }

    }
  }
}
