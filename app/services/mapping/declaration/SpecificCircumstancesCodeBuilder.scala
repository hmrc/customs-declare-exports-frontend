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
import forms.declaration.officeOfExit.{OfficeOfExit, OfficeOfExitStandard}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.declaration_ds.dms._2._

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
      .getEntry[OfficeOfExitStandard](OfficeOfExit.formId)
      .filter(data => data.circumstancesCode == yes)
      .map(createCircumstancesCode)
      .orNull

  private def createCircumstancesCode(data: OfficeOfExitStandard): DeclarationSpecificCircumstancesCodeCodeType = {
    val circumstancesCode = new DeclarationSpecificCircumstancesCodeCodeType()
    circumstancesCode.setValue("A20")
    circumstancesCode
  }
}
