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
import models.declaration.ProcedureCodesData
import uk.gov.hmrc.http.cache.client.CacheMap

import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment.GovernmentAgencyGoodsItem.GovernmentProcedure
import wco.datamodel.wco.declaration_ds.dms._2.{GovernmentProcedureCurrentCodeType, GovernmentProcedurePreviousCodeType}

object ProcedureCodesBuilder {

  def build(implicit cacheMap: CacheMap): Option[Seq[GovernmentProcedure]] = {
    cacheMap
        .getEntry[ProcedureCodesData](ProcedureCodesData.formId)
        .map(
          form =>
            Seq(createGovernmentProcedure(form.procedureCode.map(_.substring(0, 2)), form.procedureCode.map(_.substring(2, 4))))
              ++ form.additionalProcedureCodes.map(code => createGovernmentProcedure(Some(code)))
        )
  }

  private def createGovernmentProcedure(currentCode : Option[String] = None, previousCode: Option[String] = None): GovernmentProcedure ={
    val governmentProcedure = new GovernmentProcedure

    val currentCodeType = new GovernmentProcedureCurrentCodeType
    currentCodeType.setValue(currentCode.orNull)

    val previousCodeType = new GovernmentProcedurePreviousCodeType
    previousCodeType.setValue(previousCode.orNull)

    governmentProcedure.setCurrentCode(currentCodeType)
    governmentProcedure.setPreviousCode(previousCodeType)

    governmentProcedure
  }
}
