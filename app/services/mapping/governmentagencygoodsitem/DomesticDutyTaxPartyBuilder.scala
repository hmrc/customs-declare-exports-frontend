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

import forms.declaration.AdditionalFiscalReference
import javax.inject.Inject
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.DomesticDutyTaxParty
import wco.datamodel.wco.declaration_ds.dms._2.{
  DomesticDutyTaxPartyIdentificationIDType,
  DomesticDutyTaxPartyRoleCodeType
}

import scala.collection.JavaConverters._

class DomesticDutyTaxPartyBuilder @Inject()()
    extends ModifyingBuilder[AdditionalFiscalReference, GovernmentAgencyGoodsItem] {
  override def buildThenAdd(model: AdditionalFiscalReference, item: GovernmentAgencyGoodsItem): Unit =
    item.getDomesticDutyTaxParty.add(DomesticDutyTaxPartyBuilder.createDomesticDutyTaxParty(model))
}

object DomesticDutyTaxPartyBuilder {

  def build(fiscalReferences: Seq[AdditionalFiscalReference]): java.util.List[DomesticDutyTaxParty] =
    fiscalReferences.map(ref => createDomesticDutyTaxParty(ref)).toList.asJava

  def createDomesticDutyTaxParty(additionalFiscalReference: AdditionalFiscalReference): DomesticDutyTaxParty = {

    val domesticDutyTaxParty = new DomesticDutyTaxParty

    val roleCodeType = new DomesticDutyTaxPartyRoleCodeType
    val referenceIdType = new DomesticDutyTaxPartyIdentificationIDType

    roleCodeType.setValue("FR1")
    referenceIdType.setValue(additionalFiscalReference.country + additionalFiscalReference.reference)
    domesticDutyTaxParty.setRoleCode(roleCodeType)
    domesticDutyTaxParty.setID(referenceIdType)

    domesticDutyTaxParty
  }
}
