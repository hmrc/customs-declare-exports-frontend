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

import forms.declaration.RepresentativeDetails
import services.cache.ExportsCacheModel
import services.mapping.declaration._
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.declaration_ds.dms._2.{DeclarationFunctionalReferenceIDType, DeclarationTypeCodeType}

object DeclarationBuilder {

  val defaultFunctionCode = "9"

  def build(exportsCacheModel: ExportsCacheModel): Declaration = {
    val declaration = new Declaration()

    declaration.setFunctionCode(FunctionCodeBuilder.build(defaultFunctionCode))
    declaration.setFunctionalReferenceID(createDeclarationFunctionalReferenceIDType(exportsCacheModel))

    val typeCode: Option[DeclarationTypeCodeType] = exportsCacheModel.additionalDeclarationType.map(
      additionalDeclarationType =>
        TypeCodeBuilder.createTypeCode(additionalDeclarationType, exportsCacheModel.dispatchLocation)
    )

    declaration.setTypeCode(typeCode.getOrElse(new DeclarationTypeCodeType()))
    declaration.setGoodsItemQuantity(GoodsItemQuantityBuilder.createGoodsItemQuantity(exportsCacheModel.items.toSeq))

    generateAgent(exportsCacheModel).foreach { data =>
      declaration.setAgent(data)
    }

    //    declaration.setGoodsShipment(GoodsShipmentBuilder.build)
    //    declaration.setExitOffice(ExitOfficeBuilder.build)
    //    declaration.setBorderTransportMeans(BorderTransportMeansBuilder.build)
    //    declaration.setExporter(ExporterBuilder.build)
    //    declaration.setDeclarant(DeclarantBuilder.build)
    //    declaration.setInvoiceAmount(InvoiceAmountBuilder.build)
    //    declaration.setPresentationOffice(PresentationOfficeBuilder.build)
    //    declaration.setSpecificCircumstancesCodeCode(SpecificCircumstancesCodeBuilder.build)
    //    declaration.setSupervisingOffice(SupervisingOfficeBuilder.build)
    //    declaration.setTotalPackageQuantity(TotalPackageQuantityBuilder.build)
    //    declaration.setConsignment(DeclarationConsignmentBuilder.build)
    //    declaration.setTypeCode(TypeCodeBuilder.build)
    //
    //    val authorisationHolders = AuthorisationHoldersBuilder.build
    //    if (authorisationHolders != null && !authorisationHolders.isEmpty) {
    //      declaration.getAuthorisationHolder.addAll(authorisationHolders)
    //    }
    //
    //    val currencyExchangeList = CurrencyExchangeBuilder.build
    //    if (currencyExchangeList != null && !currencyExchangeList.isEmpty) {
    //      declaration.getCurrencyExchange.addAll(currencyExchangeList)
    //    }

    declaration
  }

  private def createDeclarationFunctionalReferenceIDType(
    exportsCacheModel: ExportsCacheModel
  ): DeclarationFunctionalReferenceIDType = {
    val returnVal = new DeclarationFunctionalReferenceIDType()
    exportsCacheModel.consignmentReferences.map(
      consignmentReferences => if (consignmentReferences.lrn.nonEmpty) returnVal.setValue(consignmentReferences.lrn)
    )
    returnVal
  }

  private def generateAgent(exportsCacheModel: ExportsCacheModel): Option[Declaration.Agent] =
    exportsCacheModel.parties.representativeDetails.map { representativeDetails =>
      if (RepresentativeDetails.isDefined(representativeDetails)) {
        AgentBuilder.createAgent(representativeDetails)
      } else {
        new Declaration.Agent()
      }
    }
}
