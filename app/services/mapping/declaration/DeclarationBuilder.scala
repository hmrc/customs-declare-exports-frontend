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
import services.mapping.AuthorisationHoldersBuilder
import services.mapping.declaration.consignment.DeclarationConsignmentBuilder
import services.mapping.goodsshipment.GoodsShipmentBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

object DeclarationBuilder {

  def build(implicit cacheMap: CacheMap, choice: Choice): Declaration = {
    val declaration = new Declaration()

    declaration.setFunctionCode(FunctionCodeBuilder.build("9"))
    declaration.setFunctionalReferenceID(FunctionalReferenceIdBuilder.build)
    declaration.setTypeCode(TypeCodeBuilder.build)
    declaration.setGoodsItemQuantity(GoodsItemQuantityBuilder.build)
//    declaration.setGoodsShipment(GoodsShipmentBuilder.build)
    declaration.setExitOffice(ExitOfficeBuilder.build)
    declaration.setBorderTransportMeans(BorderTransportMeansBuilder.build)
    declaration.setExporter(ExporterBuilder.build)
    declaration.setDeclarant(DeclarantBuilder.build)
    declaration.setInvoiceAmount(InvoiceAmountBuilder.build)
    declaration.setPresentationOffice(PresentationOfficeBuilder.build)
    declaration.setSpecificCircumstancesCodeCode(SpecificCircumstancesCodeBuilder.build)
    declaration.setSupervisingOffice(SupervisingOfficeBuilder.build)
    declaration.setTotalPackageQuantity(TotalPackageQuantityBuilder.build)
//    declaration.setConsignment(DeclarationConsignmentBuilder.build)
    declaration.setTypeCode(TypeCodeBuilder.build)

    val authorisationHolders = AuthorisationHoldersBuilder.build
    if (authorisationHolders != null && !authorisationHolders.isEmpty) {
      declaration.getAuthorisationHolder.addAll(authorisationHolders)
    }

    val currencyExchangeList = CurrencyExchangeBuilder.build
    if (currencyExchangeList != null && !currencyExchangeList.isEmpty) {
      declaration.getCurrencyExchange.addAll(currencyExchangeList)
    }

    declaration
  }

  def buildCancelationRequest(
    functionalReferenceId: String,
    declarationId: String,
    statementDescription: String,
    changeReason: String,
    eori: String
  ): Declaration = {
    val declaration = new Declaration()

    declaration.setFunctionCode(FunctionCodeBuilder.build("13"))
    declaration.setTypeCode(TypeCodeBuilder.build("INV"))
    declaration.setFunctionalReferenceID(FunctionalReferenceIdBuilder.build(functionalReferenceId))
    declaration.setID(IdentificationBuilder.build(declarationId))
    declaration.setSubmitter(SubmitterBuilder.build(eori))
    declaration.getAmendment.add(AmendmentBuilder.build(changeReason))
    declaration.getAdditionalInformation.add(AdditionalInformationBuilder.build(statementDescription))

    declaration
  }
}
