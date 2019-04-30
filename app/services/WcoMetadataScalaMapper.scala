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

package services

import models.declaration.governmentagencygoodsitem.{
  Classification,
  Commodity,
  DangerousGoods,
  Packaging,
  GovernmentAgencyGoodsItem => InternalAgencyGoodsItem,
  Amount => InternalAmount
}
import uk.gov.hmrc.wco.dec
import uk.gov.hmrc.wco.dec._

object WcoMetadataScalaMapper {
  def mapGoodsItem(item: InternalAgencyGoodsItem): GovernmentAgencyGoodsItem = {
    val mappedAmount = mapAmount(item.statisticalValueAmount)

    GovernmentAgencyGoodsItem(
      sequenceNumeric = item.sequenceNumeric,
      statisticalValueAmount = mappedAmount,
      additionalDocuments = item.additionalDocuments.map(doc => mapGovernmentAgencyGoodsItemDocument(doc)),
      additionalInformations = item.additionalInformations.map(info => mapGovernmentAgencyInformation(info)),
      commodity = item.commodity.map(commodity => mapCommodity(commodity)),
      governmentProcedures = item.governmentProcedures
        .map(procedure => dec.GovernmentProcedure(procedure.currentCode, procedure.previousCode)),
      packagings = item.packagings.map(packaging => mapPackaging(packaging))
    )
  }

  def mapPackaging(packaging: Packaging): dec.Packaging =
    dec.Packaging(
      sequenceNumeric = packaging.sequenceNumeric,
      marksNumbersId = packaging.marksNumbersId,
      quantity = packaging.quantity,
      typeCode = packaging.typeCode
    )

  def mapClassification(classification: Classification): dec.Classification =
    dec.Classification(
      id = classification.id,
      nameCode = classification.nameCode,
      identificationTypeCode = classification.identificationTypeCode,
      bindingTariffReferenceId = classification.bindingTariffReferenceId
    )

  def mapDangerousGoods(dangerousGoods: DangerousGoods): dec.DangerousGoods =
    dec.DangerousGoods(dangerousGoods.undgid)

  def mapCommodity(commodity: Commodity): dec.Commodity = {
    val classifications = commodity.classifications.map(classification => mapClassification(classification))
    val dangerousGoods = commodity.dangerousGoods.map(dangerousGoods => mapDangerousGoods(dangerousGoods))
    val goodsMeasure = commodity.goodsMeasure.map(
      measure =>
        dec.GoodsMeasure(
          mapMeasure(measure.grossMassMeasure),
          mapMeasure(measure.netWeightMeasure),
          mapMeasure(measure.tariffQuantity)
      )
    )
    dec.Commodity(
      description = commodity.description,
      classifications = classifications,
      dangerousGoods = dangerousGoods,
      goodsMeasure = goodsMeasure
    )
  }

  def mapMeasure(maybeMeasure: Option[models.declaration.governmentagencygoodsitem.Measure]): Option[dec.Measure] =
    maybeMeasure.map(measure => dec.Measure(measure.unitCode, measure.value))

  def mapAmount(amountToMap: Option[InternalAmount]): Option[Amount] =
    amountToMap.map(amt => Amount(amt.currencyId, amt.value))

  def mapGovernmentAgencyInformation(
    info: forms.declaration.AdditionalInformation
  ): uk.gov.hmrc.wco.dec.AdditionalInformation =
    uk.gov.hmrc.wco.dec.AdditionalInformation(Some(info.code), Some(info.description))

  private def mapGovernmentAgencyGoodsItemDocument(
    doc: models.declaration.governmentagencygoodsitem.GovernmentAgencyGoodsItemAdditionalDocument
  ): GovernmentAgencyGoodsItemAdditionalDocument = {
    val mappedDateTime: Option[DateTimeElement] = doc.effectiveDateTime.map(
      dte => DateTimeElement(DateTimeString(dte.dateTimeString.formatCode, dte.dateTimeString.value))
    )

    val documentSubmitter = doc.submitter.map(
      submitter =>
        GovernmentAgencyGoodsItemAdditionalDocumentSubmitter(name = submitter.name, roleCode = submitter.roleCode)
    )
    val writeOffAmount = doc.writeOff.flatMap(writeOff => mapAmount(writeOff.amount))
    val writeOffMeasure =
      doc.writeOff.flatMap(writeOff => writeOff.quantity.map(qty => Measure(qty.unitCode, qty.value)))

    val writeOff = doc.writeOff.map(_ => WriteOff(writeOffMeasure, writeOffAmount))

    GovernmentAgencyGoodsItemAdditionalDocument(
      categoryCode = doc.categoryCode,
      effectiveDateTime = mappedDateTime,
      id = doc.id,
      name = doc.name,
      typeCode = doc.typeCode,
      lpcoExemptionCode = doc.lpcoExemptionCode,
      submitter = documentSubmitter,
      writeOff = writeOff
    )
  }
}
