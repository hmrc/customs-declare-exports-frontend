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
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class TotalPackageQuantityBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "GoodsItemQuantityBuilder" should {

    "build then add" when {
      "no Total number of Items" in {
        val model = aCacheModel(withoutTotalNumberOfItems())
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getTotalPackageQuantity should be(null)
      }

      "populated" in {
        val model = aCacheModel(withTotalNumberOfItems(totalPackage = "123"))
        val declaration = new Declaration()

        builder.buildThenAdd(model, declaration)

        declaration.getTotalPackageQuantity.getValue.intValue should be(123)
      }
    }
  }

  private def builder = new TotalPackageQuantityBuilder()
}
