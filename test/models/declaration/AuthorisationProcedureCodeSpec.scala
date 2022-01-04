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

package models.declaration

import base.UnitSpec
import models.declaration.AuthorisationProcedureCode._
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class AuthorisationProcedureCodeSpec extends UnitSpec {

  "Formatter" should {
    "map to json" in {
      Json.toJson(Code1040.asInstanceOf[AuthorisationProcedureCode]) mustBe JsString("Code1040")
      Json.toJson(Code1007.asInstanceOf[AuthorisationProcedureCode]) mustBe JsString("Code1007")
      Json.toJson(CodeOther.asInstanceOf[AuthorisationProcedureCode]) mustBe JsString("CodeOther")

    }

    "map from json" in {
      Json.fromJson[AuthorisationProcedureCode](JsString("Code1040")) mustBe JsSuccess(Code1040)
      Json.fromJson[AuthorisationProcedureCode](JsString("Code1007")) mustBe JsSuccess(Code1007)
      Json.fromJson[AuthorisationProcedureCode](JsString("CodeOther")) mustBe JsSuccess(CodeOther)
      Json.fromJson[AuthorisationProcedureCode](JsString("other")) mustBe JsError("error.unknown")
    }
  }
}
