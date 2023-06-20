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
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.declaration.EoriSource.UserEori
import models.declaration.{EoriSource, ImplicitlySequencedObject}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, compareStringDifference, ExportsDeclarationDiff}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class DeclarationHolder(authorisationTypeCode: Option[String], eori: Option[Eori], eoriSource: Option[EoriSource])
    extends DiffTools[DeclarationHolder] with ImplicitlySequencedObject {

  // eoriSource is not used to generate the WCO XML
  def createDiff(original: DeclarationHolder, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(original.authorisationTypeCode, authorisationTypeCode, combinePointers(pointerString, sequenceId)),
      compareDifference(original.eori, eori, combinePointers(pointerString, sequenceId))
    ).flatten

  def id: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
  def isEmpty: Boolean = authorisationTypeCode.isEmpty && eori.isEmpty
  def isComplete: Boolean = authorisationTypeCode.isDefined && eori.isDefined
}

object DeclarationHolder extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "holders"
  val authorisationTypeCodePointer: ExportsFieldPointer = "authorisationTypeCode"
  val eoriPointer: ExportsFieldPointer = "eori"

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
}

object DeclarationHolderRequired extends DeclarationPage {

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE => List(TariffContentKey("tariff.declaration.isAuthorisationRequired.clearance"))
      case _ =>
        List(
          TariffContentKey("tariff.declaration.isAuthorisationRequired.1.common"),
          TariffContentKey("tariff.declaration.isAuthorisationRequired.2.common"),
          TariffContentKey("tariff.declaration.isAuthorisationRequired.3.common")
        )
    }
}

object DeclarationHolderSummary extends DeclarationPage
