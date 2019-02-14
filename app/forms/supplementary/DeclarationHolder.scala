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
import utils.validators.FormFieldValidator._

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[String]) {
  override def toString: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
}

object DeclarationHolder {
  implicit val format = Json.format[DeclarationHolder]

  val mapping = Forms.mapping(
    "authorisationTypeCode" -> optional(
      text().verifying("supplementary.declarationHolder.authorisationCode.error", lengthInRange(1)(4) and isAlphanumeric)
    ),
    "eori" -> optional(text().verifying("supplementary.eori.error", lengthInRange(1)(17) and isAlphanumeric))
  )(DeclarationHolder.apply)(DeclarationHolder.unapply)

  def form(): Form[DeclarationHolder] = Form(mapping)

  // Method for parse format typeCode-eori
  def buildFromString(value: String): DeclarationHolder = {
    val dividedString = value.split('-')

    if (dividedString.length == 0) DeclarationHolder(None, None)
    else if (dividedString.length == 1) DeclarationHolder(Some(value.split('-')(0)), None)
    else DeclarationHolder(Some(value.split('-')(0)), Some(value.split('-')(1)))
  }
}
