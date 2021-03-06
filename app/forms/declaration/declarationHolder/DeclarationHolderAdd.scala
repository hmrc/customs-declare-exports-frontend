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

package forms.declaration.declarationHolder

import forms.DeclarationPage
import forms.common.Eori
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json

case class DeclarationHolderAdd(authorisationTypeCode: Option[String], eori: Option[Eori]) {
  override def toString: String = id

  def id: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
  def isEmpty: Boolean = authorisationTypeCode.isEmpty && eori.isEmpty
  def isComplete: Boolean = authorisationTypeCode.isDefined && eori.isDefined

  def isAdditionalDocumentationRequired: Boolean = authorisationTypeCode.exists(AuthorizationTypeCodes.CodesRequiringDocumentation.contains)
}

object DeclarationHolderAdd extends DeclarationPage {

  implicit val format = Json.format[DeclarationHolderAdd]

  val mandatoryMapping: Mapping[DeclarationHolderAdd] =
    Forms.mapping(
      "authorisationTypeCode" ->
        optional(text()).verifying("declaration.declarationHolder.authorisationCode.empty", _.isDefined),
      "eori" ->
        optional(Eori.mapping()).verifying("declaration.eori.empty", _.isDefined)
    )(DeclarationHolderAdd.apply)(DeclarationHolderAdd.unapply)

  def form: Form[DeclarationHolderAdd] = Form(mandatoryMapping)

  // Method to parse format typeCode-eori
  def fromId(id: String): DeclarationHolderAdd = {
    val dividedString: Array[String] = id.split('-').filterNot(_.isEmpty)

    dividedString.length match {
      case 0 => DeclarationHolderAdd(None, None)
      case 1 => DeclarationHolderAdd(Some(dividedString(0).trim), None)
      case _ => DeclarationHolderAdd(Some(dividedString(0).trim), Some(Eori(dividedString(1).trim)))
    }
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.addAuthorisationRequired.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object DeclarationHolderRequired extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.isAuthorisationRequired.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object DeclarationSummaryHolder extends DeclarationPage
