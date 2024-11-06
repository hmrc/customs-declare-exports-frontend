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

package forms.section6

import forms.common.DeclarationPageBaseSpec
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class TransportPaymentSpec extends DeclarationPageBaseSpec {

  def formData(paymentMethod: String) =
    JsObject(Map("paymentMethod" -> JsString(paymentMethod)))

  "TransportContainer mapping" should {

    "return form with errors" when {
      "provided with invalid payment method" in {
        val form = TransportPayment.form.bind(formData("invalid"), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("paymentMethod", "declaration.transportInformation.transportPayment.paymentMethod.error.empty"))
      }

      "provided with no input for required question" in {
        val form = TransportPayment.form.bind(formData(""), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("paymentMethod", "declaration.transportInformation.transportPayment.paymentMethod.error.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input" in {
        val form = TransportPayment.form.bind(formData(TransportPayment.cash), JsonBindMaxChars)

        form.hasErrors must be(false)
      }
    }
  }
}
