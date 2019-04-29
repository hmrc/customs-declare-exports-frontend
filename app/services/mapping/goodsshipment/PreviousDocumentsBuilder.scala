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
import java.util

import forms.declaration.{Document, PreviousDocumentsData}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.declaration_ds.dms._2.{
  PreviousDocumentCategoryCodeType,
  PreviousDocumentIdentificationIDType,
  PreviousDocumentTypeCodeType
}

import scala.collection.JavaConverters._

object PreviousDocumentsBuilder {

  def build(implicit cacheMap: CacheMap): util.List[GoodsShipment.PreviousDocument] =
    cacheMap
      .getEntry[PreviousDocumentsData](Document.formId)
      .map(_.documents.map(createPreviousDocuments))
      .getOrElse(Seq.empty)
      .toList
      .asJava

  private def createPreviousDocuments(document: Document): GoodsShipment.PreviousDocument = {
    val categoryCode = new PreviousDocumentCategoryCodeType()
    categoryCode.setValue(document.documentCategory)

    val id = new PreviousDocumentIdentificationIDType()
    id.setValue(document.documentReference)

    val lineNumeric = new java.math.BigDecimal(document.goodsItemIdentifier.orNull)

    val typeCode = new PreviousDocumentTypeCodeType()
    typeCode.setValue(document.documentType)

    val previousDocument = new GoodsShipment.PreviousDocument()
    previousDocument.setCategoryCode(categoryCode)
    previousDocument.setID(id)
    previousDocument.setLineNumeric(lineNumeric)
    previousDocument.setTypeCode(typeCode)
    previousDocument
  }
}
