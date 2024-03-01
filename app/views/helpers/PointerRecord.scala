/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.CodeListConnector
import controllers.declaration.routes._
import forms.declaration.ModeOfTransportCode
import models.ExportsDeclaration
import models.declaration.{Container, ExportItem}
import play.api.i18n.Messages
import play.api.mvc.Call
import services.{DocumentTypeService, PackageTypesService}

import javax.inject.{Inject, Singleton}

trait PointerRecord {
  def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String]
  def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String]
  val pageLink1Param: Option[Call] = None
  val pageLink2Param: Option[String => Call] = None
}

abstract class DefaultPointerRecord extends PointerRecord {
  def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
    fetchRawValue(dec, args: _*)
}

// scalastyle:off
@Singleton
class PointerRecords @Inject() (countryHelper: CountryHelper, codeListConnector: CodeListConnector) {

  val getItem = (dec: ExportsDeclaration, idx: Int) => dec.items.lift(idx)
  val getAdditionalDocument = (item: ExportItem, idx: Int) => item.additionalDocuments.flatMap(_.documents.lift(idx))
  val getAdditionalInformation = (item: ExportItem, idx: Int) => item.additionalInformation.flatMap(_.items.lift(idx))
  val getAdditionalFiscalRefs = (item: ExportItem, idx: Int) => item.additionalFiscalReferencesData.flatMap(_.references.lift(idx))
  val getPackageInformation = (item: ExportItem, idx: Int) => item.packageInformation.flatMap(_.lift(idx))
  val getDeclarationHolder = (dec: ExportsDeclaration, idx: Int) => dec.authorisationHolders.lift(idx)
  val getRoutingCountry = (dec: ExportsDeclaration, idx: Int) => dec.locations.routingCountries.lift(idx)
  val getContainer = (dec: ExportsDeclaration, idx: Int) => dec.containers.lift(idx)
  val getSeal = (container: Container, idx: Int) => container.seals.lift(idx)
  val getPreviousDocument = (dec: ExportsDeclaration, idx: Int) => dec.previousDocuments.flatMap(_.documents.lift(idx))

