/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.section2.authorisationHolder

import forms.DeclarationPage
import forms.common.Eori
import forms.mappings.MappingHelper.requiredRadio
import forms.section1.AdditionalDeclarationType._
import forms.section2.authorisationHolder.AuthorizationTypeCodes.EXRR
import models.DeclarationType.{CLEARANCE, DeclarationType}
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import models.declaration.EoriSource.UserEori
import models.declaration.Parties.partiesPrefix
import models.declaration.{EoriSource, ImplicitlySequencedObject}
import models.viewmodels.TariffContentKey
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import services.DiffTools
import services.DiffTools.{combinePointers, compareDifference, compareStringDifference, ExportsDeclarationDiff}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

case class AuthorisationHolder(authorisationTypeCode: Option[String], eori: Option[Eori], eoriSource: Option[EoriSource])
    extends DiffTools[AuthorisationHolder] with ImplicitlySequencedObject {

  // eoriSource is not used to generate the WCO XML
  def createDiff(original: AuthorisationHolder, pointerString: ExportsFieldPointer, sequenceId: Option[Int] = None): ExportsDeclarationDiff =
    Seq(
      compareStringDifference(
        original.authorisationTypeCode,
        authorisationTypeCode,
        combinePointers(pointerString, AuthorisationHolder.authorisationTypeCodePointer, sequenceId)
      ),
      compareDifference(original.eori, eori, combinePointers(pointerString, AuthorisationHolder.eoriPointer, sequenceId))
    ).flatten

  def id: String = s"${authorisationTypeCode.getOrElse("")}-${eori.getOrElse("")}"
  def isEmpty: Boolean = authorisationTypeCode.isEmpty && eori.isEmpty
  def isComplete: Boolean = authorisationTypeCode.isDefined && eori.isDefined
}

object AuthorisationHolder extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "holders"
  val authorisationTypeCodePointer: ExportsFieldPointer = "authorisationTypeCode"
  val eoriPointer: ExportsFieldPointer = "eori"

  lazy val keyForEori = s"${partiesPrefix}.holders.holder.eori"
  lazy val keyForTypeCode = s"${partiesPrefix}.holders.holder.type"

  implicit val format: OFormat[AuthorisationHolder] = Json.format[AuthorisationHolder]

  val authorisationHolderFormGroupId: String = "authorisationHolder"
  val AuthorisationTypeCodeId = "authorisationTypeCode"
  val EoriId = "eori"
  val EoriSourceId = "eoriSource"

  def form(eori: String, additionalDeclarationType: Option[AdditionalDeclarationType]): Form[AuthorisationHolder] =
    Form(mapping(eori, additionalDeclarationType))

  def mapping(userEori: String, additionalDeclarationType: Option[AdditionalDeclarationType]): Mapping[AuthorisationHolder] =
    Forms.mapping(
      AuthorisationTypeCodeId ->
        optional(text())
          .verifying("declaration.authorisationHolder.authorisationCode.empty", _.isDefined)
          .verifying("declaration.authorisationHolder.EXRR.error.prelodged", nonExrrSelectedForPrelodgedDecl(_, additionalDeclarationType)),
      EoriId -> mandatoryIfEqual(EoriSourceId, EoriSource.OtherEori.toString, Eori.mapping("declaration.authorisationHolder.eori.other.error.empty")),
      EoriSourceId -> requiredRadio("declaration.authorisationHolder.eori.error.radio", EoriSource.values.map(_.toString))
    )(applyAuthorisationHolder(userEori))(unapplyAuthorisationHolder)

  private def nonExrrSelectedForPrelodgedDecl(maybeCode: Option[String], maybeAdditionalDeclarationType: Option[AdditionalDeclarationType]): Boolean =
    maybeCode.fold(true) { authorisationCode =>
      maybeAdditionalDeclarationType.fold(true) { additionalDeclarationType =>
        !(authorisationCode == EXRR && isPreLodged(additionalDeclarationType))
      }
    }

  private def applyAuthorisationHolder(userEori: String)(authorisationCode: Option[String], eori: Option[Eori], eoriSource: String) = {
    val maybeEoriSource = EoriSource.lookupByValue.get(eoriSource)

    (eori, maybeEoriSource) match {
      case (None, Some(sourceEori)) if sourceEori.equals(UserEori) =>
        AuthorisationHolder(authorisationCode, Some(Eori(userEori)), maybeEoriSource)

      case _ =>
        AuthorisationHolder(authorisationCode, eori, maybeEoriSource)
    }
  }

  private def unapplyAuthorisationHolder(authorisationHolder: AuthorisationHolder): Option[(Option[String], Option[Eori], String)] = {
    val maybeEoriSourceValue = authorisationHolder.eoriSource.map(_.toString) orElse authorisationHolder.eori.map(_ => EoriSource.OtherEori.toString)

    Some((authorisationHolder.authorisationTypeCode, authorisationHolder.eori, maybeEoriSourceValue.getOrElse("")))
  }
}

object AuthorisationHolderRequired extends DeclarationPage {

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    decType match {
      case CLEARANCE =>
        List(TariffContentKey("tariff.declaration.isAuthorisationRequired.clearance"))
      case _ =>
        List(TariffContentKey("tariff.declaration.isAuthorisationRequired.common"))
    }
}

object AuthorisationHolderSummary extends DeclarationPage
