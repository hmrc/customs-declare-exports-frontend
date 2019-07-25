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
import forms.common.Date
import forms.declaration._
import forms.declaration.additionaldocuments.{DocumentIdentifierAndPart, DocumentWriteOff, DocumentsProduced}
import models.declaration.governmentagencygoodsitem._
import models.declaration.{AdditionalInformationData, DocumentsProducedData, ProcedureCodesData}

import scala.math.BigDecimal

trait GovernmentAgencyGoodsItemData {

  //Document Produced Data
  val documentQuantity = BigDecimal(10)
  val dateOfValidity = Date(Some(25), Some(4), Some(2019))
  val documentAndAdditionalDocumentTypeCode = "C501"

  val documentIdentifier = "SYSUYSU"
  val documentPart = "12324554"
  val documentStatus = "PENDING"
  val documentStatusReason = "Reason"
  val issusingAuthorityName = "issuingAuthorityName"

  val measurementUnit = "KGM"

  val documentsProduced: Seq[DocumentsProduced] = Seq(
    DocumentsProduced(
      documentTypeCode = Some(documentAndAdditionalDocumentTypeCode),
      documentIdentifierAndPart = Some(
        DocumentIdentifierAndPart(documentIdentifier = Some(documentIdentifier), documentPart = Some(documentPart))
      ),
      documentStatus = Some(documentStatus),
      documentStatusReason = Some(documentStatus + documentStatusReason),
      issuingAuthorityName = Some(issusingAuthorityName),
      dateOfValidity = Some(dateOfValidity),
      documentWriteOff =
        Some(DocumentWriteOff(measurementUnit = Some(measurementUnit), documentQuantity = Some(documentQuantity)))
    )
  )

  val documentsProducedData = DocumentsProducedData(documentsProduced)

  //Package Information Data
  val shippingMarksValue = "shippingMarks"
  val packageTypeValue = "packageType"
  val packageQuantity = 12
  val numberOfPackages = Some(packageQuantity)
  val shippingMarksString = Some(shippingMarksValue)

  val packageInformation = new PackageInformation(Some(packageTypeValue), numberOfPackages, shippingMarksString)

  //Item Type Data
  val descriptionOfGoods = "descriptionOfGoods"
  val unDangerousGoodsCode = "unDangerousGoodsCode"
  val itemType = Some(
    ItemType(
      "combinedNomenclatureCode",
      Seq("taricAdditionalCodes"),
      Seq("nationalAdditionalCodes"),
      descriptionOfGoods,
      Some("cusCode"),
      Some(unDangerousGoodsCode),
      "10"
    )
  )

  //commodity measure data
  val netMassString = "15.00"
  val grossMassString = "25.00"
  val tariffQuantity = "31"
  val commodityMeasure = CommodityMeasure(Some(tariffQuantity), netMass = netMassString, grossMass = grossMassString)

  //procedureCodes Data

  val previousCode = "1stPrevcode"
  val previousCodes = Seq(previousCode)
  val cachedCode = "CUPR"
  val procedureCodesData = ProcedureCodesData(Some(cachedCode), previousCodes)

  //Additional Information data
  val statementCode = "code"
  val descriptionValue = "description"
  val additionalInformation = AdditionalInformation(statementCode, descriptionValue)
  val additionalInformationData = AdditionalInformationData(Seq(additionalInformation))

  val amount = Amount(Some("GBP"), Some(BigDecimal(123)))
  val goodsMeasure = GoodsMeasure(Some(Measure(Some("kg"), Some(BigDecimal(10)))))
  val commodity = Commodity(Some("someDescription"), Seq(), Seq(), Some(goodsMeasure))
}
