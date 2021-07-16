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

package views.helpers

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType._
import models.declaration.AuthorisationProcedureCode._
import models.ExportsDeclaration

object DynamicContent {

  def whichDeclarationHolderRequiredContent(model: ExportsDeclaration): Seq[String] =
    (model.`type`, model.additionalDeclarationType, model.parties.authorisationProcedureCodeChoice.map(_.code)) match {
      case (STANDARD, Some(STANDARD_PRE_LODGED), Some(Code1040))  => Seq("standard_prelodged_1040")
      case (STANDARD, Some(STANDARD_PRE_LODGED), Some(CodeOther)) => Seq("standard_prelodged_other")
      case (OCCASIONAL, _, _)                                     => (1 to 2).map(idx => s"occasional.$idx")
      case _                                                      => Seq("default")
    }
}
