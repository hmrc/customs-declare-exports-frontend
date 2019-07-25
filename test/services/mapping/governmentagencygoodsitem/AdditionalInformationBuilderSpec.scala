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

import java.util

import forms.declaration.AdditionalInformation
import models.declaration.AdditionalInformationData
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.CacheTestData
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem

class AdditionalInformationBuilderSpec extends WordSpec with Matchers with MockitoSugar with CacheTestData {

  val additionalInformation = AdditionalInformation("code", "description")

  "AdditionalInformationBuilder" should {
    "map correctly when values are present" in {
      val mappedAdditionalInformation = AdditionalInformationBuilder.build(Seq(additionalInformation))
      validateAdditionalInformations(mappedAdditionalInformation)

    }

    "map correctly when values are not Present" in {
      val mappedAdditionalInformation = AdditionalInformationBuilder.build(Seq())
      mappedAdditionalInformation.isEmpty shouldBe true
    }

    "map correctly when export item from cache is present" in {
      val exportItem =
        createExportItem().copy(additionalInformation = Some(AdditionalInformationData(Seq(additionalInformation))))
      val governmentAgencyGoodsItem = new GovernmentAgencyGoodsItem()
      AdditionalInformationBuilder.buildThenAdd(exportItem, governmentAgencyGoodsItem)
      validateAdditionalInformations(governmentAgencyGoodsItem.getAdditionalInformation)
    }

    "map correctly when export item from cache is not present" in {
      val exportItem =
        createExportItem().copy(additionalInformation = Some(AdditionalInformationData(Seq())))
      val governmentAgencyGoodsItem = new GovernmentAgencyGoodsItem()
      AdditionalInformationBuilder.buildThenAdd(exportItem, governmentAgencyGoodsItem)
      governmentAgencyGoodsItem.getAdditionalInformation.isEmpty shouldBe true
    }
  }

  private def validateAdditionalInformations(
    mappedAdditionalInformation: util.List[GovernmentAgencyGoodsItem.AdditionalInformation]
  ) = {
    mappedAdditionalInformation.isEmpty shouldBe false
    mappedAdditionalInformation.get(0).getStatementCode.getValue shouldBe additionalInformation.code
    mappedAdditionalInformation.get(0).getStatementDescription.getValue shouldBe additionalInformation.description
  }
}
