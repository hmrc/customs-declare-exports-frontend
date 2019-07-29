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

package services.cache

import java.time.LocalDateTime

import forms.ChoiceSpec.supplementaryChoice
import forms.Ducr
import forms.declaration.{ConsignmentReferences, DispatchLocation}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import models.declaration.Parties

trait CacheTestData {

  val ducr = "5GB123456789000-123ABC456DEFIIIII"
  val cachedLRN = "FG7676767889"
  val dispatchLocation = "GB"

  val itemIdService = new ExportItemIdGeneratorService()

  def createEmptyExportsModel =
    ExportsCacheModel("sessionId", "draftId", LocalDateTime.now(), LocalDateTime.now(), supplementaryChoice.value)

  def createAdditionalDeclarationType(decType: String): AdditionalDeclarationType = AdditionalDeclarationType(decType)

  def createConsignmentReferencesData(maybeDucr: Option[String], lrn: String): ConsignmentReferences = {
    val ducr: Option[Ducr] = maybeDucr.fold(Option.empty[Ducr]) { value =>
      Some(Ducr(value))
    }
    ConsignmentReferences(ducr, lrn)
  }

  def createDispatchLocation(dispatchLocation: String) =
    DispatchLocation(dispatchLocation)

  def createExportItem(): ExportItem =
    ExportItem(id = itemIdService.generateItemId())

  def createEmptyParties(): Parties =
    Parties()
}
