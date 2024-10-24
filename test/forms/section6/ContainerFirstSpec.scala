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
import forms.section6.ContainerFirst._
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class ContainerFirstSpec extends DeclarationPageBaseSpec {

  "ContainerYesNo mapping" should {

    "return form with errors" when {

      "provided with non-alphanumeric id" in {
        val form = ContainerFirst.form.bind(formData("Yes", Some("!2345678")), JsonBindMaxChars)
        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInformation.containerId.error.invalid"))
      }

      "provided with id too long" in {
        val form = ContainerFirst.form.bind(formData("Yes", Some("123456789012345678")), JsonBindMaxChars)
        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInformation.containerId.error.length"))
      }

      "provided with no id when user said yes" in {
        val form = ContainerFirst.form.bind(formData("Yes", None), JsonBindMaxChars)
        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInformation.containerId.empty"))
      }

      "no answer for yes/no" in {
        val form = ContainerFirst.form.bind(formData("", None), JsonBindMaxChars)
        form.errors mustBe Seq(FormError(hasContainerKey, "declaration.transportInformation.container.answer.empty"))
      }
    }

    "return form without errors" when {
      "provided with valid input when user said Yes" in {
        val form = ContainerFirst.form.bind(formData("Yes", Some("1234ABCD")), JsonBindMaxChars)
        form.hasErrors must be(false)
      }

      "provided with no input when user said No" in {
        val form = ContainerFirst.form.bind(formData("No", None), JsonBindMaxChars)
        form.hasErrors must be(false)
      }
    }
  }

  private def formData(hasContainer: String, containerId: Option[String]): JsObject =
    JsObject(Map(hasContainerKey -> JsString(hasContainer), containerIdKey -> JsString(containerId.getOrElse(""))))
}
