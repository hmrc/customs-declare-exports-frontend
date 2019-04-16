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

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem

class ProcedureCodesBuilderSpec extends WordSpec with Matchers with GovernmentAgencyGoodsItemMocks {

  "ProcedureCodesBuilder" should {
    "build governmentProcedure correctly" in {

      implicit val cacheMap = mock[CacheMap]
      setUpProcedureCodes()

      val results: Option[Seq[GovernmentAgencyGoodsItem.GovernmentProcedure]] = ProcedureCodesBuilder.build

      results.isDefined shouldBe true
      val mappedProcedures = results.get
      mappedProcedures.head.getCurrentCode.getValue shouldBe cachedCode.substring(0,2)
      mappedProcedures.head.getPreviousCode.getValue shouldBe cachedCode.substring(2,4)

      mappedProcedures.last.getCurrentCode.getValue shouldBe previousCode
      mappedProcedures.last.getPreviousCode.getValue shouldBe null
    }
  }

}
