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

package controllers.helpers

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.declarationHolder.DeclarationHolder
import models.requests.JourneyRequest
import play.api.data.FormError

object DeclarationHolderHelper {

  val DeclarationHolderFormGroupId: String = "declarationHolder"

  def cachedHolders(implicit request: JourneyRequest[_]): Seq[DeclarationHolder] =
    request.cacheModel.parties.declarationHoldersData.map(_.holders).getOrElse(Seq.empty)

  def validateAuthCode(maybeDeclarationHolder: Option[DeclarationHolder])(implicit r: JourneyRequest[_]): Option[FormError] =
    maybeDeclarationHolder match {
      case Some(DeclarationHolder(Some(authorisationCode), _, _)) =>
        if (isExrrSelectedForPrelodgedDecl(authorisationCode, r.cacheModel.additionalDeclarationType))
          Some(FormError(DeclarationHolderFormGroupId, "declaration.declarationHolder.EXRR.error.prelodged"))
        else
          validateMutuallyExclusiveAuthCodes(authorisationCode, cachedHolders)

      case _ => None
    }

  private val nonExrrAdditionalDeclarationTypes =
    List(STANDARD_PRE_LODGED, SIMPLIFIED_PRE_LODGED, OCCASIONAL_PRE_LODGED, CLEARANCE_PRE_LODGED)

  private def isExrrSelectedForPrelodgedDecl(authorisationCode: String, declarationType: Option[AdditionalDeclarationType]): Boolean =
    declarationType.fold(false)(authorisationCode == "EXRR" && nonExrrAdditionalDeclarationTypes.contains(_))

  private val mutuallyExclusiveAuthorisationCodes = List("CSE", "EXRR")

  private def validateMutuallyExclusiveAuthCodes(authorisationCode: String, holders: Seq[DeclarationHolder]): Option[FormError] =
    if (!mutuallyExclusiveAuthorisationCodes.contains(authorisationCode)) None
    else {
      val mustNotAlreadyContainCodes = mutuallyExclusiveAuthorisationCodes.filter(_ != authorisationCode)

      if (!holders.map(_.authorisationTypeCode.getOrElse("")).containsSlice(mustNotAlreadyContainCodes)) None
      else Some(FormError(DeclarationHolderFormGroupId, s"declaration.declarationHolder.${authorisationCode}.error.exclusive"))
    }
}
