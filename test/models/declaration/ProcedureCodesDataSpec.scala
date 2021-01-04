/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

class ProcedureCodesDataSpec extends WordSpec with MustMatchers {

  "Procedure code" should {

    "contain correct limit" in {

      val expectedProceduresCodesDataMaxAmount = 99

      ProcedureCodesData.limitOfCodes must be(expectedProceduresCodesDataMaxAmount)
    }
  }
}

object ProcedureCodesDataSpec {
  private val procedureCode_1 = "12"
  private val procedureCode_2 = "34"

  val correctProcedureCodes =
    ProcedureCodesData(procedureCode = Some(procedureCode_1 + procedureCode_2), additionalProcedureCodes = Seq("111", "222", "333"))

  val emptyProcedureCodes = ProcedureCodesData(procedureCode = None, additionalProcedureCodes = Seq.empty)

  val correctProcedureCodesJSON: JsValue = JsObject(
    Map(
      "procedureCode" -> JsString(procedureCode_1 + procedureCode_2),
      "additionalProcedureCodes" -> JsArray(Seq(JsString("111"), JsString("222"), JsString("333")))
    )
  )

  val emptyProcedureCodesJSON: JsValue = JsObject(Map("procedureCode" -> JsString(""), "additionalProcedureCodes" -> JsArray()))
}
