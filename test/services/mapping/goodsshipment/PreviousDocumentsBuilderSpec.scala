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
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class PreviousDocumentsBuilderSpec extends WordSpec with Matchers with MockitoSugar with ExportsCacheModelBuilder {

  "PreviousDocumentsBuilder " should {
    "correctly map to a WCO-DEC GoodsShipment.PreviousDocuments instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(Document.formId -> DocumentSpec.correctPreviousDocumentsJSONList))
      val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
      previousDoc.size should be(1)
      previousDoc.get(0).getID.getValue should be("DocumentReference")
      previousDoc.get(0).getCategoryCode.getValue should be("X")
      previousDoc.get(0).getLineNumeric.intValue() should be(123)
      previousDoc.get(0).getTypeCode.getValue should be("MCR")
    }

    "handle no documents when mapping to WCO-DEC GoodsShipment.PreviousDocuments" in {
      implicit val cacheMap: CacheMap = mock[CacheMap]
      when(cacheMap.getEntry[PreviousDocumentsData](Document.formId))
        .thenReturn(None)

      PreviousDocumentsBuilder.build(cacheMap).isEmpty shouldBe true
    }

    "handle empty documents when mapping to WCO-DEC GoodsShipment.PreviousDocuments" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(Document.formId -> DocumentSpec.emptyPreviousDocumentsJSONList))

      PreviousDocumentsBuilder.build(cacheMap).isEmpty shouldBe true
    }

    "handle documents mapping to WCO-DEC GoodsShipment.PreviousDocuments" when {
      "'lineNumeric' has not been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(Document.formId -> setupCacheData(goodsItemIdentifier = "")))

        val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
        previousDoc.size should be(1)
        previousDoc.get(0).getID.getValue should be("DocumentReference")
        previousDoc.get(0).getCategoryCode.getValue should be("X")
        previousDoc.get(0).getTypeCode.getValue should be("ABC")
        previousDoc.get(0).getLineNumeric should be(null)
      }
      "'documentType' has not been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(Document.formId -> setupCacheData(documentType = "")))

        val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
        previousDoc.size should be(1)
        previousDoc.get(0).getID.getValue should be("DocumentReference")
        previousDoc.get(0).getCategoryCode.getValue should be("X")
        previousDoc.get(0).getTypeCode should be(null)
        previousDoc.get(0).getLineNumeric.doubleValue() should be(10.0)
      }
      "'documentReference' has not been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(Document.formId -> setupCacheData(documentReference = "")))

        val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
        previousDoc.size should be(1)
        previousDoc.get(0).getID should be(null)
        previousDoc.get(0).getCategoryCode.getValue should be("X")
        previousDoc.get(0).getTypeCode.getValue should be("ABC")
        previousDoc.get(0).getLineNumeric.doubleValue() should be(10.0)
      }
      "'documentCategory' has not been supplied" in {
        implicit val cacheMap: CacheMap =
          CacheMap("CacheID", Map(Document.formId -> setupCacheData(documentCategory = "")))

        val previousDoc = PreviousDocumentsBuilder.build(cacheMap)
        previousDoc.size should be(1)
        previousDoc.get(0).getID.getValue should be("DocumentReference")
        previousDoc.get(0).getCategoryCode should be(null)
        previousDoc.get(0).getTypeCode.getValue should be("ABC")
        previousDoc.get(0).getLineNumeric.doubleValue() should be(10.0)
      }
    }

    "correctly map new model to a WCO-DEC GoodsShipment.PreviousDocuments instance" when {
      "when document data is present" in {

        val builder = new PreviousDocumentsBuilder
        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(PreviousDocumentsData(Seq(DocumentSpec.correctPreviousDocument)), goodsShipment)

        val previousDocs = goodsShipment.getPreviousDocument
        previousDocs.size should be(1)
        previousDocs.get(0).getID.getValue should be("DocumentReference")
        previousDocs.get(0).getCategoryCode.getValue should be("X")
        previousDocs.get(0).getTypeCode.getValue should be("ABC")
        previousDocs.get(0).getLineNumeric should be(BigDecimal(123).bigDecimal)

      }
    }
  }

  private def setupCacheData(
    documentCategory: String = "X",
    documentType: String = "ABC",
    documentReference: String = "DocumentReference",
    goodsItemIdentifier: String = "10.0"
  ) = {
    val previousDocumentsJSON: JsValue = JsObject(
      Map(
        "documentCategory" -> JsString(documentCategory),
        "documentType" -> JsString(documentType),
        "documentReference" -> JsString(documentReference),
        "goodsItemIdentifier" -> JsString(goodsItemIdentifier)
      )
    )

    val previousDocumentsJSONList = JsObject(Map("documents" -> JsArray(Seq(previousDocumentsJSON))))
    previousDocumentsJSONList
  }
}
