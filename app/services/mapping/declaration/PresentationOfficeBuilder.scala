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
import forms.declaration.officeOfExit.{OfficeOfExitForms, OfficeOfExitStandard}
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.PresentationOffice
import wco.datamodel.wco.declaration_ds.dms._2._

object PresentationOfficeBuilder {

  def build(implicit cacheMap: CacheMap, choice: Choice): Declaration.PresentationOffice =
    choice match {
      case Choice(AllowedChoiceValues.StandardDec)      => buildPresentationOffice(choice)
      case Choice(AllowedChoiceValues.SupplementaryDec) => null
    }

  private def buildPresentationOffice(choice: Choice)(implicit cacheMap: CacheMap): Declaration.PresentationOffice =
    cacheMap
      .getEntry[OfficeOfExitStandard](OfficeOfExitForms.formId)
      .filter(_.presentationOfficeId.isDefined)
      .map(createPresentationOffice)
      .orNull

  private def createPresentationOffice(data: OfficeOfExitStandard): Declaration.PresentationOffice = {
    val presentationOfficeId = new PresentationOfficeIdentificationIDType()

    data.presentationOfficeId.foreach(presentationOfficeId.setValue(_))

    val presentationOffice = new PresentationOffice()
    presentationOffice.setID(presentationOfficeId)
    presentationOffice
  }
}
