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

package forms.declaration

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsObject, JsString, JsValue}

class WarehouseIdentificationSpec extends WordSpec with MustMatchers {
  import WarehouseIdentificationSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val warehouseIdentification = correctWarehouseIdentification
      val expectedMetadataProperties: Map[String, String] = Map(
        "declaration.goodsShipment.warehouse.id" -> warehouseId,
        "declaration.goodsShipment.warehouse.typeCode" -> warehouseTypeCode
      )

      warehouseIdentification.toMetadataProperties() must equal(expectedMetadataProperties)
    }
  }

}

object WarehouseIdentificationSpec {
  private val warehouseTypeCode = "R"
  private val warehouseId = "1234567GB"

  val correctWarehouseIdentification = WarehouseIdentification(id = Some(warehouseTypeCode + warehouseId))
  val emptyWarehouseIdentification = WarehouseIdentification(id = None)

  val correctWarehouseIdentificationJSON: JsValue =
    JsObject(Map("id" -> JsString(warehouseTypeCode + warehouseId)))
  val emptyWarehouseIdentificationJSON: JsValue =
    JsObject(Map("identificationNumber" -> JsString("")))
}
