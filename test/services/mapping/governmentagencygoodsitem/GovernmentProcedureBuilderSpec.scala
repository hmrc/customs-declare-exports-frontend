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

package services.mapping.governmentagencygoodsitem

import models.declaration.governmentagencygoodsitem.GovernmentProcedure
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsItemBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem

class GovernmentProcedureBuilderSpec
    extends WordSpec with Matchers with GovernmentAgencyGoodsItemData with ExportsItemBuilder {

  val firstProcedureCode = "CUPR"
  val additionalProcedureCode = "ABC"

  "ProcedureCodesBuilder" should {

    "build then add" when {
      "no procedure codes" in {
        val exportItem = aCachedItem(withoutProcedureCodes())
        val governmentAgencyGoodsItem = new GovernmentAgencyGoodsItem()

        builder.buildThenAdd(exportItem, governmentAgencyGoodsItem)

        governmentAgencyGoodsItem.getGovernmentProcedure shouldBe empty
      }

      "populated procedure codes" in {
        val exportItem = aCachedItem(withProcedureCodes(Some(firstProcedureCode), Seq(additionalProcedureCode)))
        val governmentAgencyGoodsItem = new GovernmentAgencyGoodsItem()

        builder.buildThenAdd(exportItem, governmentAgencyGoodsItem)

        val mappedProcedure1 = governmentAgencyGoodsItem.getGovernmentProcedure.get(0)
        mappedProcedure1.getCurrentCode.getValue shouldBe firstProcedureCode.substring(0, 2)
        mappedProcedure1.getPreviousCode.getValue shouldBe firstProcedureCode.substring(2, 4)

        val mappedProcedure2 = governmentAgencyGoodsItem.getGovernmentProcedure.get(1)
        mappedProcedure2.getCurrentCode.getValue shouldBe additionalProcedureCode
        mappedProcedure2.getPreviousCode shouldBe null
      }
    }
  }

  private def builder = new GovernmentProcedureBuilder()
}
