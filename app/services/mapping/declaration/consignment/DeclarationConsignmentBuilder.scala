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

package services.mapping.declaration.consignment

import forms.Choice
import forms.Choice.AllowedChoiceValues
import services.cache.ExportsCacheModel
import services.mapping.declaration.consignment.{FreightBuilder, IteneraryBuilder}
import services.mapping.goodsshipment.consignment.ConsignmentCarrierBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

object DeclarationConsignmentBuilder {
  def buildThenAdd(model: ExportsCacheModel, declaration: Declaration): Unit = {
    if(model.choice.equals(AllowedChoiceValues.StandardDec)) {
      val consignment = new Declaration.Consignment()
      FreightBuilder.buildThenAdd(model, consignment)
      //IteneraryBuilder.buildThenAdd(model, consignment)
      //ConsignmentCarrierBuilder.buildThenAdd(model, consignment)
      declaration.setConsignment(consignment)
    }
  }

  def build(implicit cacheMap: CacheMap, choice: Choice): Declaration.Consignment =
    choice match {
      case Choice(AllowedChoiceValues.StandardDec)      => buildCircumstancesCode
      case Choice(AllowedChoiceValues.SupplementaryDec) => null
    }

  private def buildCircumstancesCode(implicit cacheMap: CacheMap, choice: Choice): Declaration.Consignment = {
    val consignment = new Declaration.Consignment()

    consignment.setFreight(FreightBuilder.build)

    val iteneraries = IteneraryBuilder.build
    if (!iteneraries.isEmpty) {
      consignment.getItinerary.addAll(iteneraries)
    }

    consignment.setCarrier(ConsignmentCarrierBuilder.build)

    consignment
  }

}
