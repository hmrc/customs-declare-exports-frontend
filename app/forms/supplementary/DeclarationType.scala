/*
 * Copyright 2018 HM Revenue & Customs
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

package forms.supplementary

import play.api.data.Forms.nonEmptyText
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class DeclarationType(
  declarationType: String,
  additionalDeclarationType: String
)

object DeclarationType {
  implicit val format = Json.format[DeclarationType]

  private val declarationTypeAllowedValues = Set("CO", "EX")
  private val additionalDeclarationTypeAllowedValues = Set("Y", "Z")

  val formId = "DeclarationTypeId"

  val mapping = Forms.mapping(
    "declarationType" -> nonEmptyText(maxLength = 2)
      .verifying("Please, provide valid declaration type", declarationTypeAllowedValues(_)),
    "additionalDeclarationType" -> nonEmptyText(maxLength = 1)
      .verifying("Please, provide valid additional declaration type", additionalDeclarationTypeAllowedValues(_))
  )(DeclarationType.apply)(DeclarationType.unapply)

  val form = Form(mapping)
}
