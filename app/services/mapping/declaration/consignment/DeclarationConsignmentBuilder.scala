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

import forms.Choice.AllowedChoiceValues
import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.ModifyingBuilder
import services.mapping.goodsshipment.consignment.ConsignmentCarrierBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarationConsignmentBuilder @Inject()(
  freightBuilder: FreightBuilder,
  iteneraryBuilder: IteneraryBuilder,
  consignmentCarrierBuilder: ConsignmentCarrierBuilder
) extends ModifyingBuilder[ExportsCacheModel, Declaration] {
  override def buildThenAdd(model: ExportsCacheModel, declaration: Declaration): Unit =
    if (model.choice.equals(AllowedChoiceValues.StandardDec)) {
      val consignment = new Declaration.Consignment()
      freightBuilder.buildThenAdd(model, consignment)
      iteneraryBuilder.buildThenAdd(model, consignment)
      consignmentCarrierBuilder.buildThenAdd(model, consignment)
      declaration.setConsignment(consignment)
    }
}