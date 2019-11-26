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

import play.api.libs.json.{Format, JsString, Reads, Writes}

object AdditionalDeclarationType extends Enumeration {
  type AdditionalDeclarationType = Value
  implicit val format: Format[AdditionalDeclarationType.Value] =
    Format(
      Reads(
        _.validate[String]
          .filter(`type` => AdditionalDeclarationType.values.exists(_.toString == `type`))
          .map(AdditionalDeclarationType.from(_).get)
      ),
      Writes(value => JsString(value.toString))
    )

  val SUPPLEMENTARY_SIMPLIFIED = Value("Y")
  val SUPPLEMENTARY_EIDR = Value("Z")
  val STANDARD_PRE_LODGED = Value("D")
  val STANDARD_FRONTIER = Value("A")
  val SIMPLIFIED_FRONTIER = Value("C")
  val SIMPLIFIED_PRE_LODGED = Value("F")
  val OCCASIONAL_FRONTIER = Value("B")
  val OCCASIONAL_PRE_LODGED = Value("E")

  def from(string: String): Option[AdditionalDeclarationType] = AdditionalDeclarationType.values.find(_.toString == string)

  def asText(additionalDeclarationType: AdditionalDeclarationType): String = additionalDeclarationType match {
    case STANDARD_PRE_LODGED | SIMPLIFIED_PRE_LODGED => "Pre-lodged"
    case STANDARD_FRONTIER | SIMPLIFIED_FRONTIER     => "Frontier"
    case SUPPLEMENTARY_EIDR                          => "Z" //TODO Don't know what's this
    case SUPPLEMENTARY_SIMPLIFIED                    => "Simplified"
  }
}
