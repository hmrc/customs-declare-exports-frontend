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

package unit.services.model

import services.model.DmsRejError
import unit.base.UnitSpec

class DmsRejErrorSpec extends UnitSpec {

  "Dms Rejected Error model" should {

    "create correct error based on the list" in {

      val errorCode = "ErrorCode"
      val errorDescription = "Error description"
      val error = List(errorCode, errorDescription)

      DmsRejError.apply(error) mustBe DmsRejError(errorCode, errorDescription)
    }

    "throw an exception when input is incorrect" in {

      intercept[IllegalArgumentException](DmsRejError.apply(List.empty))
    }
  }
}
