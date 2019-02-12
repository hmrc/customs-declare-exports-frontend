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
import models.declaration.supplementary.ProcedureCodesData
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class ProcedureCodesDataSpec extends WordSpec with MustMatchers {
  import ProcedureCodesDataSpec._

  "Method toMetadataProperties" should {
    "return proper Metadata Properties" in {
      val procedureCodes = correctProcedureCodes
      val expectedProcedureCodesProperties: Map[String, String] = Map(
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].currentCode" -> procedureCode_1,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[0].previousCode" -> procedureCode_2,
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[1].currentCode" -> procedureCodes
          .additionalProcedureCodes(0),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[2].currentCode" -> procedureCodes
          .additionalProcedureCodes(1),
        "declaration.goodsShipment.governmentAgencyGoodsItems[0].governmentProcedures[3].currentCode" -> procedureCodes
          .additionalProcedureCodes(2)
      )

      procedureCodes.toMetadataProperties() must equal(expectedProcedureCodesProperties)
    }
  }

  "Procedure code limit" should {
    "contains correct value" in {
      ProcedureCodesData.limitOfCodes must be(99)
    }
  }

}

object ProcedureCodesDataSpec {
  private val procedureCode_1 = "12"
  private val procedureCode_2 = "34"

  val correctProcedureCodes = ProcedureCodesData(
    procedureCode = Some(procedureCode_1 + procedureCode_2),
    additionalProcedureCodes = Seq("111", "222", "333")
  )
  val emptyProcedureCodes = ProcedureCodesData(procedureCode = None, additionalProcedureCodes = Seq.empty)

  val correctProcedureCodesJSON: JsValue = JsObject(
    Map(
      "procedureCode" -> JsString(procedureCode_1 + procedureCode_2),
      "additionalProcedureCodes" -> JsArray(Seq(JsString("111"), JsString("222"), JsString("333")))
    )
  )
  val emptyProcedureCodesJSON: JsValue = JsObject(
    Map("procedureCode" -> JsString(""), "additionalProcedureCodes" -> JsArray())
  )

}
