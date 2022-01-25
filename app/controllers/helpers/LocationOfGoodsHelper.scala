/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.declarationHolder.AuthorizationTypeCodes
import models.ExportsDeclaration

object LocationOfGoodsHelper {
  def skipLocationOfGoods(declaration: ExportsDeclaration): Boolean =
    declaration.isAdditionalDeclarationType(SUPPLEMENTARY_EIDR) &&
      declaration.declarationHolders.exists(holder => AuthorizationTypeCodes.codesThatSkipLocationOfGoods.contains(holder.authorisationTypeCode))
}
