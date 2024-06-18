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

  val pointerToDucr = "declaration.consignmentReferences.ucr"

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

  val defaultPointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some("MISSING") // Option.empty[String]
  }

  val procedureCodePointerRecord = new DefaultPointerRecord() {
    override val pageLink2Param = Some(ProcedureCodesController.displayPage)
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.procedureCodes).flatMap(_.procedureCode)
  }

  val cusCodePointerRecord = new DefaultPointerRecord() {
    override val pageLink2Param = Some(CusCodeController.displayPage)
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.cusCode).flatMap(_.cusCode)
  }

  val officeOfExitRecord = new DefaultPointerRecord() {
    override val pageLink1Param = Some(OfficeOfExitController.displayPage)
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.officeOfExit.map(_.officeId)
  }

  val borderTransport = new DefaultPointerRecord() {
    override val pageLink1Param = Some(BorderTransportController.displayPage)
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportCrossingTheBorderIDNumber
  }

  val library: Map[String, PointerRecord] = Map(
    "declaration.typeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option(dec.`type`.toString)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(raw => msgs(s"declaration.type.${raw.toLowerCase}"))
    },
    "declaration.items.$.statisticalValue.statisticalValue" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(StatisticalValueController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.statisticalValue.map(_.statisticalValue))
    },
    "declaration.items.$.additionalDocument" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option.empty[String]
    },
    "declaration.items.$.additionalDocument.documentTypeCode" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = None
    },
    "declaration.items.$.additionalDocument.$.documentTypeCode" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentTypeCode))
    },
    "declaration.items.$.additionalDocument.$.documentIdentifier" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentIdentifier))
    },
    "declaration.items.$.additionalDocument.$.dateOfValidity" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.dateOfValidity.map(_.toDisplayFormat)))
    },
    "declaration.items.$.additionalDocument.$.documentStatus" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatus))
    },
    "declaration.items.$.additionalDocument.$.documentStatusReason" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatusReason))
    },
    "declaration.items.$.additionalDocument.$.issuingAuthorityName" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.issuingAuthorityName))
    },
    "declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalDocumentsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0))
          .flatMap(
            getAdditionalDocument(_, args(1))
              .flatMap(_.documentWriteOff)
              .flatMap(_.documentQuantity.map(_.toString()))
          )
    },
    "declaration.items.$.additionalInformation.code" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.additionalInformation.map(_.items.size.toString))
      override val pageLink2Param = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.additionalInformation.$.code" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalInformationController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.code))
    },
    "declaration.items.$.additionalInformation.$.description" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalInformationController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.description))
    },
    "declaration.items.$.commodityDetails" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(CommodityDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.combinedNomenclatureCode)
    },
    "declaration.items.$.commodityDetails.descriptionOfGoods" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(CommodityDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.descriptionOfGoods)
    },
    "declaration.items.$.cusCode.id" -> cusCodePointerRecord,
    "declaration.items.$.cusCode.cusCode" -> cusCodePointerRecord,
    "declaration.items.$.dangerousGoodsCode.dangerousGoodsCode" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(UNDangerousGoodsCodeController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.dangerousGoodsCode).flatMap(_.dangerousGoodsCode)
    },
    "declaration.items.$.commodityMeasure.grossMass" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(CommodityMeasureController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.grossMass)
    },
    "declaration.items.$.commodityMeasure.netMass" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(CommodityMeasureController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.netMass)
    },
    "declaration.items.$.commodityMeasure.supplementaryUnits" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(SupplementaryUnitsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.supplementaryUnits)
    },
    "declaration.items.$.additionalFiscalReferences.$.id" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalFiscalReferencesController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getAdditionalFiscalRefs(_, args(1)).map(_.country))

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.items.$.additionalFiscalReferences.$.roleCode" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(AdditionalFiscalReferencesController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalFiscalRefs(_, args(1)).map(_.reference))
    },
    "declaration.items.$.procedureCodes.procedureCode.current" -> procedureCodePointerRecord,
    "declaration.items.$.procedureCodes.procedureCode.previous" -> procedureCodePointerRecord,
    "declaration.items.$.packageInformation.$.shippingMarks" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(PackageInformationSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.shippingMarks))
    },
    "declaration.items.$.packageInformation.$.numberOfPackages" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(PackageInformationSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.numberOfPackages.map(_.toString)))
    },
    "declaration.items.$.packageInformation.$.typesOfPackages" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(PackageInformationSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.typesOfPackages))

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => PackageTypesService.findByCode(codeListConnector, code).asText)
    },
    "declaration.items.size" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ItemsSummaryController.displayItemsSummaryPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.items.size.toString)
    },
    "declaration.items.$" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ItemsSummaryController.displayItemsSummaryPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option.empty[String]
    },
    "declaration.consignmentReferences.lrn" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(LocalReferenceNumberController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.consignmentReferences.flatMap(_.lrn.map(_.lrn))
    },
    pointerToDucr -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(DucrEntryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.consignmentReferences.flatMap(_.ducr.map(_.ducr))
    },
    "declaration.totalNumberOfItems.totalAmountInvoiced" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(InvoiceAndExchangeRateController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.totalNumberOfItems.flatMap(_.totalAmountInvoiced)
    },
    "declaration.totalPackageQuantity" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(TotalPackageQuantityController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.totalNumberOfItems.flatMap(_.totalPackage)
    },
    "declaration.parties.representativeDetails.details.eori" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(RepresentativeEntityController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.representativeDetails.flatMap(_.details.flatMap(_.eori.map(_.value)))
    },
    "declaration.parties.representativeDetails.statusCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(RepresentativeStatusController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.representativeDetails.flatMap(_.statusCode)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.parties.representative.type.$code"))
    },
    "declaration.parties.declarationHolders.$.eori" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(AuthorisationHolderSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getDeclarationHolder(dec, args(0)).flatMap(_.eori.map(_.value))
    },
    "declaration.parties.declarationHolders.$.authorisationTypeCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(AuthorisationHolderSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getDeclarationHolder(dec, args(0)).flatMap(_.authorisationTypeCode)
    },
    "declaration.parties.declarationHolders.authorisationTypeCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(AuthorisationHolderSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.authorisationHolders.size.toString)
    },
    "declaration.transport.meansOfTransportCrossingTheBorderIDNumber" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(BorderTransportController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.transport.meansOfTransportCrossingTheBorderIDNumber
    },
    "declaration.transport.meansOfTransportCrossingTheBorderType" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(BorderTransportController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.transport.meansOfTransportCrossingTheBorderType

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.border.meansOfTransport.$code"))
    },
    "declaration.transport.transportCrossingTheBorderNationality.countryCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(TransportCountryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.transport.transportCrossingTheBorderNationality.flatMap(_.countryCode)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.borderTransport.modeCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(TransportLeavingTheBorderController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        for {
          code <- dec.transport.borderModeOfTransportCode.flatMap(_.code.map(_.value))
          value <- ModeOfTransportCode.valueToCodeAll.get(code).map(_.toString)
        } yield value

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.inlandModeOfTransport.$code"))
    },
    "declaration.parties.carrierDetails.details.eori" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(CarrierEoriNumberController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.eori.map(_.value))
    },
    "declaration.parties.carrierDetails.details.address.fullName" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map(_.fullName))
    },
    "declaration.parties.carrierDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map(_.addressLine))
    },
    "declaration.parties.carrierDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map(_.townOrCity))
    },
    "declaration.parties.carrierDetails.details.address.country" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map(_.country))

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.carrierDetails.details.address.postCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(CarrierDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map(_.postCode))
    },
    "declaration.transport.transportPayment.paymentMethod" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(TransportPaymentController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.transportPayment.map(_.paymentMethod)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.payment.$code"))
    },
    "declaration.locations.destinationCountries.countriesOfRouting" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(RoutingCountriesController.displayRoutingCountry)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.locations.routingCountries.size.toString)
    },
    "declaration.locations.destinationCountries.countriesOfRouting.$" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(RoutingCountriesController.displayRoutingCountry)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getRoutingCountry(dec, args(0)).flatMap(_.country.code)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.locations.destinationCountries.countryOfDestination" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(DestinationCountryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.destinationCountry.flatMap(_.code)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.totalNumberOfItems.exchangeRate" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(InvoiceAndExchangeRateController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.exchangeRate)
    },
    "declaration.declarantDetails.details.eori" -> new DefaultPointerRecord() { // Alters if dec is CLEARANCE and isEXS and personPresentingGoodsDetails is nonEmpty
      override val pageLink1Param = Some(DeclarantDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.declarantDetails.flatMap(_.details.eori.map(_.value))
    },
    "declaration.locations.officeOfExit.circumstancesCode" -> officeOfExitRecord,
    "declaration.locations.officeOfExit.officeId" -> officeOfExitRecord,
    "declaration.parties.exporterDetails.details.eori" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ExporterEoriNumberController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.eori.map(_.value))
    },
    "declaration.parties.exporterDetails.details.address.fullName" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.fullName))
    },
    "declaration.parties.exporterDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.townOrCity))
    },
    "declaration.parties.exporterDetails.details.address.country" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.country))

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.exporterDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.addressLine))
    },
    "declaration.parties.exporterDetails.details.address.postCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ExporterDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.postCode))
    },
    "declaration.natureOfTransaction.natureType" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(NatureOfTransactionController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.natureOfTransaction.map(_.natureType)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transaction.natureOfTransaction.$code"))
    },
    "declaration.parties.additionalActors" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(AdditionalActorsSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.declarationAdditionalActorsData.map(_.actors.size.toString)
    },
    "declaration.parties.consigneeDetails.details.address.fullName" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.fullName))
    },
    "declaration.parties.consigneeDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.townOrCity))
    },
    "declaration.parties.consigneeDetails.details.address.country" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.country))

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.consigneeDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.addressLine))
    },
    "declaration.parties.consigneeDetails.details.address.postCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsigneeDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.postCode))
    },
    "declaration.parties.consignorDetails.details.address.fullName" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsignorDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.fullName))
    },
    "declaration.parties.consignorDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsignorDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.townOrCity))
    },
    "declaration.parties.consignorDetails.details.address.country" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsignorDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.country))

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    },
    "declaration.parties.consignorDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsignorDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.addressLine))
    },
    "declaration.parties.consignorDetails.details.address.postCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(ConsignorDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.postCode))
    },
    "declaration.departureTransport.meansOfTransportOnDepartureIDNumber" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(DepartureTransportController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportOnDepartureIDNumber
    },
    "declaration.departureTransport.borderModeOfTransportCode" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(InlandTransportDetailsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        for {
          imotCode <- dec.locations.inlandModeOfTransportCode
          motCode <- imotCode.inlandModeOfTransportCode
        } yield motCode.value

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
      override val pageLink1Param = Some(DepartureTransportController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportOnDepartureType

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.departure.meansOfTransport.$code"))
    },
    "declaration.locations.goodsLocation.nameOfLocation" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
    },
    "declaration.locations.goodsLocation.identificationOfLocation" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
    },
    "declaration.locations.goodsLocation.qualifierOfIdentification" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
    },
    "declaration.locations.goodsLocation.typeOfLocation" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(LocationOfGoodsController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.goodsLocation.map(_.value)
    },
    "declaration.containers.container.$.id" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(TransportContainerController.displayContainerSummary)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getContainer(dec, args(0)).map(_.id)
    },
    "declaration.containers.container.$.seals.seal.$.id" -> new DefaultPointerRecord() {
      override val pageLink2Param = Some(SealController.displaySealSummary)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getSeal(dec.containers(args(0)), args(1)).map(_.id)
    },
    "declaration.previousDocuments.$.documentCategory" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option.empty[String]
    },
    "declaration.previousDocuments.$.documentReference" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).map(_.documentReference)
    },
    "declaration.previousDocuments.$.documentType" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).map(_.documentType)

      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => DocumentTypeService.findByCode(codeListConnector, code).asText)
    },
    "declaration.previousDocuments.$.goodsItemIdentifier" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(PreviousDocumentsSummaryController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getPreviousDocument(dec, args(0)).flatMap(_.goodsItemIdentifier)
    },
    "declaration.locations.warehouseIdentification.identificationNumber" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(WarehouseIdentificationController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
    },
    "declaration.locations.warehouseIdentification.identificationType" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(WarehouseIdentificationController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
    },
    "declaration.locations.warehouseIdentification.supervisingCustomsOffice" -> new DefaultPointerRecord() {
      override val pageLink1Param = Some(SupervisingCustomsOfficeController.displayPage)
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.locations.supervisingCustomsOffice.flatMap(_.supervisingCustomsOffice)
    }
  )
}
// scalastyle:on
