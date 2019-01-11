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

  private val procedureCode = "1234"
  private val additionalProcedureCodes = Seq("111", "222", "333")

  private val procedureCodes = ProcedureCodes(
    procedureCode = procedureCode,
    additionalProcedureCodes = additionalProcedureCodes
  )

  private val expectedProcedureCodesProperties: Map[String, String] = Map(
    "sth.sth.sth" -> procedureCode,
    "sth.sth[0].sth" -> additionalProcedureCodes(0),
    "sth.sth[1].sth" -> additionalProcedureCodes(1),
    "sth.sth[2].sth" -> additionalProcedureCodes(2)
  )

  "ProcedureCodes" should {
    "convert itself into procedure codes properties" in {
      procedureCodes.toMetadataProperties() must equal(expectedProcedureCodesProperties)
    }
  }


}