  val defaultPointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Some("MISSING") // Option.empty[String]
  }

  val procedureCodePointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
      getItem(dec, args(0)).flatMap(_.procedureCodes).flatMap(_.procedureCode)
    override val pageLink2Param = Some(ProcedureCodesController.displayPage)
  }

  val cusCodePointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
      getItem(dec, args(0)).flatMap(_.cusCode).flatMap(_.cusCode)
    override val pageLink2Param = Some(CusCodeController.displayPage)
  }

  val officeOfExitRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.officeOfExit.map(_.officeId)
    override val pageLink1Param = Some(OfficeOfExitController.displayPage)
  }

  val borderTransport = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.meansOfTransportCrossingTheBorderIDNumber
    override val pageLink1Param = Some(BorderTransportController.displayPage)
  }

  val library: Map[String, PointerRecord] = Map(
    "declaration.typeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option(dec.`type`.toString)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages) =
        fetchRawValue(dec, args: _*).map(raw => msgs(s"declaration.type.${raw.toLowerCase}"))
    },
    "declaration.items.$.statisticalValue.statisticalValue" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.statisticalValue.map(_.statisticalValue))
      override val pageLink2Param = Some(StatisticalValueController.displayPage)
    },
    "declaration.items.$.additionalDocument" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option.empty[String]
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentTypeCode))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentIdentifier" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentIdentifier))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.dateOfValidity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.dateOfValidity.map(_.toDisplayFormat)))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentStatus" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatus))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentStatusReason" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatusReason))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.issuingAuthorityName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.issuingAuthorityName))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentWriteOff).flatMap(_.documentQuantity.map(_.toString())))
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalInformation.$.code" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.code))
      override val pageLink2Param = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.additionalInformation.$.description" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.description))
      override val pageLink2Param = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.commodityDetails" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.combinedNomenclatureCode)
      override val pageLink2Param = Some(CommodityDetailsController.displayPage)
    },
    "declaration.items.$.commodityDetails.descriptionOfGoods" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.descriptionOfGoods)
      override val pageLink2Param = Some(CommodityDetailsController.displayPage)
    },
    "declaration.items.$.cusCode.id" -> cusCodePointerRecord,
    "declaration.items.$.cusCode.cusCode" -> cusCodePointerRecord,
    "declaration.items.$.dangerousGoodsCode.dangerousGoodsCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.dangerousGoodsCode).flatMap(_.dangerousGoodsCode)
      override val pageLink2Param = Some(UNDangerousGoodsCodeController.displayPage)
    },
    "declaration.items.$.commodityMeasure.grossMass" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.grossMass)
      override val pageLink2Param = Some(CommodityMeasureController.displayPage)
    },
    "declaration.items.$.commodityMeasure.netMass" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.netMass)
      override val pageLink2Param = Some(CommodityMeasureController.displayPage)
    },
    "declaration.items.$.commodityMeasure.supplementaryUnits" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.supplementaryUnits)
      override val pageLink2Param = Some(SupplementaryUnitsController.displayPage)
    },
    "declaration.items.$.additionalFiscalReferences.$.id" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalFiscalRefs(_, args(1)).map(_.country))
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(countryHelper.getShortNameForCountryCode)
      override val pageLink2Param = Some(AdditionalFiscalReferencesController.displayPage)
    },
    "declaration.items.$.additionalFiscalReferences.$.roleCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalFiscalRefs(_, args(1)).map(_.reference))
      override val pageLink2Param = Some(AdditionalFiscalReferencesController.displayPage)
    },
    "declaration.items.$.procedureCodes.procedureCode.current" -> procedureCodePointerRecord,
    "declaration.items.$.procedureCodes.procedureCode.previous" -> procedureCodePointerRecord,
    "declaration.items.$.packageInformation.$.shippingMarks" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.shippingMarks))
      override val pageLink2Param = Some(PackageInformationSummaryController.displayPage)
    },
    "declaration.items.$.packageInformation.$.numberOfPackages" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.numberOfPackages.map(_.toString)))
      override val pageLink2Param = Some(PackageInformationSummaryController.displayPage)
    },
    "declaration.items.$.packageInformation.$.typesOfPackages" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.typesOfPackages))
      override val pageLink2Param = Some(PackageInformationSummaryController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => PackageTypesService.findByCode(codeListConnector, code).asText)
    },
    "declaration.items.size" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Some(dec.items.size.toString)
      override val pageLink1Param = Some(ItemsSummaryController.displayItemsSummaryPage)
    },
    "declaration.items.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option.empty[String]
      override val pageLink1Param = Some(ItemsSummaryController.displayItemsSummaryPage)
    },
    "declaration.consignmentReferences.lrn" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.consignmentReferences.flatMap(_.lrn.map(_.lrn))
      override val pageLink1Param = Some(LocalReferenceNumberController.displayPage)
    },
    "declaration.consignmentReferences.ucr" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.consignmentReferences.flatMap(_.ducr.map(_.ducr))
      override val pageLink1Param = Some(DucrEntryController.displayPage)
    },
    "declaration.totalNumberOfItems.totalAmountInvoiced" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.totalNumberOfItems.flatMap(_.totalAmountInvoiced)
      override val pageLink1Param = Some(InvoiceAndExchangeRateController.displayPage)
    },
    "declaration.totalPackageQuantity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.totalNumberOfItems.flatMap(_.totalPackage)
      override val pageLink1Param = Some(TotalPackageQuantityController.displayPage)
    },
    "declaration.parties.representativeDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.representativeDetails.flatMap(_.details.flatMap(_.eori.map(_.value)))
      override val pageLink1Param = Some(RepresentativeEntityController.displayPage)
    },
    "declaration.parties.representativeDetails.statusCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.representativeDetails.flatMap(_.statusCode)
      override val pageLink1Param = Some(RepresentativeStatusController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.parties.representative.type.$code"))
    },
    "declaration.parties.declarationHolders.$.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getDeclarationHolder(dec, args(0)).flatMap(_.eori.map(_.value))
      override val pageLink1Param = Some(AuthorisationHolderSummaryController.displayPage)
    },
    "declaration.parties.declarationHolders.$.authorisationTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getDeclarationHolder(dec, args(0)).flatMap(_.authorisationTypeCode)
      override val pageLink1Param = Some(AuthorisationHolderSummaryController.displayPage)
    },
    "declaration.transport.meansOfTransportCrossingTheBorderIDNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.meansOfTransportCrossingTheBorderIDNumber
      override val pageLink1Param = Some(BorderTransportController.displayPage)
    },
    "declaration.transport.meansOfTransportCrossingTheBorderType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.meansOfTransportCrossingTheBorderType
      override val pageLink1Param = Some(BorderTransportController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.border.meansOfTransport.$code"))
    },
    "declaration.transport.transportCrossingTheBorderNationality.countryName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.transportCrossingTheBorderNationality.flatMap(_.countryName)

      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(countryHelper.getShortNameForCountryCode)
      override val pageLink1Param = Some(TransportCountryController.displayPage)
    },
    "declaration.borderTransport.modeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        for {
          code <- dec.transport.borderModeOfTransportCode.flatMap(_.code.map(_.value))
          value <- ModeOfTransportCode.valueToCodeAll.get(code).map(_.toString)
        } yield value
      override val pageLink1Param = Some(TransportLeavingTheBorderController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.inlandModeOfTransport.$code"))
    },
    "declaration.parties.carrierDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.carrierDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param = Some(CarrierEoriNumberController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.carrierDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.carrierDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.carrierDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.carrierDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.carrierDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
    },
    "declaration.transport.transportPayment.paymentMethod" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.transportPayment.map(_.paymentMethod)
      override val pageLink1Param = Some(TransportPaymentController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.payment.$code"))
    },
    "declaration.locations.destinationCountries.countriesOfRouting.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getRoutingCountry(dec, args(0)).flatMap(_.country.code)
      override val pageLink1Param = Some(RoutingCountriesController.displayRoutingCountry)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(countryHelper.getShortNameForCountryCode)
    },
    "declaration.locations.destinationCountries.countryOfDestination" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.destinationCountry.flatMap(_.code)
      override val pageLink1Param = Some(DestinationCountryController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(countryHelper.getShortNameForCountryCode)
    },
    "declaration.totalNumberOfItems.exchangeRate" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.totalNumberOfItems.flatMap(_.exchangeRate)
      override val pageLink1Param = Some(InvoiceAndExchangeRateController.displayPage)
    },
    "declaration.declarantDetails.details.eori" -> new DefaultPointerRecord() { // Alters if dec is CLEARANCE and isEXS and personPresentingGoodsDetails is nonEmpty
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.declarantDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param = Some(DeclarantDetailsController.displayPage)
    },
    "declaration.locations.officeOfExit.circumstancesCode" -> officeOfExitRecord,
    "declaration.locations.officeOfExit.officeId" -> officeOfExitRecord,
    "declaration.parties.exporterDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.exporterDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param = Some(ExporterEoriNumberController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.exporterDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.exporterDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.exporterDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.exporterDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.exporterDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
    },
    "declaration.natureOfTransaction.natureType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.natureOfTransaction.map(_.natureType)
      override val pageLink1Param = Some(NatureOfTransactionController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transaction.natureOfTransaction.$code"))
    },
    "declaration.parties.additionalActors" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.declarationAdditionalActorsData.map(_.actors.size.toString)
      override val pageLink1Param = Some(AdditionalActorsSummaryController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.departureTransport.meansOfTransportOnDepartureIDNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.meansOfTransportOnDepartureIDNumber
      override val pageLink1Param = Some(DepartureTransportController.displayPage)
    },
    "declaration.departureTransport.borderModeOfTransportCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        for {
          imotCode <- dec.locations.inlandModeOfTransportCode
          motCode <- imotCode.inlandModeOfTransportCode
        } yield motCode.value
      override val pageLink1Param = Some(InlandTransportDetailsController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map { code =>
          val motc = ModeOfTransportCode.valueToCodeAll.get(code)
          ModeOfTransportCodeHelper.transportMode(motc, false)
        }
    },
    "declaration.departureTransport.meansOfTransportOnDepartureType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.transport.meansOfTransportOnDepartureType
      override val pageLink1Param = Some(DepartureTransportController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.departure.meansOfTransport.$code"))
    },
    "declaration.locations.goodsLocation.nameOfLocation" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.locations.goodsLocation.identificationOfLocation" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.locations.goodsLocation.qualifierOfIdentification" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.locations.goodsLocation.typeOfLocation" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.containers.container.$.id" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getContainer(dec, args(0)).map(_.id)
      override val pageLink1Param = Some(TransportContainerController.displayContainerSummary)
    },
    "declaration.previousDocuments.$.documentCategory" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option.empty[String]
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.previousDocuments.$.documentReference" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getPreviousDocument(dec, args(0)).map(_.documentReference)
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.previousDocuments.$.documentType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getPreviousDocument(dec, args(0)).map(_.documentType)
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
      override def fetchReadableValue(dec: ExportsDeclaration, args: Int*)(implicit msgs: Messages): Option[String] =
        fetchRawValue(dec, args: _*).map(code => DocumentTypeService.findByCode(codeListConnector, code).asText)
    },
    "declaration.previousDocuments.$.goodsItemIdentifier" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = getPreviousDocument(dec, args(0)).flatMap(_.goodsItemIdentifier)
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.locations.warehouseIdentification.identificationNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
      override val pageLink1Param = Some(WarehouseIdentificationController.displayPage)
    },
    "declaration.locations.warehouseIdentification.identificationType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
      override val pageLink1Param = Some(WarehouseIdentificationController.displayPage)
    },
    "declaration.locations.warehouseIdentification.supervisingCustomsOffice" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = dec.locations.supervisingCustomsOffice.flatMap(_.supervisingCustomsOffice)
      override val pageLink1Param = Some(SupervisingCustomsOfficeController.displayPage)
    }
  )
}
// scalastyle:on
