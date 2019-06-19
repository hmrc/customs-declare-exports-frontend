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
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class DomesticDutyTaxPartyBuilderSpec
    extends WordSpec with Matchers with GovernmentAgencyGoodsItemMocks with GovernmentAgencyGoodsItemData {

  "DomesticDutyTaxPartyBuilder" should {
    "map correctly if option is 'Yes'" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(FiscalInformation.formId -> Json.toJson(FiscalInformation("Yes"))))

      val domesticDutyTaxParties: java.util.List[GoodsShipment.DomesticDutyTaxParty] = DomesticDutyTaxPartyBuilder.build
      domesticDutyTaxParties.size() should be(1)
      domesticDutyTaxParties.get(0).getID should be(null)
      domesticDutyTaxParties.get(0).getRoleCode.getValue should be("FR1")

    }

    "should not create list of DomesticDutyTaxParty if option is 'No'" in {
      implicit val cacheMap: CacheMap =
        CacheMap("CacheID", Map(FiscalInformation.formId -> Json.toJson(FiscalInformation("No"))))

      val domesticDutyTaxParties: java.util.List[GoodsShipment.DomesticDutyTaxParty] = DomesticDutyTaxPartyBuilder.build
      domesticDutyTaxParties.isEmpty shouldBe true
    }
  }
}
