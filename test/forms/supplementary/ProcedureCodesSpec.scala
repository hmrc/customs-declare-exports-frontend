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

package forms.supplementary
import org.scalatest.{MustMatchers, WordSpec}

class ProcedureCodesSpec extends WordSpec with MustMatchers {

  private val procedureCode_1 = "12"
  private val procedureCode_2 = "34"
  private val additionalProcedureCodes = Seq("111", "222", "333")

  private val procedureCodes = ProcedureCodes(
    procedureCode = procedureCode_1 + procedureCode_2,
    additionalProcedureCodes = additionalProcedureCodes
  )

  private val expectedProcedureCodesProperties: Map[String, String] = Map(
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].currentCode" -> procedureCode_1,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].previousCode" -> procedureCode_2,
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[1].currentCode" -> additionalProcedureCodes(
      0
    ),
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[2].currentCode" -> additionalProcedureCodes(
      1
    ),
    "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[3].currentCode" -> additionalProcedureCodes(
      2
    )
  )

  "ProcedureCodes" should {
    "convert itself into procedure codes properties" in {
      procedureCodes.toMetadataProperties() must equal(expectedProcedureCodesProperties)
    }
  }

}
