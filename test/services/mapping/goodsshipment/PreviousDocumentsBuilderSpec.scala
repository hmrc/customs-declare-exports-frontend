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

      "document data empty" in {
        val builder = new PreviousDocumentsBuilder
        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(PreviousDocumentsData(Seq.empty), goodsShipment)

        goodsShipment.getPreviousDocument.isEmpty should be (true)
      }

      "'document type' not supplied" in {
        val builder = new PreviousDocumentsBuilder
        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(PreviousDocumentsData(Seq(DocumentSpec.correctPreviousDocument.copy(documentType = ""))), goodsShipment)

        val previousDocs = goodsShipment.getPreviousDocument
        previousDocs.size should be(1)
        previousDocs.get(0).getID.getValue should be("DocumentReference")
        previousDocs.get(0).getCategoryCode.getValue should be("X")
        previousDocs.get(0).getTypeCode should be(null)
        previousDocs.get(0).getLineNumeric should be(BigDecimal(123).bigDecimal)
      }

      "'document reference' not supplied" in {
        val builder = new PreviousDocumentsBuilder
        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(PreviousDocumentsData(Seq(DocumentSpec.correctPreviousDocument.copy(documentReference = ""))), goodsShipment)

        val previousDocs = goodsShipment.getPreviousDocument
        previousDocs.size should be(1)
        previousDocs.get(0).getID should be(null)
        previousDocs.get(0).getCategoryCode.getValue should be("X")
        previousDocs.get(0).getTypeCode.getValue should be("ABC")
        previousDocs.get(0).getLineNumeric should be(BigDecimal(123).bigDecimal)
      }

      "'document catagory' not supplied" in {
        val builder = new PreviousDocumentsBuilder
        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(PreviousDocumentsData(Seq(DocumentSpec.correctPreviousDocument.copy(documentCategory = ""))), goodsShipment)

        val previousDocs = goodsShipment.getPreviousDocument
        previousDocs.size should be(1)

        previousDocs.get(0).getID.getValue should be("DocumentReference")
        previousDocs.get(0).getCategoryCode should be(null)
        previousDocs.get(0).getTypeCode.getValue should be("ABC")
        previousDocs.get(0).getLineNumeric should be(BigDecimal(123).bigDecimal)
      }

      "'line number' not supplied" in {
        val builder = new PreviousDocumentsBuilder
        val goodsShipment = new GoodsShipment
        builder.buildThenAdd(PreviousDocumentsData(Seq(DocumentSpec.correctPreviousDocument.copy(goodsItemIdentifier = None))), goodsShipment)

        val previousDocs = goodsShipment.getPreviousDocument
        previousDocs.size should be(1)
        previousDocs.get(0).getID.getValue should be("DocumentReference")
        previousDocs.get(0).getCategoryCode.getValue should be("X")
        previousDocs.get(0).getTypeCode.getValue should be("ABC")
        previousDocs.get(0).getLineNumeric should be(null)
      }

    }
  }

}
