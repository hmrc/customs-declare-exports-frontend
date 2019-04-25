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

import forms.declaration.{Document, DocumentSpec, PreviousDocumentsData}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap

class PreviousDocumentsBuilderSpec extends WordSpec with Matchers with MockitoSugar {

  "PreviousDocumentsBuilder " should {
    "correctly map to a WCO-DEC GoodsShipment.PreviousDocuments instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(Document.formId -> DocumentSpec.correctPreviousDocumentsJSONList))
      val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
      previousDoc.size should be(1)
      previousDoc.get(0).getID.getValue should be("DocumentReference")
      previousDoc.get(0).getCategoryCode.getValue should be("X")
      previousDoc.get(0).getLineNumeric.intValue() should be(123)
      previousDoc.get(0).getTypeCode.getValue should be("ABC")
    }

    "handle empty documents when mapping to WCO-DEC GoodsShipment.PreviousDocuments" in {
      implicit val cacheMap: CacheMap = mock[CacheMap]
      when(cacheMap.getEntry[PreviousDocumentsData](Document.formId))
        .thenReturn(None)

      val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
      previousDoc.isEmpty shouldBe true
    }
  }
}
