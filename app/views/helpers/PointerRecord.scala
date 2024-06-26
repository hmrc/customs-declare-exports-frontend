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
import controllers.section1.routes._
import controllers.declaration.routes._
import forms.declaration.ModeOfTransportCode
import models.ExportsDeclaration
import models.declaration.{Container, ExportItem}
import play.api.i18n.Messages
import play.api.mvc.Call
import services.{DocumentTypeService, PackageTypesService}
import views.helpers.PointerPatterns.{pointerToAdditionalProcedureCodes, pointerToDucr}

trait PointerRecord {
  def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String]
  def fetchReadableValue(
    dec: ExportsDeclaration,
    args: Int*
  )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String]
  val pageLink1Param: Option[Call] = None
  val pageLink2Param: Option[String => Call] = None
}

abstract class DefaultPointerRecord extends PointerRecord {
  def fetchReadableValue(
    dec: ExportsDeclaration,
    args: Int*
  )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
    fetchRawValue(dec, args: _*)
}

// scalastyle:off
object PointerRecord {

  private val getItem = (dec: ExportsDeclaration, idx: Int) => dec.items.lift(idx)
  private val getAdditionalDocument = (item: ExportItem, idx: Int) => item.additionalDocuments.flatMap(_.documents.lift(idx))
  private val getAdditionalInformation = (item: ExportItem, idx: Int) => item.additionalInformation.flatMap(_.items.lift(idx))
  private val getAdditionalFiscalRefs = (item: ExportItem, idx: Int) => item.additionalFiscalReferencesData.flatMap(_.references.lift(idx))
  private val getPackageInformation = (item: ExportItem, idx: Int) => item.packageInformation.flatMap(_.lift(idx))
  private val getDeclarationHolder = (dec: ExportsDeclaration, idx: Int) => dec.authorisationHolders.lift(idx)
  private val getRoutingCountry = (dec: ExportsDeclaration, idx: Int) => dec.locations.routingCountries.lift(idx)
  private val getContainer = (dec: ExportsDeclaration, idx: Int) => dec.containers.lift(idx)
  private val getSeal = (container: Container, idx: Int) => container.seals.lift(idx)
  private val getPreviousDocument = (dec: ExportsDeclaration, idx: Int) => dec.previousDocuments.flatMap(_.documents.lift(idx))

