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

package services.mapping.declaration
import forms.declaration.{WarehouseIdentification, WarehouseIdentificationSpec}
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class SupervisingOfficeBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "SupervisingOfficeBuilder" should {
    "correctly map to the WCO-DEC SupervisingOffice instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(WarehouseIdentification.formId -> WarehouseIdentificationSpec.correctWarehouseIdentificationJSON)
        )
      val supervisingOffice = SupervisingOfficeBuilder.build(cacheMap)
      supervisingOffice.getID.getValue should be("12345678")
    }

    "build then add" when {
      "no warehouse identification" in {
        val model = aCacheModel(withoutWarehouseIdentification())
        val declaration = new Declaration()

        new SupervisingOfficeBuilder().buildThenAdd(model, declaration)

        declaration.getSupervisingOffice should be(null)
      }

      "missing supervising customs office" in {
        val model = aCacheModel(withWarehouseIdentification(supervisingCustomsOffice = None))
        val declaration = new Declaration()

        new SupervisingOfficeBuilder().buildThenAdd(model, declaration)

        declaration.getSupervisingOffice should be(null)
      }

      "populated" in {
        val model = aCacheModel(withWarehouseIdentification(supervisingCustomsOffice = Some("value")))
        val declaration = new Declaration()

        new SupervisingOfficeBuilder().buildThenAdd(model, declaration)

        declaration.getSupervisingOffice.getID.getValue should be("value")
      }
    }
  }
}
