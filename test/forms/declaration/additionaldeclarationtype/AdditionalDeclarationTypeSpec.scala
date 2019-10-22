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

package forms.declaration.additionaldeclarationtype

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class AdditionalDeclarationTypeSpec extends WordSpec with MustMatchers {

  "Formatter" should {
    "map to json" in {
      Json.toJson(AdditionalDeclarationType.STANDARD_FRONTIER) mustBe JsString("A")
      Json.toJson(AdditionalDeclarationType.STANDARD_PRE_LODGED) mustBe JsString("D")
      Json.toJson(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED) mustBe JsString("Y")
      Json.toJson(AdditionalDeclarationType.SUPPLEMENTARY_EIDR) mustBe JsString("Z")
    }

    "map from json" in {
      Json.fromJson[AdditionalDeclarationType](JsString("A")) mustBe JsSuccess(AdditionalDeclarationType.STANDARD_FRONTIER)
      Json.fromJson[AdditionalDeclarationType](JsString("D")) mustBe JsSuccess(AdditionalDeclarationType.STANDARD_PRE_LODGED)
      Json.fromJson[AdditionalDeclarationType](JsString("Y")) mustBe JsSuccess(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED)
      Json.fromJson[AdditionalDeclarationType](JsString("Z")) mustBe JsSuccess(AdditionalDeclarationType.SUPPLEMENTARY_EIDR)
      Json.fromJson[AdditionalDeclarationType](JsString("other")) mustBe an[JsError]
    }
  }

}