  val defaultPointerRecord: DefaultPointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Some[String] = Some("MISSING") // Option.empty[String]
  }

  private val procedureCodePointerRecord: DefaultPointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.procedureCodes).flatMap(_.procedureCode)
    override val pageLink2Param: Option[String => Call] = Some(ProcedureCodesController.displayPage)
  }

  private val cusCodePointerRecord: DefaultPointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.cusCode).flatMap(_.cusCode)
    override val pageLink2Param: Option[String => Call] = Some(CusCodeController.displayPage)
  }

  private val officeOfExitRecord: DefaultPointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.officeOfExit.map(_.officeId)
    override val pageLink1Param: Option[Call] = Some(OfficeOfExitController.displayPage)
  }

  val library: Map[String, PointerRecord] = Map(
    "declaration.typeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option(dec.`type`.toString)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(raw => msgs(s"declaration.type.${raw.toLowerCase}"))
    },
    "declaration.items.$.statisticalValue.statisticalValue" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.statisticalValue.map(_.statisticalValue))
      override val pageLink2Param: Option[String => Call] = Some(StatisticalValueController.displayPage)
    },
    "declaration.items.$.additionalDocument" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option.empty[String]
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.documentTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[Nothing] = None
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentTypeCode))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentIdentifier" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentIdentifier))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.dateOfValidity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.dateOfValidity.map(_.toDisplayFormat)))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentStatus" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatus))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentStatusReason" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatusReason))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.issuingAuthorityName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.issuingAuthorityName))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentWriteOff).flatMap(_.documentQuantity.map(_.toString())))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    },
    "declaration.items.$.additionalInformation.code" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.additionalInformation.map(_.items.size.toString))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.additionalInformation.$.code" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.code))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.additionalInformation.$.description" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.description))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.commodityDetails" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.combinedNomenclatureCode)
      override val pageLink2Param: Option[String => Call] = Some(CommodityDetailsController.displayPage)
    },
    "declaration.items.$.commodityDetails.descriptionOfGoods" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.descriptionOfGoods)
      override val pageLink2Param: Option[String => Call] = Some(CommodityDetailsController.displayPage)
    },
    "declaration.items.$.cusCode.id" -> cusCodePointerRecord,
    "declaration.items.$.cusCode.cusCode" -> cusCodePointerRecord,
    "declaration.items.$.dangerousGoodsCode.dangerousGoodsCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.dangerousGoodsCode).flatMap(_.dangerousGoodsCode)
      override val pageLink2Param: Option[String => Call] = Some(UNDangerousGoodsCodeController.displayPage)
    },
    "declaration.items.$.commodityMeasure.grossMass" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.grossMass)
      override val pageLink2Param: Option[String => Call] = Some(CommodityMeasureController.displayPage)
    },
    "declaration.items.$.commodityMeasure.netMass" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.netMass)
      override val pageLink2Param: Option[String => Call] = Some(CommodityMeasureController.displayPage)
    },
    "declaration.items.$.commodityMeasure.supplementaryUnits" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.supplementaryUnits)
      override val pageLink2Param: Option[String => Call] = Some(SupplementaryUnitsController.displayPage)
    },
    "declaration.items.$.additionalFiscalReferences.$.id" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalFiscalRefs(_, args(1)).map(_.country))
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val pageLink2Param: Option[String => Call] = Some(AdditionalFiscalReferencesController.displayPage)
    },
    "declaration.items.$.additionalFiscalReferences.$.roleCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalFiscalRefs(_, args(1)).map(_.reference))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalFiscalReferencesController.displayPage)
    },
    "declaration.items.$.procedureCodes.procedureCode.current" -> procedureCodePointerRecord,
    "declaration.items.$.procedureCodes.procedureCode.previous" -> procedureCodePointerRecord,
    pointerToAdditionalProcedureCodes -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = {
        val idxOfAPC = (args(1) - 1).max(0) // APCs are stored at the same WCO document location as the procedure code, which is always first
        getItem(dec, args(0)).flatMap(_.procedureCodes.map(_.additionalProcedureCodes(idxOfAPC)))
      }
      override val pageLink2Param: Option[String => Call] = Some(AdditionalProcedureCodesController.displayPage)
    },
    "declaration.items.$.packageInformation.$.shippingMarks" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.shippingMarks))
      override val pageLink2Param: Option[String => Call] = Some(PackageInformationSummaryController.displayPage)
    },
    "declaration.items.$.packageInformation.$.numberOfPackages" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.numberOfPackages.map(_.toString)))
      override val pageLink2Param: Option[String => Call] = Some(PackageInformationSummaryController.displayPage)
    },
    "declaration.items.$.packageInformation.$.typesOfPackages" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.typesOfPackages))
      override val pageLink2Param: Option[String => Call] = Some(PackageInformationSummaryController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => PackageTypesService.findByCode(codeListConnector, code).asText)
    },
    "declaration.items.size" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.items.size.toString)
      override val pageLink1Param: Option[Call] = Some(ItemsSummaryController.displayItemsSummaryPage)
    },
    "declaration.items.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option.empty[String]
      override val pageLink1Param: Option[Call] = Some(ItemsSummaryController.displayItemsSummaryPage)
    },
    "declaration.consignmentReferences.lrn" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.consignmentReferences.flatMap(_.lrn.map(_.lrn))
      override val pageLink1Param: Option[Call] = Some(LocalReferenceNumberController.displayPage)
    },
    pointerToDucr -> new DefaultPointerRecord() {
      override val pageLink1Param: Option[Call] = Some(DucrEntryController.displayPage)

      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.consignmentReferences.flatMap(_.ducr.map(_.ducr))
    },
    "declaration.consignmentReferences.ucr" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.consignmentReferences.flatMap(_.ducr.map(_.ducr))
      override val pageLink1Param: Option[Call] = Some(DucrEntryController.displayPage)
    },
    "declaration.totalNumberOfItems.totalAmountInvoiced" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.totalAmountInvoiced)
      override val pageLink1Param: Option[Call] = Some(InvoiceAndExchangeRateController.displayPage)
    },
    "declaration.totalPackageQuantity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.totalPackage)
      override val pageLink1Param: Option[Call] = Some(TotalPackageQuantityController.displayPage)
    },
    "declaration.parties.representativeDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.representativeDetails.flatMap(_.details.flatMap(_.eori.map(_.value)))
      override val pageLink1Param: Option[Call] = Some(RepresentativeEntityController.displayPage)
    },
    "declaration.parties.representativeDetails.statusCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.representativeDetails.flatMap(_.statusCode)
      override val pageLink1Param: Option[Call] = Some(RepresentativeStatusController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.parties.representative.type.$code"))
    },
    "declaration.parties.declarationHolders.$.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getDeclarationHolder(dec, args(0)).flatMap(_.eori.map(_.value))
      override val pageLink1Param: Option[Call] = Some(AuthorisationHolderSummaryController.displayPage)
    },
    "declaration.parties.declarationHolders.$.authorisationTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getDeclarationHolder(dec, args(0)).flatMap(_.authorisationTypeCode)
      override val pageLink1Param: Option[Call] = Some(AuthorisationHolderSummaryController.displayPage)
    },
    "declaration.parties.declarationHolders.authorisationTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.authorisationHolders.size.toString)
      override val pageLink1Param: Option[Call] = Some(AuthorisationHolderSummaryController.displayPage)
    },
    "declaration.transport.meansOfTransportCrossingTheBorderIDNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportCrossingTheBorderIDNumber
      override val pageLink1Param: Option[Call] = Some(BorderTransportController.displayPage)
    },
    "declaration.transport.meansOfTransportCrossingTheBorderType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportCrossingTheBorderType
      override val pageLink1Param: Option[Call] = Some(BorderTransportController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.border.meansOfTransport.$code"))
    },
    "declaration.transport.transportCrossingTheBorderNationality.countryCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.transport.transportCrossingTheBorderNationality.flatMap(_.countryCode)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val pageLink1Param: Option[Call] = Some(TransportCountryController.displayPage)
    },
    "declaration.borderTransport.modeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        for {
          code <- dec.transport.borderModeOfTransportCode.flatMap(_.code.map(_.value))
          value <- ModeOfTransportCode.valueToCodeAll.get(code).map(_.toString)
        } yield value
      override val pageLink1Param: Option[Call] = Some(TransportLeavingTheBorderController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.inlandModeOfTransport.$code"))
    },
    "declaration.parties.carrierDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.carrierDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param: Option[Call] = Some(CarrierEoriNumberController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.carrierDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.carrierDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(CarrierDetailsController.displayPage)
    },
    "declaration.parties.carrierDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.carrierDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(CarrierDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.carrierDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.carrierDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(CarrierDetailsController.displayPage)
    },
    "declaration.transport.transportPayment.paymentMethod" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.transportPayment.map(_.paymentMethod)
      override val pageLink1Param: Option[Call] = Some(TransportPaymentController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.payment.$code"))
    },
    "declaration.locations.destinationCountries.countriesOfRouting" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.locations.routingCountries.size.toString)
      override val pageLink1Param: Option[Call] = Some(RoutingCountriesController.displayRoutingCountry)
    },
    "declaration.locations.destinationCountries.countriesOfRouting.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getRoutingCountry(dec, args(0)).flatMap(_.country.code)
      override val pageLink1Param: Option[Call] = Some(RoutingCountriesController.displayRoutingCountry)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.locations.destinationCountries.countryOfDestination" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.destinationCountry.flatMap(_.code)
      override val pageLink1Param: Option[Call] = Some(DestinationCountryController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.totalNumberOfItems.exchangeRate" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.exchangeRate)
      override val pageLink1Param: Option[Call] = Some(InvoiceAndExchangeRateController.displayPage)
    },
    "declaration.declarantDetails.details.eori" -> new DefaultPointerRecord() { // Alters if dec is CLEARANCE and isEXS and personPresentingGoodsDetails is nonEmpty
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.declarantDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param: Option[Call] = Some(DeclarantDetailsController.displayPage)
    },
    "declaration.locations.officeOfExit.circumstancesCode" -> officeOfExitRecord,
    "declaration.locations.officeOfExit.officeId" -> officeOfExitRecord,
    "declaration.parties.exporterDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param: Option[Call] = Some(ExporterEoriNumberController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.exporterDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
    },
    "declaration.parties.exporterDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
    },
    "declaration.natureOfTransaction.natureType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.natureOfTransaction.map(_.natureType)
      override val pageLink1Param: Option[Call] = Some(NatureOfTransactionController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transaction.natureOfTransaction.$code"))
    },
    "declaration.parties.additionalActors" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.declarationAdditionalActorsData.map(_.actors.size.toString)
      override val pageLink1Param: Option[Call] = Some(AdditionalActorsSummaryController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.consigneeDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consigneeDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
    },
    "declaration.parties.consignorDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consignorDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
    },
    "declaration.parties.consignorDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
    },
    "declaration.parties.consignorDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consignorDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.consignorDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
    },
    "declaration.parties.consignorDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consignorDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
    },
    "declaration.departureTransport.meansOfTransportOnDepartureIDNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportOnDepartureIDNumber
      override val pageLink1Param: Option[Call] = Some(DepartureTransportController.displayPage)
    },
    "declaration.departureTransport.borderModeOfTransportCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        for {
          imotCode <- dec.locations.inlandModeOfTransportCode
          motCode <- imotCode.inlandModeOfTransportCode
        } yield motCode.value
      override val pageLink1Param: Option[Call] = Some(InlandTransportDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map { code =>
          val motc = ModeOfTransportCode.valueToCodeAll.get(code)
          ModeOfTransportCodeHelper.transportMode(motc, false)
        }
    },
    "declaration.departureTransport.meansOfTransportOnDepartureType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportOnDepartureType
      override val pageLink1Param: Option[Call] = Some(DepartureTransportController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.departure.meansOfTransport.$code"))
    },
    "declaration.locations.goodsLocation.nameOfLocation" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param: Option[Call] = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.locations.goodsLocation.identificationOfLocation" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param: Option[Call] = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.locations.goodsLocation.qualifierOfIdentification" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param: Option[Call] = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.locations.goodsLocation.typeOfLocation" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
      override val pageLink1Param: Option[Call] = Some(LocationOfGoodsController.displayPage)
    },
    "declaration.containers.container.$.id" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getContainer(dec, args(0)).map(_.id)
      override val pageLink1Param: Option[Call] = Some(TransportContainerController.displayContainerSummary)
    },
    "declaration.containers.container.$.seals.seal.$.id" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getSeal(dec.containers(args(0)), args(1)).map(_.id)
      override val pageLink2Param: Option[String => Call] = Some(SealController.displaySealSummary)
    },
    "declaration.previousDocuments.$.documentCategory" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option.empty[String]
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.previousDocuments.$.documentReference" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).map(_.documentReference)
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.previousDocuments.$.documentType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).map(_.documentType)
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => DocumentTypeService.findByCode(codeListConnector, code).asText)
    },
    "declaration.previousDocuments.$.goodsItemIdentifier" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).flatMap(_.goodsItemIdentifier)
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.locations.warehouseIdentification.identificationNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
      override val pageLink1Param: Option[Call] = Some(WarehouseIdentificationController.displayPage)
    },
    "declaration.locations.warehouseIdentification.identificationType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
      override val pageLink1Param: Option[Call] = Some(WarehouseIdentificationController.displayPage)
    },
    "declaration.locations.warehouseIdentification.supervisingCustomsOffice" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.locations.supervisingCustomsOffice.flatMap(_.supervisingCustomsOffice)
      override val pageLink1Param: Option[Call] = Some(SupervisingCustomsOfficeController.displayPage)
    }
  )
}
// scalastyle:on

object PointerPatterns {
  val pointerToAdditionalProcedureCodes = "declaration.items.$.procedureCodes.additionalProcedureCodes.$"
  val pointerToDucr = "declaration.consignmentReferences.ucr"
}
