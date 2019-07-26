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

import forms.Choice
import forms.Choice.AllowedChoiceValues
import forms.declaration.officeOfExit.OfficeOfExitStandard.AllowedCircumstancesCodeAnswers.yes
import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitStandard}
import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.ModifyingBuilder
import services.mapping.declaration.SpecificCircumstancesCodeBuilder.createCircumstancesCode
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.declaration_ds.dms._2._

class SpecificCircumstancesCodeBuilder @Inject()() extends ModifyingBuilder[Declaration] {
  override def buildThenAdd(model: ExportsCacheModel, declaration: Declaration): Unit =
    if (model.choice.equals(AllowedChoiceValues.StandardDec)) {
      model.locations.officeOfExit
        .filter(_.circumstancesCode.contains(yes))
        .map(_ => createCircumstancesCode)
        .foreach(declaration.setSpecificCircumstancesCodeCode)
    }
}

object SpecificCircumstancesCodeBuilder {

  def build(implicit cacheMap: CacheMap, choice: Choice): DeclarationSpecificCircumstancesCodeCodeType =
    choice match {
      case Choice(AllowedChoiceValues.StandardDec)      => buildCircumstancesCode(choice)
      case Choice(AllowedChoiceValues.SupplementaryDec) => null
    }

  private def buildCircumstancesCode(
    choice: Choice
  )(implicit cacheMap: CacheMap): DeclarationSpecificCircumstancesCodeCodeType =
    cacheMap
      .getEntry[OfficeOfExitStandard](OfficeOfExitForms.formId)
      .filter(data => data.circumstancesCode == yes)
      .map(_ => createCircumstancesCode)
      .orNull

  private def createCircumstancesCode: DeclarationSpecificCircumstancesCodeCodeType = {
    val circumstancesCode = new DeclarationSpecificCircumstancesCodeCodeType()
    circumstancesCode.setValue("A20")
    circumstancesCode
  }
}
