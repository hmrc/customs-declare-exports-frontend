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

package models.declaration

import forms.declaration.DeclarationHolder
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}

object DeclarationHoldersDataSpec {
  val correctDeclarationHolder =
    DeclarationHolder(authorisationTypeCode = Some("1234"), eori = Some("PL213472539481923"))
  val correctDeclarationHoldersData = DeclarationHoldersData(Seq(correctDeclarationHolder))
  val correctDeclarationHolderJSON: JsValue = JsObject(Map("authorisationTypeCode" -> JsString("1234"), "eori" -> JsString("PL213472539481923")))
  val anotherCorrectDeclarationHolderJSON: JsValue = JsObject(
    Map("authorisationTypeCode" -> JsString("4321"), "eori" -> JsString("PT213472539481923"))
  )
  val correctDeclarationHoldersDataJSON: JsValue = JsObject(
    Map("holders" -> JsArray(Seq(correctDeclarationHolderJSON, anotherCorrectDeclarationHolderJSON)))
  )
  private val eoriMaxLength = 17
}
