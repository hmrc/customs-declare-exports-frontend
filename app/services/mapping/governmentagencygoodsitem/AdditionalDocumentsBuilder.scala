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

import forms.declaration.additionaldocuments.{DocumentWriteOff, DocumentsProduced}
import models.declaration.DocumentsProducedData
import services.ExportsItemsCacheIds.dateTimeCode
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.AdditionalDocument
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.AdditionalDocument.{Submitter, WriteOff}
import wco.datamodel.wco.declaration_ds.dms._2.AdditionalDocumentEffectiveDateTimeType.{DateTimeString => WCODateTimeString}
import wco.datamodel.wco.declaration_ds.dms._2.{AdditionalDocumentEffectiveDateTimeType, SubmitterNameTextType, WriteOffQuantityQuantityType, _}

object AdditionalDocumentsBuilder {

  def build()(implicit cachedMap: CacheMap): Option[Seq[AdditionalDocument]] =
    cachedMap
      .getEntry[DocumentsProducedData](DocumentsProducedData.formId)
      .map(_.documents.map(mapWCOAdditionalDocument(_)))

  private def mapWCOAdditionalDocument(doc: DocumentsProduced): AdditionalDocument = {
    val additionalDocument = new AdditionalDocument
    val additionalDocumentCategoryCodeType = new AdditionalDocumentCategoryCodeType
    additionalDocumentCategoryCodeType.setValue(doc.documentTypeCode.map(_.substring(0, 1)).orNull)

    val additionalDocumentTypeCodeType = new AdditionalDocumentTypeCodeType
    additionalDocumentTypeCodeType.setValue(doc.documentTypeCode.map(_.substring(1)).orNull)

    val additionalDocumentIdentificationIDType = new AdditionalDocumentIdentificationIDType
    additionalDocumentIdentificationIDType.setValue(mapDocumentIdentificationIDType(doc).orNull)

    val additionalDocumentLPCOExemptionCodeType = new AdditionalDocumentLPCOExemptionCodeType
    additionalDocumentLPCOExemptionCodeType.setValue(doc.documentStatus.orNull)

    val additionalDocumentNameTextType = new AdditionalDocumentNameTextType
    additionalDocumentNameTextType.setValue(doc.documentStatusReason.orNull)

    val dateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
    val additionalDocumentEffectiveDateTimeType = new AdditionalDocumentEffectiveDateTimeType
    val dateTimeString = new WCODateTimeString
    dateTimeString.setFormatCode(dateTimeCode)
    dateTimeString.setValue(doc.dateOfValidity.map(date => date.toString).orNull)

    additionalDocumentEffectiveDateTimeType.setDateTimeString(dateTimeString)

    additionalDocument.setCategoryCode(additionalDocumentCategoryCodeType)
    additionalDocument.setTypeCode(additionalDocumentTypeCodeType)
    additionalDocument.setID(additionalDocumentIdentificationIDType)
    additionalDocument.setLPCOExemptionCode(additionalDocumentLPCOExemptionCodeType)
    additionalDocument.setName(additionalDocumentNameTextType)
    additionalDocument.setSubmitter(doc.issuingAuthorityName.map(name => mapSubmitter(name = Some(name))).orNull)
    additionalDocument.setEffectiveDateTime(additionalDocumentEffectiveDateTimeType)

    additionalDocument.setWriteOff(mapWriteOff(doc.documentWriteOff))

    additionalDocument
  }

  private def mapDocumentIdentificationIDType(document: DocumentsProduced): Option[String] = for {
      identifierAndPart <- document.documentIdentifierAndPart
      documentIdentifier <- identifierAndPart.documentIdentifier
      documentPart <- identifierAndPart.documentPart
    } yield documentIdentifier + documentPart

  private def mapWriteOff(writeOff: Option[DocumentWriteOff]): WriteOff = {
    val writeoff = new WriteOff
    val quantityType = new WriteOffQuantityQuantityType
    quantityType.setValue(writeOff.flatMap(_.documentQuantity.map(quantity => quantity.bigDecimal)).orNull)
    quantityType.setUnitCode(writeOff.flatMap(_.measurementUnit).orNull)

    writeoff.setQuantityQuantity(quantityType)
    writeoff
  }

  private def mapSubmitter(name: Option[String], role: Option[String] = None): Submitter = {
    val submitter = new Submitter

    val submitterNameTextType = new SubmitterNameTextType
    submitterNameTextType.setValue(name.orNull)

    val submitterRoleCodeType = new SubmitterRoleCodeType
    submitterRoleCodeType.setValue(role.orNull)

    submitter.setName(submitterNameTextType)
    submitter.setRoleCode(submitterRoleCodeType)

    submitter
  }

}
