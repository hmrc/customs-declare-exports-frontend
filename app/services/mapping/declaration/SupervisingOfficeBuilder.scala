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

import javax.inject.Inject
import models.ExportsCacheModel
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.SupervisingOffice
import wco.datamodel.wco.declaration_ds.dms._2._

class SupervisingOfficeBuilder @Inject()() extends ModifyingBuilder[ExportsCacheModel, Declaration] {

  override def buildThenAdd(model: ExportsCacheModel, declaration: Declaration): Unit =
    model.locations.warehouseIdentification
      .flatMap(_.supervisingCustomsOffice)
      .map(createSupervisingOffice)
      .foreach(declaration.setSupervisingOffice)

  private def createSupervisingOffice(data: String): SupervisingOffice = {
    val supervisingOffice = new SupervisingOffice()

    val iDType = new SupervisingOfficeIdentificationIDType()
    iDType.setValue(data)
    supervisingOffice.setID(iDType)

    supervisingOffice
  }
}
