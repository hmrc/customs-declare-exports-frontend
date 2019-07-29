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

import models.declaration.governmentagencygoodsitem._
import org.scalatest.{Matchers, WordSpec}

class AdditionalDocumentsBuilderSpec extends WordSpec with Matchers with GovernmentAgencyGoodsItemData {
  "AdditionalDocumentsBuilder" should {
    "map correctly when values are present" in {

      val amount = Amount(Some("GBP"), Some(BigDecimal(100)))
      val measure = Measure(Some("KGM"), Some(BigDecimal(10)))
      val writeOff = WriteOff(Some(measure), Some(amount))
      val submitter = GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(Some("issuingAuthorityName"), Some("role"))
      val dateTimeString = DateTimeString("102", "20170304")
      val dateTimeElement = DateTimeElement(dateTimeString)
      val additionalDocument = GovernmentAgencyGoodsItemAdditionalDocument(
        categoryCode = Some("C"),
        effectiveDateTime = Some(dateTimeElement),
        id = Some("123"),
        name = Some("Reason"),
        typeCode = Some("501"),
        lpcoExemptionCode = Some("PENDING"),
        submitter = Some(submitter),
        writeOff = Some(writeOff)
      )

      val mappedDocuments = AdditionalDocumentsBuilder.build(Seq(additionalDocument))
      mappedDocuments.get(0).getCategoryCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(0, 1)
      mappedDocuments.get(0).getTypeCode.getValue shouldBe documentAndAdditionalDocumentTypeCode.substring(1)
      mappedDocuments.get(0).getID.getValue should be("123")
      mappedDocuments.get(0).getLPCOExemptionCode.getValue shouldBe documentStatus
      mappedDocuments.get(0).getName.getValue shouldBe documentStatusReason
      mappedDocuments.get(0).getSubmitter.getName.getValue shouldBe issusingAuthorityName

      val writeoff = mappedDocuments.get(0).getWriteOff
      writeoff.getAmountAmount shouldBe null
      val writeOffQuantity = writeoff.getQuantityQuantity
      writeOffQuantity.getUnitCode shouldBe measurementUnit
      writeOffQuantity.getValue shouldBe documentQuantity.bigDecimal
    }
  }
}
