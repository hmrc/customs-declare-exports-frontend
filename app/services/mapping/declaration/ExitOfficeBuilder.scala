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

package services.mapping.declaration

import forms.Choice.AllowedChoiceValues
import forms.declaration.officeOfExit.{OfficeOfExitStandard, OfficeOfExitSupplementary}
import javax.inject.Inject
import models.ExportsDeclaration
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.ExitOffice
import wco.datamodel.wco.declaration_ds.dms._2._

class ExitOfficeBuilder @Inject()() extends ModifyingBuilder[ExportsDeclaration, Declaration] {
  override def buildThenAdd(model: ExportsDeclaration, declaration: Declaration): Unit = model.choice match {
    case AllowedChoiceValues.StandardDec =>
      model.locations.officeOfExit
        .map(OfficeOfExitStandard(_))
        .map(createExitOfficeFromStandardJourney)
        .foreach(declaration.setExitOffice)

    case AllowedChoiceValues.SupplementaryDec =>
      model.locations.officeOfExit
        .map(OfficeOfExitSupplementary(_))
        .map(createExitOfficeFromSupplementaryJourney)
        .foreach(declaration.setExitOffice)
  }

  private def createExitOfficeFromStandardJourney(data: OfficeOfExitStandard): Declaration.ExitOffice = {
    val officeIdentificationIDType = new ExitOfficeIdentificationIDType()
    officeIdentificationIDType.setValue(data.officeId)

    val exitOffice = new ExitOffice()
    exitOffice.setID(officeIdentificationIDType)
    exitOffice
  }

  private def createExitOfficeFromSupplementaryJourney(data: OfficeOfExitSupplementary): Declaration.ExitOffice = {
    val officeIdentificationIDType = new ExitOfficeIdentificationIDType()
    officeIdentificationIDType.setValue(data.officeId)

    val exitOffice = new ExitOffice()
    exitOffice.setID(officeIdentificationIDType)
    exitOffice
  }

}
