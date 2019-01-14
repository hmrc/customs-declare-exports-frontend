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

package forms.supplementary

import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[String])

object DeclarationHolder {
  implicit val format = Json.format[DeclarationHolder]

  // TODO remove controller from formId - it can break data on every environment, but should be done without bigger problems
  val formId = "DeclarationHolderController"

  val authorizationCodePattern = "[0-9A-Z]{1,4}"
  val eoriPattern = "[0-9a-zA-Z]{1,17}"

  val mapping = Forms.mapping(
    "authorisationTypeCode" -> optional(
      text().verifying("supplementary.declarationHolder.authorisationCode.error", _.matches(authorizationCodePattern))
    ),
    "eori" -> optional(text().verifying("supplementary.eori.error", _.matches(eoriPattern)))
  )(DeclarationHolder.apply)(DeclarationHolder.unapply)

  def form(): Form[DeclarationHolder] = Form(mapping)

  def toMetadataProperties(holder: DeclarationHolder): Map[String, String] =
    Map(
      "declaration.authorisationHolder.categoryCode" -> holder.authorisationTypeCode.getOrElse(""),
      "declaration.authorisationHolder.ID" -> holder.eori.getOrElse("")
    )
}
