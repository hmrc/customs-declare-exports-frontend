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

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class DeclarationAdditionalActors(
  eori: Option[String],
  partyType: Option[String]
)

object DeclarationAdditionalActors {
  implicit val format = Json.format[DeclarationAdditionalActors]

  private val allowedDeclarationAdditionalActorsValues = Set("CS", "MF", "FW", "WH")

  val formId = "DeclarationAdditionalActors"

  val mapping = Forms.mapping(
    "eori" -> optional(
      text().verifying("supplementary.eori.error", input => input.length <= 17)
    ),
    "partyType" -> optional(
      text().verifying("supplementary.partyType.error", input => allowedDeclarationAdditionalActorsValues(input))
    )
  )(DeclarationAdditionalActors.apply)(DeclarationAdditionalActors.unapply)

  def form(): Form[DeclarationAdditionalActors] = Form(mapping)
}
