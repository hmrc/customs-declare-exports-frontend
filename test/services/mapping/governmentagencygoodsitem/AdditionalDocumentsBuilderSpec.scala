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

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class AdditionalDocumentsBuilderSpec extends WordSpec with Matchers with GovernmentAgencyGoodsItemMocks {

  "AdditionalDocumentsBuilder" should {
    "map correctly when values are present" in {
        implicit val cacheMap: CacheMap = mock[CacheMap]
        setUpAdditionalDocuments()

        val mappedDocuments = AdditionalDocumentsBuilder.build().get
        mappedDocuments.isEmpty shouldBe false
      val firstMappedDocument = mappedDocuments.head

        firstMappedDocument.getCategoryCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(0,1)
        firstMappedDocument.getTypeCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(1)
        firstMappedDocument.getID.getValue shouldBe documentIdentifier + documentPart
        firstMappedDocument.getLPCOExemptionCode.getValue shouldBe documentStatus
        firstMappedDocument.getName.getValue shouldBe documentStatus + documentStatusReason
        firstMappedDocument.getSubmitter.getName.getValue shouldBe issusingAuthorityName

        val writeoff = firstMappedDocument.getWriteOff
        writeoff.getAmountAmount shouldBe null
        val writeOffQuantity = writeoff.getQuantityQuantity
        writeOffQuantity.getUnitCode shouldBe measurementUnit
        writeOffQuantity.getValue shouldBe documentQuantity.bigDecimal
    }
  }
}
