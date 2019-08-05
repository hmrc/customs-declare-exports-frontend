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

package services.mapping.governmentagencygoodsitem
import javax.inject.Inject
import services.cache.ExportItem
import services.mapping.ModifyingBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.GovernmentProcedure
import wco.datamodel.wco.declaration_ds.dms._2.{GovernmentProcedureCurrentCodeType, GovernmentProcedurePreviousCodeType}

class GovernmentProcedureBuilder @Inject()()
    extends ModifyingBuilder[ExportItem, GoodsShipment.GovernmentAgencyGoodsItem] {

  override def buildThenAdd(
    exportItem: ExportItem,
    wcoGovernmentAgencyGoodsItem: GoodsShipment.GovernmentAgencyGoodsItem
  ): Unit =
    exportItem.procedureCodes.foreach { procedureCode =>
      {
        val code = procedureCode.toProcedureCode().extractProcedureCode()
        wcoGovernmentAgencyGoodsItem.getGovernmentProcedure.add(createGovernmentProcedure(code._1, code._2))
        procedureCode.additionalProcedureCodes.foreach { additionalProcedureCode =>
          wcoGovernmentAgencyGoodsItem.getGovernmentProcedure.add(
            createGovernmentProcedure(Some(additionalProcedureCode))
          )
        }
      }
    }

  private def createGovernmentProcedure(
    currentCode: Option[String] = None,
    previousCode: Option[String] = None
  ): GovernmentProcedure = {
    val governmentProcedure = new GovernmentProcedure

    currentCode.foreach { value =>
      val currentCodeType = new GovernmentProcedureCurrentCodeType
      currentCodeType.setValue(value)
      governmentProcedure.setCurrentCode(currentCodeType)
    }

    previousCode.foreach { value =>
      val previousCodeType = new GovernmentProcedurePreviousCodeType
      previousCodeType.setValue(value)
      governmentProcedure.setPreviousCode(previousCodeType)
    }

    governmentProcedure
  }
}
