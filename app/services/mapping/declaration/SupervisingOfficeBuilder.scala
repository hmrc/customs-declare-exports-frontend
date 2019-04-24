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

import forms.declaration.WarehouseIdentification
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration.SupervisingOffice
import wco.datamodel.wco.declaration_ds.dms._2._

object SupervisingOfficeBuilder {

  def build(implicit cacheMap: CacheMap): SupervisingOffice =
    cacheMap
      .getEntry[WarehouseIdentification](WarehouseIdentification.formId)
      .map(createSupervisingOffice)
      .orNull

  private def createSupervisingOffice(data: WarehouseIdentification): SupervisingOffice = {
    val iDType = new SupervisingOfficeIdentificationIDType()
    iDType.setValue(data.supervisingCustomsOffice.orNull)

    val supervisingOffice = new SupervisingOffice()
    supervisingOffice.setID(iDType)
    supervisingOffice
  }
}
