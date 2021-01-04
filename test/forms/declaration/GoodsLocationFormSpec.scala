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

package forms.declaration

import play.api.libs.json.{JsObject, JsString}
import unit.base.UnitSpec

class GoodsLocationFormSpec extends UnitSpec {

  "GoodsLocation form" should {

    "convert to upper case" in {

      def formData(code: String) =
        JsObject(Map("code" -> JsString(code)))

      val form = GoodsLocationForm.form().bind(formData("plaucorrect"))

      form.value.map(_.code) must be(Some("PLAUCORRECT"))
    }

  }
}
