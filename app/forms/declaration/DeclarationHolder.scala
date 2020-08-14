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
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import services.HolderOfAuthorisationCode
import utils.validators.forms.FieldValidator._
import views.pdf.DeclarationType.DeclarationType

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[Eori]) {
  override def toString: String = id

  def id: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
  def isEmpty: Boolean = authorisationTypeCode.isEmpty && eori.isEmpty
  def isComplete: Boolean = authorisationTypeCode.isDefined && eori.isDefined
}

object DeclarationHolder extends DeclarationPage {
  implicit val format = Json.format[DeclarationHolder]

  private def eoriMapping = optional(Eori.mapping)

  private def codeMapping =
    optional(text().verifying("declaration.declarationHolder.authorisationCode.invalid", isContainedIn(HolderOfAuthorisationCode.all.map(_.code))))

  val optionalMapping: Mapping[DeclarationHolder] =
    Forms.mapping("authorisationTypeCode" -> codeMapping, "eori" -> eoriMapping)(DeclarationHolder.apply)(DeclarationHolder.unapply)

  val requiredMapping: Mapping[DeclarationHolder] = Forms.mapping(
    "authorisationTypeCode" -> codeMapping.verifying("declaration.declarationHolder.authorisationCode.empty", _.isDefined),
    "eori" -> eoriMapping.verifying("declaration.eori.empty", _.isDefined)
  )(DeclarationHolder.apply)(DeclarationHolder.unapply)

  def form(optional: Boolean = false): Form[DeclarationHolder] = if (optional) Form(optionalMapping) else Form(requiredMapping)

  // Method for parse format typeCode-eori
  def buildId(value: String): DeclarationHolder = {
    val dividedString: Array[String] = value.split('-')

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

object DeclarationSummaryHolder extends DeclarationPage
