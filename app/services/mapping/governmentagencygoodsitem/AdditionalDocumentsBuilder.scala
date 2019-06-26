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

import models.declaration.governmentagencygoodsitem.{GovernmentAgencyGoodsItemAdditionalDocument, WriteOff}
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.AdditionalDocument
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.AdditionalDocument.{
  Submitter,
  WriteOff => WCOWriteOff
}
import wco.datamodel.wco.declaration_ds.dms._2.AdditionalDocumentEffectiveDateTimeType.{
  DateTimeString => WCODateTimeString
}
import wco.datamodel.wco.declaration_ds.dms._2.{
  AdditionalDocumentEffectiveDateTimeType,
  SubmitterNameTextType,
  WriteOffQuantityQuantityType,
  _
}

import scala.collection.JavaConverters._

object AdditionalDocumentsBuilder {

  def build(procedureCodes: Seq[GovernmentAgencyGoodsItemAdditionalDocument]): java.util.List[AdditionalDocument] =
    procedureCodes
      .map(document => createAdditionalDocument(document))
      .toList
      .asJava

  private def createAdditionalDocument(doc: GovernmentAgencyGoodsItemAdditionalDocument): AdditionalDocument = {
    val additionalDocument = new AdditionalDocument

    //TODO IT'S FIXED< CHECK TESTS AND ADD THIS CASE
    // CategoryCode and TypeCode - double check value inside, try with TypeCode C501
    doc.categoryCode.foreach { categoryCode =>
      val additionalDocumentCategoryCodeType = new AdditionalDocumentCategoryCodeType
      additionalDocumentCategoryCodeType.setValue(categoryCode)
      additionalDocument.setCategoryCode(additionalDocumentCategoryCodeType)
    }

    doc.typeCode.foreach { typeCode =>
      val additionalDocumentTypeCodeType = new AdditionalDocumentTypeCodeType
      additionalDocumentTypeCodeType.setValue(typeCode)
      additionalDocument.setTypeCode(additionalDocumentTypeCodeType)
    }

    doc.id.foreach { id =>
      val additionalDocumentIdentificationIDType = new AdditionalDocumentIdentificationIDType
      additionalDocumentIdentificationIDType.setValue(id)
      additionalDocument.setID(additionalDocumentIdentificationIDType)
    }

    doc.lpcoExemptionCode.foreach { exemptionCode =>
      val additionalDocumentLPCOExemptionCodeType = new AdditionalDocumentLPCOExemptionCodeType
      additionalDocumentLPCOExemptionCodeType.setValue(exemptionCode)
      additionalDocument.setLPCOExemptionCode(additionalDocumentLPCOExemptionCodeType)
    }

    doc.name.foreach { name =>
      val additionalDocumentNameTextType = new AdditionalDocumentNameTextType
      additionalDocumentNameTextType.setValue(name)
      additionalDocument.setName(additionalDocumentNameTextType)
    }

    doc.effectiveDateTime.foreach { validityDate =>
      val additionalDocumentEffectiveDateTimeType = new AdditionalDocumentEffectiveDateTimeType
      val dateTimeString = new WCODateTimeString
      dateTimeString.setFormatCode(validityDate.dateTimeString.formatCode)
      dateTimeString.setValue(validityDate.dateTimeString.value)

      additionalDocumentEffectiveDateTimeType.setDateTimeString(dateTimeString)
      additionalDocument.setEffectiveDateTime(additionalDocumentEffectiveDateTimeType)
    }

    doc.submitter.foreach { submitter =>
      additionalDocument.setSubmitter(mapSubmitter(name = submitter.name))
    }

    doc.writeOff.foreach(writeOff => additionalDocument.setWriteOff(mapWriteOff(writeOff)))

    additionalDocument
  }

  private def mapWriteOff(documentWriteOff: WriteOff): WCOWriteOff = {
    val writeOff = new WCOWriteOff
    val quantityType = new WriteOffQuantityQuantityType

    documentWriteOff.amount.foreach { quantity =>
      quantityType.setValue(quantity.value.get.bigDecimal)
    }
    documentWriteOff.quantity.foreach { measure =>
      quantityType.setUnitCode(measure.unitCode.get)
      quantityType.setValue(measure.value.get.bigDecimal)
    }

    writeOff.setQuantityQuantity(quantityType)
    writeOff
  }

  private def mapSubmitter(name: Option[String], role: Option[String] = None): Submitter = {
    val submitter = new Submitter

    name.foreach { nameValue =>
      val submitterNameTextType = new SubmitterNameTextType
      submitterNameTextType.setValue(nameValue)
      submitter.setName(submitterNameTextType)
    }

    role.foreach { roleValue =>
      val submitterRoleCodeType = new SubmitterRoleCodeType
      submitterRoleCodeType.setValue(roleValue)
      submitter.setRoleCode(submitterRoleCodeType)
    }

    submitter
  }
}
