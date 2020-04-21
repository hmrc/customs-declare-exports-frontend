/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.declaration

import forms.DeclarationPage
import forms.common.Eori
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import services.HolderOfAuthorisationCode
import utils.validators.forms.FieldValidator._

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[Eori]) {
  override def toString: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
}

object DeclarationHolder extends DeclarationPage {
  implicit val format = Json.format[DeclarationHolder]

  val mapping = Forms.mapping(
    "authorisationTypeCode" -> optional(
      text()
        .verifying("declaration.declarationHolder.authorisationCode.invalid", isContainedIn(HolderOfAuthorisationCode.all.map(_.value)))
    ),
    "eori" -> optional(Eori.mapping("supplementary"))
  )(DeclarationHolder.apply)(DeclarationHolder.unapply)

  def form(): Form[DeclarationHolder] = Form(mapping)

  // Method for parse format typeCode-eori
  def buildFromString(value: String): DeclarationHolder = {
    val dividedString: Array[String] = value.split('-')

    if (dividedString.length == 0) DeclarationHolder(None, None)
    else if (dividedString.length == 1) DeclarationHolder(Some(value.split('-')(0)), None)
    else DeclarationHolder(Some(value.split('-')(0)), Some(Eori(value.split('-')(1))))
  }
}
