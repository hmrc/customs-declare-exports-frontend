/*
 * Copyright 2022 HM Revenue & Customs
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
import base.UnitSpec
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class AdditionalDeclarationTypeSpec extends UnitSpec {

  "Formatter" should {
    "map to json" in {
      Json.toJson(AdditionalDeclarationType.STANDARD_FRONTIER) mustBe JsString("A")
      Json.toJson(AdditionalDeclarationType.STANDARD_PRE_LODGED) mustBe JsString("D")
      Json.toJson(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED) mustBe JsString("Y")
      Json.toJson(AdditionalDeclarationType.SUPPLEMENTARY_EIDR) mustBe JsString("Z")
      Json.toJson(AdditionalDeclarationType.SIMPLIFIED_FRONTIER) mustBe JsString("C")
      Json.toJson(AdditionalDeclarationType.SIMPLIFIED_PRE_LODGED) mustBe JsString("F")
      Json.toJson(AdditionalDeclarationType.OCCASIONAL_FRONTIER) mustBe JsString("B")
      Json.toJson(AdditionalDeclarationType.OCCASIONAL_PRE_LODGED) mustBe JsString("E")
      Json.toJson(AdditionalDeclarationType.CLEARANCE_FRONTIER) mustBe JsString("J")
      Json.toJson(AdditionalDeclarationType.CLEARANCE_PRE_LODGED) mustBe JsString("K")
    }

    "map from json" in {
      Json.fromJson[AdditionalDeclarationType](JsString("A")) mustBe JsSuccess(AdditionalDeclarationType.STANDARD_FRONTIER)
      Json.fromJson[AdditionalDeclarationType](JsString("D")) mustBe JsSuccess(AdditionalDeclarationType.STANDARD_PRE_LODGED)
      Json.fromJson[AdditionalDeclarationType](JsString("Y")) mustBe JsSuccess(AdditionalDeclarationType.SUPPLEMENTARY_SIMPLIFIED)
      Json.fromJson[AdditionalDeclarationType](JsString("Z")) mustBe JsSuccess(AdditionalDeclarationType.SUPPLEMENTARY_EIDR)
      Json.fromJson[AdditionalDeclarationType](JsString("C")) mustBe JsSuccess(AdditionalDeclarationType.SIMPLIFIED_FRONTIER)
      Json.fromJson[AdditionalDeclarationType](JsString("F")) mustBe JsSuccess(AdditionalDeclarationType.SIMPLIFIED_PRE_LODGED)
      Json.fromJson[AdditionalDeclarationType](JsString("B")) mustBe JsSuccess(AdditionalDeclarationType.OCCASIONAL_FRONTIER)
      Json.fromJson[AdditionalDeclarationType](JsString("E")) mustBe JsSuccess(AdditionalDeclarationType.OCCASIONAL_PRE_LODGED)
      Json.fromJson[AdditionalDeclarationType](JsString("J")) mustBe JsSuccess(AdditionalDeclarationType.CLEARANCE_FRONTIER)
      Json.fromJson[AdditionalDeclarationType](JsString("K")) mustBe JsSuccess(AdditionalDeclarationType.CLEARANCE_PRE_LODGED)

      Json.fromJson[AdditionalDeclarationType](JsString("other")) mustBe an[JsError]
    }
  }
}
