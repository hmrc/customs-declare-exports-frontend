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
import forms.declaration.AdditionalInformation
import javax.inject.Inject
import services.cache.ExportItem
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.{
  AdditionalInformation => WCOAdditionalInformation
}
import wco.datamodel.wco.declaration_ds.dms._2.{
  AdditionalInformationStatementCodeType,
  AdditionalInformationStatementDescriptionTextType
}

class AdditionalInformationBuilder @Inject()()
    extends ModifyingBuilder[ExportItem, GoodsShipment.GovernmentAgencyGoodsItem] {

  def buildThenAdd(
    exportItem: ExportItem,
    wcoGovernmentAgencyGoodsItem: GoodsShipment.GovernmentAgencyGoodsItem
  ): Unit =
    exportItem.additionalInformation.foreach { additionalInformationData =>
      {
        additionalInformationData.items.foreach { additionalInformation =>
          wcoGovernmentAgencyGoodsItem.getAdditionalInformation.add(buildAdditionalInformation(additionalInformation))
        }
      }
    }

  private def buildAdditionalInformation(info: AdditionalInformation): WCOAdditionalInformation = {
    val wcoAdditionalInformation = new WCOAdditionalInformation

    val additionalInformationStatementCodeType = new AdditionalInformationStatementCodeType
    additionalInformationStatementCodeType.setValue(info.code)

    val additionalInformationStatementDescriptionTextType = new AdditionalInformationStatementDescriptionTextType
    additionalInformationStatementDescriptionTextType.setValue(info.description)

    wcoAdditionalInformation.setStatementCode(additionalInformationStatementCodeType)
    wcoAdditionalInformation.setStatementDescription(additionalInformationStatementDescriptionTextType)
    wcoAdditionalInformation
  }
}
