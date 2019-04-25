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
import models.declaration.AdditionalInformationData
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class AdditionalInformationBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "AdditionalInformationBuilder" should {
    "map correctly when values are present" in {

      val statementCode = "code"
      val descriptionValue = "description"
      val additionalInformation = AdditionalInformation(statementCode, descriptionValue)
      val additionalInformationData = AdditionalInformationData(Seq(additionalInformation))

      implicit val cacheMap: CacheMap = mock[CacheMap]
      when(
        cacheMap
          .getEntry[AdditionalInformationData](AdditionalInformationData.formId)
      ).thenReturn(Some(additionalInformationData))

      val mappedAdditionalInformation = AdditionalInformationBuilder.build().get
      mappedAdditionalInformation.isEmpty shouldBe false
      mappedAdditionalInformation.head.getStatementCode.getValue shouldBe statementCode
      mappedAdditionalInformation.head.getStatementDescription.getValue shouldBe descriptionValue
    }

    "map correctly when values are not Present" in {

      val additionalInformationData = AdditionalInformationData(Seq())

      implicit val cacheMap: CacheMap = mock[CacheMap]
      when(
        cacheMap
          .getEntry[AdditionalInformationData](AdditionalInformationData.formId)
      ).thenReturn(Some(additionalInformationData))

      val mappedAdditionalInformation = AdditionalInformationBuilder.build().get
      mappedAdditionalInformation.isEmpty shouldBe true

    }
  }

}
