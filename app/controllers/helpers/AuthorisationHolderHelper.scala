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

package controllers.helpers

import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.authorisationHolder.AuthorisationHolder
import models.DeclarationType.{CLEARANCE, OCCASIONAL}
import models.ExportsDeclaration
import models.requests.JourneyRequest
import play.api.data.Form

object AuthorisationHolderHelper {

  def authorisationHolders(implicit request: JourneyRequest[_]): Seq[AuthorisationHolder] = request.cacheModel.authorisationHolders

  def form(implicit request: JourneyRequest[_]): Form[AuthorisationHolder] =
    AuthorisationHolder.form(request.eori, request.cacheModel.additionalDeclarationType)

  def userCanLandOnIsAuthRequiredPage(declaration: ExportsDeclaration): Boolean =
    (declaration.additionalDeclarationType, declaration.parties.authorisationProcedureCodeChoice) match {
      case (Some(STANDARD_PRE_LODGED) | Some(STANDARD_FRONTIER), Choice1040) => true
      case (Some(STANDARD_PRE_LODGED), ChoiceOthers)                         => true
      case _                                                                 => declaration.isType(CLEARANCE) || declaration.isType(OCCASIONAL)
    }
}
