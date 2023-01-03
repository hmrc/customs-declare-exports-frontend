/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.MappingHelper.requiredRadio
import forms.common.Eori
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.declarationHolder.AuthorizationTypeCodes._
import models.DeclarationType.DeclarationType
import models.declaration.EoriSource
import models.declaration.EoriSource.UserEori
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[Eori], eoriSource: Option[EoriSource]) {

  def id: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
  def isEmpty: Boolean = authorisationTypeCode.isEmpty && eori.isEmpty
  def isComplete: Boolean = authorisationTypeCode.isDefined && eori.isDefined

  def isAdditionalDocumentationRequired: Boolean = authorisationTypeCode.exists(codesRequiringDocumentation.contains)

  def skipInlandOrBorder: Boolean = authorisationTypeCode.exists(authCodesThatSkipInlandOrBorder.contains)
}

object DeclarationHolder extends DeclarationPage {

  implicit val format = Json.format[DeclarationHolder]

  val DeclarationHolderFormGroupId: String = "declarationHolder"
  val AuthorisationTypeCodeId = "authorisationTypeCode"
  val EoriId = "eori"
  val EoriSourceId = "eoriSource"

  def form(eori: String, additionalDeclarationType: Option[AdditionalDeclarationType]): Form[DeclarationHolder] =
    Form(mapping(eori, additionalDeclarationType))

  def mapping(userEori: String, additionalDeclarationType: Option[AdditionalDeclarationType]): Mapping[DeclarationHolder] =
    Forms.mapping(
      AuthorisationTypeCodeId ->
        optional(text())
          .verifying("declaration.declarationHolder.authorisationCode.empty", _.isDefined)
          .verifying("declaration.declarationHolder.EXRR.error.prelodged", nonExrrSelectedForPrelodgedDecl(_, additionalDeclarationType)),
      EoriId -> mandatoryIfEqual(EoriSourceId, EoriSource.OtherEori.toString, Eori.mapping("declaration.declarationHolder.eori.other.error.empty")),
      EoriSourceId -> requiredRadio("declaration.declarationHolder.eori.error.radio", EoriSource.values.map(_.toString))
    )(applyDeclarationHolder(userEori))(unapplyDeclarationHolder)

  private def nonExrrSelectedForPrelodgedDecl(maybeCode: Option[String], maybeAdditionalDeclarationType: Option[AdditionalDeclarationType]): Boolean =
    maybeCode.fold(true) { authorisationCode =>
      maybeAdditionalDeclarationType.fold(true) { additionalDeclarationType =>
        !(authorisationCode == EXRR && isPreLodged(additionalDeclarationType))
      }
    }

  private def applyDeclarationHolder(userEori: String)(authorisationCode: Option[String], eori: Option[Eori], eoriSource: String) = {
    val maybeEoriSource = EoriSource.lookupByValue.get(eoriSource)

    (eori, maybeEoriSource) match {
      case (None, Some(eoriSource)) if eoriSource.equals(UserEori) =>
        DeclarationHolder(authorisationCode, Some(Eori(userEori)), maybeEoriSource)

      case _ =>
        DeclarationHolder(authorisationCode, eori, maybeEoriSource)
    }
  }

  private def unapplyDeclarationHolder(declarationHolder: DeclarationHolder): Option[(Option[String], Option[Eori], String)] = {
    val maybeEoriSourceValue = declarationHolder.eoriSource.map(_.toString) orElse declarationHolder.eori.map(_ => EoriSource.OtherEori.toString)

    Some((declarationHolder.authorisationTypeCode, declarationHolder.eori, maybeEoriSourceValue.getOrElse("")))
  }

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.addAuthorisationRequired.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))

  // Note that this validation takes places only when adding a new authorisation, not when changing one.
  def validateMutuallyExclusiveAuthCodes(maybeHolder: Option[DeclarationHolder], holders: Seq[DeclarationHolder]): Option[FormError] =
    maybeHolder match {
      case Some(DeclarationHolder(Some(code), _, _)) if mutuallyExclusiveAuthCodes.contains(code) =>
        val mustNotAlreadyContainCodes: List[String] = mutuallyExclusiveAuthCodes.filter(_ != code)

        if (!holders.map(_.authorisationTypeCode.getOrElse("")).containsSlice(mustNotAlreadyContainCodes)) None
        else Some(FormError(AuthorisationTypeCodeId, s"declaration.declarationHolder.${code}.error.exclusive"))

      case _ => None
    }
}

object DeclarationHolderRequired extends DeclarationPage {
  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.isAuthorisationRequired.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}

object DeclarationHolderSummary extends DeclarationPage
