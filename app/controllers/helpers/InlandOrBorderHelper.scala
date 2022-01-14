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

import forms.declaration.ModeOfTransportCode.RoRo
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import models.ExportsDeclaration
import services.DepCodes.isDesignatedExportPlaceCode

object InlandOrBorderHelper {

  def skipInlandOrBorder(declaration: ExportsDeclaration): Boolean =
    declaration.isAdditionalDeclarationType(SUPPLEMENTARY_EIDR) ||
      declaration.declarationHolders.exists(_.skipInlandOrBorder) ||
      declaration.requiresWarehouseId ||
      declaration.transportLeavingBorderCode == Some(RoRo) ||
      isDesignatedExportPlaceCode(declaration)
}
