/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section1

import base.ExportsTestData
import base.TestHelper.createRandomAlphanumericString
import forms.common.DeclarationPageBaseSpec
import org.scalatest.Assertion
import play.api.data.{Form, FormError}

class MucrSpec extends DeclarationPageBaseSpec {

  "Mucr" should {

    "correctly convert a MUCR to upper case characters" in {
      Mucr.form2Data("c:xyz1-23d") mustBe Mucr("C:XYZ1-23D")
    }

    "have specific tariff keys" when {
      testTariffContentKeys(Mucr, "tariff.declaration.mucr")
    }
  }

  "Mucr mapping" should {

    "report no errors for valid MUCRs" in {
      val validMucr = Mucr(ExportsTestData.mucr)
      val filledForm = Form(Mucr.mapping).fillAndValidate(validMucr)

      filledForm.errors mustBe empty
    }

    "return error for empty MUCR" in {
      invalidMucr("", "declaration.mucr.error.empty")
    }

    "return error for too long MUCR" in {
      invalidMucr(createRandomAlphanumericString(36), "declaration.mucr.error.invalid")
    }

    "return error for MUCR with non-allowed characters" in {
      invalidMucr("CXYZ123D^&%", "declaration.mucr.error.invalid")
    }
  }

  private def invalidMucr(invalidMucr: String, message: String): Assertion = {
    val filledForm = Form(Mucr.mapping).fillAndValidate(Mucr(invalidMucr))
    val expectedError = FormError(Mucr.MUCR, message)
    filledForm.errors mustBe Seq(expectedError)
  }
}
