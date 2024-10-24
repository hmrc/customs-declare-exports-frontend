/*
 * Copyright 2024 HM Revenue & Customs
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

package views.helpers

import base.Injector
import connectors.CodeListConnector
import forms.common.YesNoAnswer.{Yes, YesNoAnswers}
import forms.common.{Address, Country, Date, Eori}
import forms.section2.AdditionalActor
import forms.section2.authorisationHolder.AuthorizationTypeCodes.CSE
import forms.section3.LocationOfGoods
import forms.section4.NatureOfTransaction.BusinessPurchase
import forms.section4.{Document, InvoiceAndExchangeRate}
import forms.section5.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import forms.section5._
import forms.section6.InlandOrBorder.Border
import forms.section6.ModeOfTransportCode.Maritime
import forms.section6.TransportPayment.cash
import forms.section6._
import models.codes.{Country => ModelCountry}
import models.declaration.{AdditionalDocuments, CommodityMeasure, Container}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.Assertion
import services.DocumentType
import services.cache.ExportsTestHelper
import services.model.PackageType
import views.common.UnitViewSpec
import scala.collection.immutable.ListMap

class PointerRecordSpec extends UnitViewSpec with ExportsTestHelper with Injector {

  import PointerRecordSpec._

  implicit val countryHelper: CountryHelper = mock[CountryHelper]
  when(countryHelper.getShortNameForCountryCode(meq(countryGB.countryCode))(any())).thenReturn(Some(countryGB.countryName))
  when(countryHelper.getShortNameForCountryCode(meq(countryIT.countryCode))(any())).thenReturn(Some(countryIT.countryName))

  implicit val codeListConnector: CodeListConnector = mock[CodeListConnector]
  when(codeListConnector.getCountryCodes(any())).thenReturn(ListMap(countryGB.countryCode -> countryGB, countryIT.countryCode -> countryIT))
  when(codeListConnector.getPackageTypes(any())).thenReturn(ListMap(typeOfPackage.code -> typeOfPackage))
  when(codeListConnector.getDocumentTypes(any())).thenReturn(ListMap(previousDocType.code -> previousDocType))

  // scalastyle:off
  "PointerRecord" can {
    "find a record for the following pointers" in {
      validatePointerValues("declaration.typeCode", "STANDARD", "Standard declaration")
      validatePointerValues("declaration.goodsItemQuantity", "1", "1")
      validatePointerValues("declaration.items.$.statisticalValue.statisticalValue", statisticalValue, 0)
      validatePointerValues("declaration.items.$.additionalDocument", None, None)
      validatePointerValues("declaration.items.$.additionalDocument.documentTypeCode", "1", 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.documentTypeCode", documentTypeCode, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentTypeCode", documentTypeCode, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.documentIdentifier", documentIdentifier, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentIdentifier", documentIdentifier, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.dateOfValidity", formDate.toDisplayFormat, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.dateOfValidity", formDate.toDisplayFormat, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.documentStatus", documentStatus, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentStatus", documentStatus, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.documentStatusReason", documentStatusReason, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentStatusReason", documentStatusReason, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.issuingAuthorityName", issuingAuthorityName, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.issuingAuthorityName", issuingAuthorityName, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentWriteOff.measurementUnit", measurementUnit, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentWriteOff", measurementUnit, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.documents.$.documentWriteOff.measurementUnit", measurementUnit, 0, 0)
      validatePointerValues("declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity", documentQuantity.toString(), 0, 0)
      validatePointerValues("declaration.items.$.additionalInformation.$.code", additionalInformationCode, 0, 0)
      validatePointerValues("declaration.items.$.additionalInformation.items.$.code", additionalInformationCode, 0, 0)
      validatePointerValues("declaration.items.$.additionalInformation.$.code", additionalInformationCode, 0, 0)
      validatePointerValues("declaration.items.$.additionalInformation.items.$.description", additionalInformationDescription, 0, 0)
      validatePointerValues("declaration.items.$.additionalInformation.$.description", additionalInformationDescription, 0, 0)
      validatePointerValues("declaration.items.$.commodityDetails", combinedNomenclatureCode, 0, 0)
      validatePointerValues("declaration.items.$.commodityDetails.combinedNomenclatureCode", combinedNomenclatureCode, 0, 0)
      validatePointerValues("declaration.items.$.commodityDetails.descriptionOfGoods", descriptionOfGoods, 0, 0)
      validatePointerValues("declaration.items.$.cusCode.id", cusCode, 0)
      validatePointerValues("declaration.items.$.cusCode.cusCode", cusCode, 0)
      validatePointerValues("declaration.items.$.dangerousGoodsCode.dangerousGoodsCode", dangerousGoodsCode, 0)
      validatePointerValues("declaration.items.$.commodityMeasure.grossMass", grossMass, 0)
      validatePointerValues("declaration.items.$.commodityMeasure.netMass", netMass, 0)
      validatePointerValues("declaration.items.$.commodityMeasure.supplementaryUnits", supplementaryUnits, 0)
      validatePointerValues("declaration.items.$.additionalFiscalReferences.$.id", countryGB.countryCode, countryGB.countryName, 0, 0)
      validatePointerValues("declaration.items.$.additionalFiscalReferences.$.roleCode", roleCode, 0, 0)
      validatePointerValues("declaration.items.$.procedureCodes.procedureCode.current", procedureCode, 0)
      validatePointerValues("declaration.items.$.procedureCodes.procedureCode.previous", procedureCode, 0)
      validatePointerValues("declaration.items.$.procedureCodes.procedure.code", procedureCode, 0)
      validatePointerValues("declaration.items.$.procedureCodes.additionalProcedureCodes", additionalProcedureCodes, 0)
      validatePointerValues("declaration.items.$.procedureCodes.additionalProcedureCodes.$", additionalProcedureCode, 0, 0)
      validatePointerValues("declaration.items.$.procedureCodes.additionalPcs.$", additionalProcedureCode, 0, 0)
      validatePointerValues("declaration.items.$.packaging", None, None)
      validatePointerValues("declaration.items.$.packageInformation.$.shippingMarks", shippingMarks, 0, 0)
      validatePointerValues("declaration.items.$.packageInformation.$.numberOfPackages", numberOfPackages.toString, 0, 0)
      validatePointerValues("declaration.items.$.packageInformation.$.typesOfPackages", typeOfPackage.code, typeOfPackage.asText, 0, 0)
      validatePointerValues("declaration.items.size", "1")
      validatePointerValues("declaration.items.$", "")
      validatePointerValues("declaration.consignmentReferences.lrn", lrn)
      validatePointerValues("declaration.consignmentReferences.ucr", ducr)
      validatePointerValues("declaration.totalNumberOfItems.totalAmountInvoiced", totalAmountInvoiced)
      validatePointerValues("declaration.totalNumberOfItems.totalAmountInvoicedCurrency", totalAmountInvoicedCurrency)
      validatePointerValues("declaration.totalNumberOfItems.totalPackage", totalPackageQuantity)
      validatePointerValues("declaration.parties.representativeDetails", None, None)
      validatePointerValues("declaration.parties.representativeDetails.details.eori", representativeDetailsEori)
      validatePointerValues(
        "declaration.parties.representativeDetails.statusCode",
        representativeDetailsStatusCode,
        messages("declaration.summary.parties.representative.type.1")
      )
      validatePointerValues("declaration.parties.declarationHolders.$.eori", authorisationHolderEori, 0)
      validatePointerValues("declaration.parties.declarationHolders.holders.$.eori", authorisationHolderEori, 0)
      validatePointerValues("declaration.parties.declarationHolders.$.authorisationTypeCode", CSE, 0)
      validatePointerValues("declaration.parties.declarationHolders.holders.$.authorisationTypeCode", CSE, 0)
      validatePointerValues("declaration.parties.declarationHolders.authorisationTypeCode", "1")
      validatePointerValues("declaration.transport.meansOfTransportCrossingTheBorderIDNumber", meansOfTransportCrossingTheBorderIDNumber)
      validatePointerValues(
        "declaration.transport.meansOfTransportCrossingTheBorderType",
        meansOfTransportCrossingTheBorderType,
        meansOfTransportCrossingTheBorderTypeName
      )
      validatePointerValues("declaration.transport.meansOfTransportCrossingTheBorderNationality", countryIT.countryCode, countryIT.countryName)
      validatePointerValues("declaration.transport.transportCrossingTheBorderNationality.countryCode", countryIT.countryCode, countryIT.countryName)
      validatePointerValues(
        "declaration.transport.borderModeOfTransportCode.code",
        Maritime.toString,
        messages("declaration.summary.transport.inlandModeOfTransport.Maritime")
      )
      validatePointerValues(
        "declaration.borderTransport.modeCode",
        Maritime.toString,
        messages("declaration.summary.transport.inlandModeOfTransport.Maritime")
      )
      validatePointerValues("declaration.parties.carrierDetails.details.eori", detailsEori)
      validatePointerValues(
        "declaration.parties.carrierDetails.details.address",
        Seq(fullName, addressLine, postCode, countryGB.countryCode).mkString(", "),
        Seq(fullName, addressLine, postCode, countryGB.countryName).mkString("<br/>")
      )
      validatePointerValues("declaration.parties.carrierDetails.details.address.fullName", fullName)
      validatePointerValues("declaration.parties.carrierDetails.details.address.addressLine", addressLine)
      validatePointerValues("declaration.parties.carrierDetails.details.address.townOrCity", townOrCity)
      validatePointerValues("declaration.parties.carrierDetails.details.address.postCode", postCode)
      validatePointerValues("declaration.parties.carrierDetails.details.address.country", countryGB.countryCode, countryGB.countryName)
      validatePointerValues("declaration.transport.transportPayment.paymentMethod", cash, messages("declaration.summary.transport.payment.A"))
      validatePointerValues("declaration.transport.expressConsignment", YesNoAnswers.yes)
      validatePointerValues("declaration.locations.destinationCountries.countriesOfRouting", "1", 0)
      validatePointerValues("declaration.locations.destinationCountries.countriesOfRouting.$", countryIT.countryCode, countryIT.countryName, 0)
      validatePointerValues("declaration.locations.destinationCountries.countryOfDestination", countryIT.countryCode, countryIT.countryName, 0)
      validatePointerValues("declaration.totalNumberOfItems.exchangeRate", exchangeRate)
      validatePointerValues("declaration.declarantDetails.details.eori", detailsEori)
      validatePointerValues("declaration.locations.officeOfExit.circumstancesCode", officeOfExit)
      validatePointerValues("declaration.locations.officeOfExit.officeId", officeOfExit)
      validatePointerValues("declaration.parties.personPresentingGoodsDetails", YesNoAnswers.yes)
      validatePointerValues("declaration.parties.personPresentingGoodsDetails.eori", detailsEori)
      validatePointerValues("declaration.parties.exporterDetails", None, None)
      validatePointerValues("declaration.parties.exporterDetails.details.eori", detailsEori)
      validatePointerValues("declaration.parties.exporterDetails.details.address.fullName", fullName)
      validatePointerValues("declaration.parties.exporterDetails.details.address.addressLine", addressLine)
      validatePointerValues("declaration.parties.exporterDetails.details.address.townOrCity", townOrCity)
      validatePointerValues("declaration.parties.exporterDetails.details.address.postCode", postCode)
      validatePointerValues("declaration.parties.exporterDetails.details.address.country", countryGB.countryCode, countryGB.countryName)
      validatePointerValues(
        "declaration.natureOfTransaction.natureType",
        BusinessPurchase,
        messages(s"declaration.summary.transaction.natureOfTransaction.$BusinessPurchase")
      )
      validatePointerValues("declaration.parties.additionalActors", "1")
      validatePointerValues("declaration.parties.additionalActors.actors.$", "1", 0)
      validatePointerValues("declaration.parties.additionalActors.actors.$.eori", additionalActorsEori, 0)
      validatePointerValues("declaration.parties.additionalActors.actors.$.type", additionalActorsType, 0)
      validatePointerValues("declaration.parties.consigneeDetails.details.address.fullName", fullName)
      validatePointerValues("declaration.parties.consigneeDetails.details.address.addressLine", addressLine)
      validatePointerValues("declaration.parties.consigneeDetails.details.address.townOrCity", townOrCity)
      validatePointerValues("declaration.parties.consigneeDetails.details.address.postCode", postCode)
      validatePointerValues("declaration.parties.consigneeDetails.details.address.country", countryGB.countryCode, countryGB.countryName)
      validatePointerValues("declaration.parties.consignorDetails.eori", detailsEori)
      validatePointerValues("declaration.parties.consignorDetails.details.address.fullName", fullName)
      validatePointerValues("declaration.parties.consignorDetails.details.address.addressLine", addressLine)
      validatePointerValues("declaration.parties.consignorDetails.details.address.townOrCity", townOrCity)
      validatePointerValues("declaration.parties.consignorDetails.details.address.postCode", postCode)
      validatePointerValues("declaration.parties.consignorDetails.details.address.country", countryGB.countryCode, countryGB.countryName)
      validatePointerValues("declaration.transport.meansOfTransportOnDepartureIDNumber", meansOfTransportOnDepartureIDNumber)
      validatePointerValues("declaration.departureTransport", None, None)
      validatePointerValues("declaration.departureTransport.meansOfTransportOnDepartureIDNumber", meansOfTransportOnDepartureIDNumber)
      validatePointerValues(
        "declaration.locations.inlandModeOfTransportCode.inlandModeOfTransportCode",
        Maritime.value,
        messages("declaration.summary.transport.inlandModeOfTransport.Maritime")
      )
      validatePointerValues(
        "declaration.departureTransport.borderModeOfTransportCode",
        Maritime.value,
        messages("declaration.summary.transport.inlandModeOfTransport.Maritime")
      )
      validatePointerValues(
        "declaration.departureTransport.meansOfTransportOnDepartureType",
        meansOfTransportOnDepartureType,
        messages("declaration.summary.transport.border.meansOfTransport.10")
      )
      validatePointerValues("declaration.locations.goodsLocation.nameOfLocation", goodsLocation)
      validatePointerValues("declaration.locations.goodsLocation.identificationOfLocation", goodsLocation)
      validatePointerValues("declaration.locations.goodsLocation.qualifierOfIdentification", goodsLocation)
      validatePointerValues("declaration.locations.goodsLocation.typeOfLocation", goodsLocation)
      validatePointerValues("declaration.transport.containers.$.id", containerId, 0)
      validatePointerValues("declaration.containers.container.$.id", containerId, 0)
      validatePointerValues("declaration.transport.containers.$.seals.seal.$.id", sealName, 0, 0)
      validatePointerValues("declaration.containers.container.$.seals.seal.$.id", sealName, 0, 0)
      validatePointerValues("declaration.previousDocuments.$.documentCategory", None, None)
      validatePointerValues("declaration.previousDocuments.documents.$.documentCategory", None, None)
      validatePointerValues("declaration.previousDocuments.$.documentReference", previousDocReference, 0)
      validatePointerValues("declaration.previousDocuments.documents.$.documentReference", previousDocReference, 0)
      validatePointerValues("declaration.previousDocuments.$.documentType", previousDocType.code, previousDocType.asText, 0)
      validatePointerValues("declaration.previousDocuments.documents.$.documentType", previousDocType.code, previousDocType.asText, 0)
      validatePointerValues("declaration.previousDocuments.$.goodsItemIdentifier", goodsItemIdentifier, 0)
      validatePointerValues("declaration.previousDocuments.documents.$.goodsItemIdentifier", goodsItemIdentifier, 0)
      validatePointerValues("declaration.locations.warehouseIdentification", None, None)
      validatePointerValues("declaration.locations.warehouseIdentification.identificationNumber", warehouseIdentificationId)
      validatePointerValues("declaration.locations.warehouseIdentification.identificationType", warehouseIdentificationId)
      validatePointerValues("declaration.locations.warehouseIdentification.supervisingCustomsOffice", supervisingCustomsOffice)
    }
    // scalastyle:on
  }

  val item = anItem(
    withItemId("itemId"),
    withSequenceId(1),
    withProcedureCodes(Some(procedureCode), Seq("000", "111")),
    withFiscalInformation(),
    withAdditionalFiscalReferenceData(AdditionalFiscalReferencesData(Seq(AdditionalFiscalReference(countryGB.countryCode, roleCode)))),
    withStatisticalValue(statisticalValue),
    withCommodityDetails(CommodityDetails(Some(combinedNomenclatureCode), Some(descriptionOfGoods))),
    withUNDangerousGoodsCode(UNDangerousGoodsCode(Some(dangerousGoodsCode))),
    withCUSCode(CusCode(Some(cusCode))),
    withNactCodes(NactCode("111"), NactCode("222")),
    withNactExemptionCode(NactCode("VATE")),
    withPackageInformation(typeOfPackage.code, numberOfPackages, shippingMarks),
    withCommodityMeasure(CommodityMeasure(Some(supplementaryUnits), Some(true), Some(grossMass), Some(netMass))),
    withAdditionalInformation(additionalInformationCode, additionalInformationDescription),
    withIsLicenseRequired(),
    withAdditionalDocuments(Yes, withAdditionalDocument(documentTypeCode, documentIdentifier)),
    withAdditionalDocuments(
      AdditionalDocuments(
        Yes,
        Seq(
          AdditionalDocument(
            Some(documentTypeCode),
            Some(documentIdentifier),
            Some(documentStatus),
            Some(documentStatusReason),
            Some(issuingAuthorityName),
            Some(formDate),
            Some(DocumentWriteOff(Some(measurementUnit), Some(documentQuantity)))
          )
        )
      )
    )
  )

  val fullDeclaration = aDeclaration(
    withTotalNumberOfItems(
      InvoiceAndExchangeRate(
        totalAmountInvoiced = Some(totalAmountInvoiced),
        totalAmountInvoicedCurrency = Some(totalAmountInvoicedCurrency),
        agreedExchangeRate = YesNoAnswers.yes,
        exchangeRate = Some(exchangeRate)
      )
    ),
    withTotalPackageQuantity(totalPackageQuantity),
    withAdditionalDeclarationType(),
    withAuthorisationHolders(Some(CSE), Some(Eori(authorisationHolderEori))),
    withGoodsLocation(LocationOfGoods(goodsLocation)),
    withOfficeOfExit(officeOfExit),
    withDestinationCountry(Country(Some(countryIT.countryCode))),
    withRoutingCountries(Seq(Country(Some(countryIT.countryCode)))),
    withItem(item),
    withEntryIntoDeclarantsRecords(YesNoAnswers.yes),
    withDeclarantIsExporter(),
    withDeclarantDetails(eori = Some(Eori(detailsEori)), Some(address)),
    withPersonPresentingGoods(Some(Eori(detailsEori))),
    withExporterDetails(eori = Some(Eori(detailsEori)), Some(address)),
    withDepartureTransport(Maritime, meansOfTransportOnDepartureType, meansOfTransportOnDepartureIDNumber),
    withTransportLeavingTheBorder(Some(Maritime)),
    withConsignmentReferences(ducr, lrn),
    withLinkDucrToMucr(),
    withMucr(),
    withRepresentativeDetails(Some(Eori(representativeDetailsEori)), Some(representativeDetailsStatusCode), Some("Yes")),
    withConsigneeDetails(Some(Eori(detailsEori)), Some(address)),
    withConsignorDetails(Some(Eori(detailsEori)), Some(address)),
    withCarrierDetails(Some(Eori(detailsEori)), Some(address)),
    withAdditionalActors(AdditionalActor(Some(Eori(additionalActorsEori)), Some(additionalActorsType))),
    withPreviousDocuments(Document(previousDocType.code, previousDocReference, Some(goodsItemIdentifier))),
    withNatureOfTransaction(BusinessPurchase),
    withBorderTransport(meansOfTransportCrossingTheBorderType, meansOfTransportCrossingTheBorderIDNumber),
    withTransportCountry(Some(countryIT.countryCode)),
    withWarehouseIdentification(Some(WarehouseIdentification(Some(warehouseIdentificationId)))),
    withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some(supervisingCustomsOffice)))),
    withInlandOrBorder(Some(Border)),
    withInlandModeOfTransportCode(ModeOfTransportCode.Maritime),
    withOfficeOfExit(officeOfExit),
    withTransportPayment(Some(TransportPayment(cash))),
    withContainerData(Container(1, containerId, Seq(Seal(1, sealName)))),
    withIsExs()
  )

  def validatePointerValues(pointer: String, rawValue: String, args: Int*): Assertion =
    validatePointerValues(pointer, if (rawValue.length < 1) None else Some(rawValue), Option.empty[String], args: _*)

  def validatePointerValues(pointer: String, rawValue: String, readableValue: String, args: Int*): Assertion =
    validatePointerValues(pointer, Some(rawValue), Some(readableValue), args: _*)

  def validatePointerValues(pointer: String, rawValue: Option[String], readableValue: Option[String], args: Int*): Assertion =
    withClue(s"pointer is '$pointer'") {
      val maybePointerRecord = PointerRecord.pointersToPointerRecords.get(pointer)
      val displayValue = if (readableValue.isDefined) readableValue else rawValue

      maybePointerRecord.isDefined mustBe true
      maybePointerRecord.get.fetchRawValue(fullDeclaration, args: _*) mustBe rawValue
      maybePointerRecord.get.fetchReadableValue(fullDeclaration, args: _*) mustBe displayValue
    }
}

object PointerRecordSpec {
  val statisticalValue = "123"
  val documentTypeCode = "C501"
  val documentIdentifier = "GBAEOC1342"
  val formDate = Date(Some(29), Some(2), Some(2024))
  val documentStatus = "documentStatus"
  val documentStatusReason = "documentStatusReason"
  val issuingAuthorityName = "issuingAuthorityName"
  val measurementUnit = "KGs"
  val documentQuantity = BigDecimal(1)
  val additionalInformationCode = "1234"
  val additionalInformationDescription = "la la la"
  val additionalActorsEori = "GB12345678912345"
  val additionalActorsType = "CS"
  val combinedNomenclatureCode = "1234567890"
  val descriptionOfGoods = "description"
  val cusCode = "321"
  val dangerousGoodsCode = "456"
  val grossMass = "1000"
  val netMass = "1200"
  val supplementaryUnits = "lbs"
  val roleCode = "2222"
  val procedureCode = "3333"
  val additionalProcedureCode = "000"
  val additionalProcedureCodes = "000 111"
  val shippingMarks = "marks"
  val numberOfPackages = 10
  val officeOfExit = "GB123456"
  val typeOfPackage = PackageType("PB", "Pallet, box Combined open-ended box and pallet")
  val lrn = "LRN"
  val ducr = "DUCR"
  val totalAmountInvoiced = "4444"
  val totalAmountInvoicedCurrency = "GBP"
  val totalPackageQuantity = "1"
  val representativeDetailsEori = "GB33333333"
  val representativeDetailsStatusCode = "1"
  val authorisationHolderEori = "GB444444444"
  val meansOfTransportOnDepartureIDNumber = "FAA"
  val detailsEori = "GB555555555"
  val exchangeRate = "1.01"
  val meansOfTransportOnDepartureType = "10"
  val goodsLocation = s"GBAUCBRLHRXXD"
  val containerId = "898989"
  val previousDocReference = "reference"
  val previousDocType = DocumentType("Master Unique Consignment Reference (MUCR) (Including any inventory reference where applicable)", "MCR")
  val goodsItemIdentifier = "identity"
  val warehouseIdentificationId = "666"
  val supervisingCustomsOffice = "Some Office"
  val meansOfTransportCrossingTheBorderType = "11"
  val meansOfTransportCrossingTheBorderTypeName = "Ship name"
  val meansOfTransportCrossingTheBorderIDNumber = "888"
  val sealName = "222222"

  val countryGB = ModelCountry("United Kingdom", "GB")
  val countryIT = ModelCountry("Italy", "IT")

  val fullName = "John Smith"
  val addressLine = "1 Export Street"
  val townOrCity = "Leeds"
  val postCode = "LS1 2PW"

  val address = Address(fullName, addressLine, townOrCity, postCode, countryGB.countryCode)
}
