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

package forms.declaration

import forms.declaration.ContainerYesNo._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class ContainerYesNoSpec extends WordSpec with MustMatchers {

  def formData(hasContainer: String, containerId: Option[String]) =
    JsObject(Map(hasContainerKey -> JsString(hasContainer), containerIdKey -> JsString(containerId.getOrElse(""))))

  "ContainerYesNo mapping" should {

    "return form with errors" when {
      "provided with id too long" in {
        val form = ContainerYesNo.form.bind(formData("Yes", Some("123456789012345678")))

        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInfo.containerId.error.length"))
      }

      "provided with non-alphanumeric id" in {
        val form = ContainerYesNo.form.bind(formData("Yes", Some("!2345678")))

        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInfo.containerId.error.alphanumeric"))
      }

      "provided with no id when user said yes" in {
        val form = ContainerYesNo.form.bind(formData("Yes", None))

        form.errors mustBe Seq(FormError(containerIdKey, "declaration.transportInfo.containerId.empty"))
      }

      "no answer for yes/no" in {
        val form = ContainerYesNo.form.bind(formData("", None))

        form.errors mustBe Seq(FormError(hasContainerKey, "error.yesNo.required"))
      }
    }

    "return form without errors" when {
      "provided with valid input when user said Yes" in {
        val form = ContainerYesNo.form.bind(formData("Yes", Some("1234ABCD")))

        form.hasErrors must be(false)
      }

      "provided with no input when user said No" in {
        val form = ContainerYesNo.form.bind(formData("No", None))

        form.hasErrors must be(false)
      }

    }
  }
}
