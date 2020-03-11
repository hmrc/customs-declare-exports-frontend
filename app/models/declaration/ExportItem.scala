/*
 * Copyright 2020 HM Revenue & Customs
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

package models.declaration

import forms.declaration.FiscalInformation.AllowedFiscalInformationAnswers.yes
import forms.declaration._
import models.DeclarationType
import models.DeclarationType.DeclarationType
import play.api.libs.json.Json

case class ExportItem(
  id: String,
  sequenceId: Int = 0,
  procedureCodes: Option[ProcedureCodesData] = None,
  fiscalInformation: Option[FiscalInformation] = None,
  additionalFiscalReferencesData: Option[AdditionalFiscalReferencesData] = None,
  statisticalValue: Option[StatisticalValue] = None,
  commodityDetails: Option[CommodityDetails] = None,
  dangerousGoodsCode: Option[UNDangerousGoodsCode] = None,
  cusCode: Option[CusCode] = None,
  taricCodes: Option[List[TaricCode]] = None,
  nactCodes: Option[List[NactCode]] = None,
  packageInformation: Option[List[PackageInformation]] = None,
  commodityMeasure: Option[CommodityMeasure] = None,
  additionalInformation: Option[AdditionalInformationData] = None,
  documentsProducedData: Option[DocumentsProducedData] = None
) {
  def hasFiscalReferences: Boolean =
    fiscalInformation.exists(_.onwardSupplyRelief == FiscalInformation.AllowedFiscalInformationAnswers.yes)

  val isCompleted: PartialFunction[DeclarationType, Boolean] = {
    case DeclarationType.STANDARD | DeclarationType.SUPPLEMENTARY =>
      procedureCodes.isDefined && isFiscalInformationCompleted && statisticalValue.isDefined &&
        packageInformation.nonEmpty && commodityMeasure.isDefined
    case DeclarationType.SIMPLIFIED | DeclarationType.OCCASIONAL | DeclarationType.CLEARANCE =>
      procedureCodes.isDefined && isFiscalInformationCompleted && packageInformation.nonEmpty
  }

  private def isFiscalInformationCompleted: Boolean =
    if (fiscalInformation.exists(_.onwardSupplyRelief == yes)) additionalFiscalReferencesData.isDefined
    else fiscalInformation.isDefined
}

object ExportItem {

  implicit val format = Json.format[ExportItem]
}
