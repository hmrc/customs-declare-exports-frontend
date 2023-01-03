/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import base.{ExportsTestData, UnitWithMocksSpec}
import play.api.data.{Form, FormError}

class MrnSpec extends UnitWithMocksSpec {

  "Mrn mapping" should {
    "return error for incorrect Mrn" in {

      val incorrectMrn = Mrn("91B123456664559-654A")
      val filledForm = Form("mrn" -> Mrn.mapping("test")).fillAndValidate(incorrectMrn)

      val expectedError = FormError("mrn", "test.error.invalid")

      filledForm.errors mustBe Seq(expectedError)
    }

    "has no errors for correct Mrn" in {
      val correctMrn = Mrn(ExportsTestData.mrn)
      val filledForm = Form(Mrn.mapping("test")).fillAndValidate(correctMrn)

      filledForm.errors mustBe empty
    }
  }

  "Mrn isValid" should {
    "correctly detect valid MRN values" in {
      Mrn.isValid(ExportsTestData.mrn) mustBe true
    }

    "correctly detect invalid MRN values" in {
      withClue("empty")(Mrn.isValid("") mustBe false)
      withClue("too long")(Mrn.isValid(s"${ExportsTestData.mrn}EXTRA") mustBe false)

      withClue("first char is not a digit")(Mrn.isValid(s"X0${ExportsTestData.mrn.drop(2)}") mustBe false)
      withClue("second char is not a digit")(Mrn.isValid(s"1X${ExportsTestData.mrn.drop(2)}") mustBe false)

      withClue("third char is not an alpha")(Mrn.isValid(s"001B${ExportsTestData.mrn.drop(4)}") mustBe false)
      withClue("forth char is not an alpha")(Mrn.isValid(s"00G1${ExportsTestData.mrn.drop(4)}") mustBe false)

      withClue("non-alphanumeric char present")(Mrn.isValid(s"!${ExportsTestData.mrn.drop(1)}") mustBe false)
    }
  }
}
