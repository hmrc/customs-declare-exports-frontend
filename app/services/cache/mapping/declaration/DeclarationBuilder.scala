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

package services.cache.mapping.declaration

import javax.inject.Inject
import services.cache.ExportsCacheModel
import services.mapping.AuthorisationHoldersBuilder
import services.mapping.declaration._
import services.mapping.declaration.consignment.DeclarationConsignmentBuilder
import services.mapping.goodsshipment.GoodsShipmentBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarationBuilder @Inject()(
  functionCodeBuilder: FunctionCodeBuilder,
  functionalReferenceIdBuilder: FunctionalReferenceIdBuilder,
  typeCodeBuilder: TypeCodeBuilder,
  goodsItemQuantityBuilder: GoodsItemQuantityBuilder,
  agentBuilder: AgentBuilder,
  presentationOfficeBuilder: PresentationOfficeBuilder,
  specificCircumstancesCodeBuilder: SpecificCircumstancesCodeBuilder,
  exitOfficeBuilder: ExitOfficeBuilder,
  borderTransportMeansBuilder: BorderTransportMeansBuilder,
  exporterBuilder: ExporterBuilder,
  declarantBuilder: DeclarantBuilder,
  invoiceAmountBuilder: InvoiceAmountBuilder,
  supervisingOfficeBuilder: SupervisingOfficeBuilder,
  totalPackageQuantityBuilder: TotalPackageQuantityBuilder,
  declarationConsignmentBuilder: DeclarationConsignmentBuilder,
  authorisationHoldersBuilder: AuthorisationHoldersBuilder,
  currencyExchangeBuilder: CurrencyExchangeBuilder,
  goodsShipmentBuilder: GoodsShipmentBuilder
) {

  def build(model: ExportsCacheModel): Declaration = {
    val declaration = new Declaration()
    functionCodeBuilder.buildThenAdd(model, declaration)
    functionalReferenceIdBuilder.buildThenAdd(model, declaration)
    typeCodeBuilder.buildThenAdd(model, declaration)
    goodsItemQuantityBuilder.buildThenAdd(model, declaration)
    agentBuilder.buildThenAdd(model, declaration)

    goodsShipmentBuilder.buildThenAdd(model, declaration)

    exitOfficeBuilder.buildThenAdd(model, declaration)
    borderTransportMeansBuilder.buildThenAdd(model, declaration)
    exporterBuilder.buildThenAdd(model, declaration)
    declarantBuilder.buildThenAdd(model, declaration)
    invoiceAmountBuilder.buildThenAdd(model, declaration)
    presentationOfficeBuilder.buildThenAdd(model, declaration)
    specificCircumstancesCodeBuilder.buildThenAdd(model, declaration)
    supervisingOfficeBuilder.buildThenAdd(model, declaration)
    totalPackageQuantityBuilder.buildThenAdd(model, declaration)
    declarationConsignmentBuilder.buildThenAdd(model, declaration)
    authorisationHoldersBuilder.buildThenAdd(model, declaration)
    currencyExchangeBuilder.buildThenAdd(model, declaration)
    declaration
  }
}
