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

import forms.declaration.{AdditionalFiscalReference, AdditionalFiscalReferencesData}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.DomesticDutyTaxParty
import wco.datamodel.wco.declaration_ds.dms._2.{
  DomesticDutyTaxPartyIdentificationIDType,
  DomesticDutyTaxPartyRoleCodeType
}

import scala.collection.JavaConverters._

object DomesticDutyTaxPartyBuilder {

  def build(implicit cacheMap: CacheMap): java.util.List[DomesticDutyTaxParty] =
    cacheMap
      .getEntry[AdditionalFiscalReferencesData](AdditionalFiscalReferencesData.formId)
      .map(referencesData => referencesData.references.map(ref => createDomesticDutyTaxParty(ref)))
      .getOrElse(Seq.empty)
      .toList
      .asJava

  def createDomesticDutyTaxParty(
    additionalFiscalReference: AdditionalFiscalReference
  )(implicit cacheMap: CacheMap): DomesticDutyTaxParty = {

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
