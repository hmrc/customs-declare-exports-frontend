/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import services.HolderOfAuthorisationCode
import utils.validators.forms.FieldValidator._

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[Eori]) {
  override def toString: String = id

  def id: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
  def isEmpty: Boolean = authorisationTypeCode.isEmpty && eori.isEmpty
  def isComplete: Boolean = authorisationTypeCode.isDefined && eori.isDefined
}

object DeclarationHolder extends DeclarationPage {
  implicit val format = Json.format[DeclarationHolder]

  private def codeMapping: Mapping[Option[String]] =
    optional(
      text()
        .verifying("declaration.declarationHolder.authorisationCode.invalid", isContainedIn(HolderOfAuthorisationCode.all.map(_.code)))
    )

  private def eoriMapping: Mapping[Option[Eori]] = optional(Eori.mapping)

  val mandatoryMapping: Mapping[DeclarationHolder] =
    Forms.mapping(
      "authorisationTypeCode" -> codeMapping.verifying("declaration.declarationHolder.authorisationCode.empty", _.isDefined),
      "eori" -> eoriMapping.verifying("declaration.eori.empty", _.isDefined)
    )(DeclarationHolder.apply)(DeclarationHolder.unapply)

  val form: Form[DeclarationHolder] = Form(mandatoryMapping)

  // Method to parse format typeCode-eori
  def fromId(id: String): DeclarationHolder = {
    val dividedString: Array[String] = id.split('-').filterNot(_.isEmpty)

    dividedString.length match {
      case 0 => DeclarationHolder(None, None)
      case 1 => DeclarationHolder(Some(dividedString(0).trim), None)
      case _ => DeclarationHolder(Some(dividedString(0).trim), Some(Eori(dividedString(1).trim)))
    }
  }
}

object DeclarationHolderRequired extends DeclarationPage
object DeclarationSummaryHolder extends DeclarationPage
