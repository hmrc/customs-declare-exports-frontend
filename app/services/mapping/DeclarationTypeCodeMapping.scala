/*
 * Copyright 2019 HM Revenue & Customs
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

package services.mapping

import forms.declaration.DispatchLocation
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import wco.datamodel.wco.declaration_ds.dms._2.DeclarationTypeCodeType

object DeclarationTypeCodeMapping {

  def additionalDeclarationTypeAndDispatchLocationToDeclarationTypeCode(
    dispatchLocation: Option[DispatchLocation],
    additionalDeclarationType: Option[AdditionalDeclarationType]
  ): DeclarationTypeCodeType = {
    val declarationTypeCodeType = new DeclarationTypeCodeType
    val typeCode: String = dispatchLocation.map(_.dispatchLocation).getOrElse("") +
      additionalDeclarationType.map(_.additionalDeclarationType).getOrElse("")
    declarationTypeCodeType.setValue(typeCode)
    declarationTypeCodeType
  }
}
