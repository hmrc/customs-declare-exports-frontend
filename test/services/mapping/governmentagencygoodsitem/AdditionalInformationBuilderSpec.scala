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

import forms.declaration.AdditionalInformation
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsItemBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem

class AdditionalInformationBuilderSpec extends WordSpec with Matchers with MockitoSugar with ExportsItemBuilder {

  val additionalInformation = AdditionalInformation("code", "description")

  "AdditionalInformationBuilder" should {

    "build then add" when {
      "no additional information" in {
        val exportItem = anItem(withoutAdditionalInformation())
        val governmentAgencyGoodsItem = new GovernmentAgencyGoodsItem()

        builder.buildThenAdd(exportItem, governmentAgencyGoodsItem)

        governmentAgencyGoodsItem.getAdditionalInformation shouldBe empty
      }

      "populated additional information" in {
        val exportItem = anItem(withAdditionalInformation(additionalInformation))
        val governmentAgencyGoodsItem = new GovernmentAgencyGoodsItem()

        builder.buildThenAdd(exportItem, governmentAgencyGoodsItem)

        governmentAgencyGoodsItem.getAdditionalInformation shouldNot be(empty)
        governmentAgencyGoodsItem.getAdditionalInformation
          .get(0)
          .getStatementCode
          .getValue shouldBe additionalInformation.code
        governmentAgencyGoodsItem.getAdditionalInformation
          .get(0)
          .getStatementDescription
          .getValue shouldBe additionalInformation.description
      }
    }
  }

  private def builder = new AdditionalInformationBuilder()

}
