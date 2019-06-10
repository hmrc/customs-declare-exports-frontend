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

import forms.declaration.FiscalInformation
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.DomesticDutyTaxParty
import wco.datamodel.wco.declaration_ds.dms._2.DomesticDutyTaxPartyRoleCodeType

import scala.collection.JavaConverters._

object DomesticDutyTaxPartyBuilder {

  def build(implicit cacheMap: CacheMap): java.util.List[DomesticDutyTaxParty] =
    cacheMap
      .getEntry[FiscalInformation](FiscalInformation.formId)
      .filter(_.onwardSupplyRelief == "Yes")
      .map(information => createDomesticDutyTaxParty(information))
      .toList
      .asJava

  def createDomesticDutyTaxParty(information: FiscalInformation)(implicit cacheMap: CacheMap): DomesticDutyTaxParty = {

    val domesticDutyTaxParty = new DomesticDutyTaxParty

    if (information.onwardSupplyRelief.nonEmpty) {
      val roleCodeType = new DomesticDutyTaxPartyRoleCodeType
      roleCodeType.setValue("FR1")
      domesticDutyTaxParty.setRoleCode(roleCodeType)
    }
    domesticDutyTaxParty
  }
}
