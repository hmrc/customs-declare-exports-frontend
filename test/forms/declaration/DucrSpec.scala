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

package forms.declaration

import base.UnitWithMocksSpec
import play.api.data.{Form, FormError}

class DucrSpec extends UnitWithMocksSpec {

  "Ducr" should {

    "correctly convert DUCR to upper case characters" in {
      Ducr.form.bind(Map("ducr" -> "9gb123456664559-1abc")).get mustBe Ducr("9GB123456664559-1ABC")
    }
  }

  "Ducr mapping" should {

    "return error for incorrect DUCR" in {

      val incorrectDucr = Ducr("91B123456664559-654A")
      val filledForm = Form(Ducr.mapping).fillAndValidate(incorrectDucr)

      val expectedError = FormError("ducr", "declaration.consignmentReferences.ducr.error.invalid")

      filledForm.errors mustBe Seq(expectedError)
    }

    "has no errors for correct Ducr" in {

      val correctDucr = Ducr("9GB123456664559-1H7(1)-/")
      val filledForm = Form(Ducr.mapping).fillAndValidate(correctDucr)

      filledForm.errors mustBe empty
    }
  }
}
