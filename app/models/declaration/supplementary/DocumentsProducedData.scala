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

package models.declaration.supplementary

import forms.MetadataPropertiesConvertable
import forms.supplementary.DocumentsProduced
import play.api.libs.json.Json

case class DocumentsProducedData(documents: Seq[DocumentsProduced]) extends MetadataPropertiesConvertable {
  override def toMetadataProperties(): Map[String, String] =
    documents.zipWithIndex.map { document =>
      Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[" + document._2 + "].categoryCode" ->
          document._1.documentTypeCode.flatMap(_.headOption).fold("")(_.toString),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[" + document._2 + "].typeCode" ->
          document._1.documentTypeCode.map(_.drop(1).toString).getOrElse(""),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[" + document._2 + "].id" ->
          (document._1.documentIdentifier.getOrElse("") + document._1.documentPart.getOrElse("")),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[" + document._2 + "].lpcoExemptionCode" ->
          document._1.documentStatus.getOrElse(""),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[" + document._2 + "].name" ->
          document._1.documentStatusReason.getOrElse(""),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].additionalDocuments[" + document._2 + "].writeOff.quantity" ->
          document._1.documentQuantity.getOrElse("")
      )
    }.fold(Map.empty)(_ ++ _)

  def containsItem(document: DocumentsProduced): Boolean = documents.contains(document)
}

object DocumentsProducedData {
  implicit val format = Json.format[DocumentsProducedData]

  val formId = "DocumentsProducedData"

  val maxNumberOfItems = 99
}
