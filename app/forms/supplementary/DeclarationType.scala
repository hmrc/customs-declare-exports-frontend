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

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class DeclarationType(
  declarationType: String, // 2 upper case alphabetic characters
  additionalDeclarationType: String // 1 upper case alphabetic character
) {

  def toMetadataProperties(): Map[String, String] = {
    val propertiesKey = "declaration.typeCode"
    val propertiesValue = declarationType + additionalDeclarationType
    Map(propertiesKey -> propertiesValue)
  }
}

object DeclarationType {
  implicit val format = Json.format[DeclarationType]

  private val declarationTypeAllowedValues = Set(
    AllowedTypes.OutsideEU,
    AllowedTypes.FiscalTerritory
  )
  private val additionalDeclarationTypeAllowedValues = Set(
    AllowedAdditionalTypes.Simplified,
    AllowedAdditionalTypes.Standard
  )

  val formId = "DeclarationTypeId"

  val mapping = Forms.mapping(
    "declarationType" -> text(maxLength = 2)
      .verifying("supplementary.declarationTypePage.inputText.declarationType.errorMessage",
        input => input.nonEmpty && declarationTypeAllowedValues(input)),
    "additionalDeclarationType" -> text(maxLength = 1)
      .verifying("supplementary.declarationTypePage.inputText.additionalDeclarationType.errorMessage",
        input => input.nonEmpty && additionalDeclarationTypeAllowedValues(input))
  )(DeclarationType.apply)(DeclarationType.unapply)

  def form(): Form[DeclarationType] = Form(mapping)

  object AllowedTypes {
    val OutsideEU = "EX"
    val FiscalTerritory = "CO"
  }

  object AllowedAdditionalTypes {
    val Simplified = "Y"
    val Standard = "Z"
  }
}
