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
import controllers.section2.routes._
import controllers.section3.routes._
import controllers.section4.routes._
import controllers.section5.routes._
import controllers.section6.routes._
import forms.section6.ModeOfTransportCode
import models.ExportsDeclaration
import models.declaration.ExportItem.itemsPrefix
import models.declaration.{Container, ExportItem}
import play.api.i18n.Messages
import play.api.mvc.Call
import services.{DocumentTypeService, PackageTypesService}
import views.helpers.PointerPatterns._

trait PointerRecord {
  def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String]
  def fetchReadableValue(
    dec: ExportsDeclaration,
    args: Int*
  )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String]
  val pageLink1Param: Option[Call] = None
  val pageLink2Param: Option[String => Call] = None
  val amendKey: Option[String]
}

abstract class DefaultPointerRecord extends PointerRecord {
  def fetchReadableValue(
    dec: ExportsDeclaration,
    args: Int*
  )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
    fetchRawValue(dec, args: _*)

  val amendKey = Option.empty[String]
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

  val defaultPointerRecord: DefaultPointerRecord = new DefaultPointerRecord {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Some[String] = Some("MISSING") // Option.empty[String]
    override val amendKey: Option[String] = None
  }

  private val procedureCodePointerRecord = new DefaultPointerRecord {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.procedureCodes).flatMap(_.procedureCode)
    override val pageLink2Param: Option[String => Call] = Some(ProcedureCodesController.displayPage)
    override val amendKey: Option[String] = Some(s"$itemsPrefix.procedureCode")
  }

  private val cusCodePointerRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.cusCode).flatMap(_.cusCode)
    override val pageLink2Param: Option[String => Call] = Some(CusCodeController.displayPage)
    override val amendKey: Option[String] = Some(s"$itemsPrefix.cusCode")
  }

  private val officeOfExitRecord = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.officeOfExit.map(_.officeId)
    override val pageLink1Param: Option[Call] = Some(OfficeOfExitController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.locations.officeOfExit")
  }

  private val declarationHoldersEori = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getDeclarationHolder(dec, args(0)).flatMap(_.eori.map(_.value))
    override val pageLink1Param: Option[Call] = Some(AuthorisationHolderSummaryController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.parties.holders.holder.eori")
  }

  private val declarationHoldersType = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getDeclarationHolder(dec, args(0)).flatMap(_.authorisationTypeCode)
    override val pageLink1Param: Option[Call] = Some(AuthorisationHolderSummaryController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.parties.holders.holder.type")
  }

  private val commodityDetailsCombinedNomenclatureCode = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.combinedNomenclatureCode)
    override val pageLink2Param: Option[String => Call] = Some(CommodityDetailsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.commodityCode")
  }

  private val documentTypeCode = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentTypeCode))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.code")
  }

  private val documentIdentifier = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentIdentifier))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.identifier")
  }

  private val documentStatus = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatus))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.status")
  }

  private val documentStatusReason = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentStatusReason))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.statusReason")
  }

  private val issuingAuthorityName = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.issuingAuthorityName))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.issuingAuthorityName")
  }

  private val dateOfValidity = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.dateOfValidity.map(_.toDisplayFormat)))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.dateOfValidity")
  }

  private val documentWriteOff = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentWriteOff).flatMap(_.measurementUnit))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.measurementUnit")
  }

  private val documentQuantity = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentWriteOff).flatMap(_.documentQuantity.map(_.toString())))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.measurementUnitQuantity")
  }

  private val additionalInformationCode = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.code))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalInformationController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalInformation.code")
  }

  private val additionalInformationDescription = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      getItem(dec, args(0)).flatMap(getAdditionalInformation(_, args(1)).map(_.description))
    override val pageLink2Param: Option[String => Call] = Some(AdditionalInformationController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.item.additionalInformation.description")
  }

  private val borderModeOfTransportCode = new DefaultPointerRecord() {
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
    override val amendKey: Option[String] = Some("declaration.summary.transport.departure.transportCode.header")
  }

  private val meansOfTransportOnDepartureType = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportOnDepartureType
    override val pageLink1Param: Option[Call] = Some(DepartureTransportController.displayPage)
    override def fetchReadableValue(
      dec: ExportsDeclaration,
      args: Int*
    )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
      fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transport.departure.meansOfTransport.$code"))
    override val amendKey: Option[String] = Some("declaration.summary.transport.departure.meansOfTransport.type")
  }

  private val meansOfTransportOnDepartureIDNumber = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportOnDepartureIDNumber
    override val pageLink1Param: Option[Call] = Some(DepartureTransportController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.transport.border.meansOfTransport.id")
  }

  private val transportCrossingTheBorderNationality = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
      dec.transport.transportCrossingTheBorderNationality.flatMap(_.countryCode)
    override def fetchReadableValue(
      dec: ExportsDeclaration,
      args: Int*
    )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
      fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
    override val pageLink1Param: Option[Call] = Some(TransportCountryController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.transport.registrationCountry")
  }

  private val containers = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getContainer(dec, args(0)).map(_.id)
    override val pageLink1Param: Option[Call] = Some(ContainerController.displayContainerSummary)
    override val amendKey: Option[String] = Some("declaration.summary.container.id")
  }

  private val seals = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.containers.lift(args(0)).flatMap(getSeal(_, args(1)).map(_.id))
    override val pageLink2Param: Option[String => Call] = Some(SealController.displaySealSummary)
    override val amendKey: Option[String] = Some("declaration.summary.container.securitySeals")
  }

  private val inlandModeOfTransportCode = new DefaultPointerRecord() {
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
    override val amendKey = Some("declaration.summary.transport.inlandModeOfTransport")
  }

  private val exporterEori = new DefaultPointerRecord() {
    def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.eori.map(_.value))
    override val pageLink1Param: Option[Call] = Some(ExporterEoriNumberController.displayPage)
    override val amendKey: Option[String] = Some("declaration.summary.parties.actors.eori")
  }

  val pointersToPointerRecords: Map[String, PointerRecord] = Map(
    "declaration.typeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option(dec.`type`.toString)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(raw => msgs(s"declaration.type.${raw.toLowerCase}"))
      override val amendKey: Option[String] = None
    },
    "declaration.goodsItemQuantity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        Some(dec.items.size.toString)
      override val amendKey: Option[String] = Some("ead.template.goodsItemQuantity")
    },
    "declaration.items.$.statisticalValue.statisticalValue" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.statisticalValue.map(_.statisticalValue))
      override val pageLink2Param: Option[String => Call] = Some(StatisticalValueController.displayPage)
      override val amendKey: Option[String] = Some(s"$itemsPrefix.itemValue")
    },
    "declaration.items.$.additionalDocument" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*) = Option.empty[String]
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
      override val amendKey: Option[String] = None
    },
//    "declaration.items.$.additionalDocument.documentTypeCode" -> documentTypeCode,
    "declaration.items.$.additionalDocument.documents.$.documentTypeCode" -> documentTypeCode,
    "declaration.items.$.additionalDocument.$.documentTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getAdditionalDocument(_, args(1)).flatMap(_.documentTypeCode))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalDocumentsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.additionalDocuments.code")
    },
    "declaration.items.$.additionalDocument.$.documentIdentifier" -> documentIdentifier,
    "declaration.items.$.additionalDocument.documents.$.documentIdentifier" -> documentIdentifier,
    "declaration.items.$.additionalDocument.$.dateOfValidity" -> dateOfValidity,
    "declaration.items.$.additionalDocument.documents.$.dateOfValidity" -> dateOfValidity,
    "declaration.items.$.additionalDocument.$.documentStatus" -> documentStatus,
    "declaration.items.$.additionalDocument.documents.$.documentStatus" -> documentStatus,
    "declaration.items.$.additionalDocument.$.documentStatusReason" -> documentStatusReason,
    "declaration.items.$.additionalDocument.documents.$.documentStatusReason" -> documentStatusReason,
    "declaration.items.$.additionalDocument.$.issuingAuthorityName" -> issuingAuthorityName,
    "declaration.items.$.additionalDocument.documents.$.issuingAuthorityName" -> issuingAuthorityName,
    "declaration.items.$.additionalDocument.$.documentWriteOff.measurementUnit" -> documentWriteOff,
    "declaration.items.$.additionalDocument.documents.$.documentWriteOff.measurementUnit" -> documentWriteOff,
    "declaration.items.$.additionalDocument.documents.$.documentWriteOff" -> documentWriteOff,
    "declaration.items.$.additionalDocument.documents.$.documentQuantity" -> documentQuantity,
    "declaration.items.$.additionalDocument.documents.$.documentWriteOff.documentQuantity" -> documentQuantity,
    "declaration.items.$.additionalDocument.$.documentWriteOff.documentQuantity" -> documentQuantity,
    "declaration.items.$.additionalInformation.code" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.additionalInformation.map(_.items.size.toString))
      override val pageLink2Param: Option[String => Call] = Some(AdditionalInformationController.displayPage)
    },
    "declaration.items.$.additionalInformation.items.$.code" -> additionalInformationCode,
    "declaration.items.$.additionalInformation.$.code" -> additionalInformationCode,
    "declaration.items.$.additionalInformation.items.$.description" -> additionalInformationDescription,
    "declaration.items.$.additionalInformation.$.description" -> additionalInformationDescription,
    "declaration.items.$.commodityDetails" -> commodityDetailsCombinedNomenclatureCode,
    "declaration.items.$.commodityDetails.combinedNomenclatureCode" -> commodityDetailsCombinedNomenclatureCode,
    "declaration.items.$.commodityDetails.descriptionOfGoods" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityDetails).flatMap(_.descriptionOfGoods)
      override val pageLink2Param: Option[String => Call] = Some(CommodityDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.goodsDescription")
    },
    "declaration.items.$.cusCode.id" -> cusCodePointerRecord,
    "declaration.items.$.cusCode.cusCode" -> cusCodePointerRecord,
    "declaration.items.$.dangerousGoodsCode.dangerousGoodsCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.dangerousGoodsCode).flatMap(_.dangerousGoodsCode)
      override val pageLink2Param: Option[String => Call] = Some(UNDangerousGoodsCodeController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.unDangerousGoodsCode")
    },
    "declaration.items.$.commodityMeasure.grossMass" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.grossMass)
      override val pageLink2Param: Option[String => Call] = Some(CommodityMeasureController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.grossWeight")
    },
    "declaration.items.$.commodityMeasure.netMass" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.netMass)
      override val pageLink2Param: Option[String => Call] = Some(CommodityMeasureController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.netWeight")
    },
    "declaration.items.$.commodityMeasure.supplementaryUnits" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.commodityMeasure).flatMap(_.supplementaryUnits)
      override val pageLink2Param: Option[String => Call] = Some(SupplementaryUnitsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.supplementaryUnits")
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
      override val amendKey: Option[String] = Some("declaration.summary.item.VATdetails")
    },
    "declaration.items.$.procedureCodes.procedureCode.current" -> procedureCodePointerRecord,
    "declaration.items.$.procedureCodes.procedureCode.previous" -> procedureCodePointerRecord,
    "declaration.items.$.procedureCodes.procedure.code" -> procedureCodePointerRecord,
    "declaration.items.$.procedureCodes.additionalProcedureCodes" -> new DefaultPointerRecord {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.procedureCodes.map(pcd => pcd.additionalProcedureCodes.mkString(" ")))
      override val amendKey: Option[String] = Some("declaration.summary.item.additionalProcedureCode")
    },
    "declaration.items.$.procedureCodes.additionalProcedureCodes.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = {
        val idxOfAPC = (args(1) - 1).max(0) // In the WCO model, APCs are stored in same seq element as the procedure code, which is always the first
        getItem(dec, args(0)).flatMap(_.procedureCodes.flatMap { pc =>
          if (pc.additionalProcedureCodes.isEmpty) Option.empty[String] else Some(pc.additionalProcedureCodes(idxOfAPC))
        })
      }
      override val pageLink2Param: Option[String => Call] = Some(AdditionalProcedureCodesController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.additionalProcedureCode")
    },
    // This pointer is equivalent to above but is generated by our diff tool.
    // In our exports model, APCs are stored in a different seq entity than the procedure code, so indexing must be handled differently
    "declaration.items.$.procedureCodes.additionalPcs.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.procedureCodes.flatMap { pc =>
          if (args(1) >= pc.additionalProcedureCodes.size) Option.empty[String] else Some(pc.additionalProcedureCodes(args(1)))
        })
      override val pageLink2Param: Option[String => Call] = Some(AdditionalProcedureCodesController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.additionalProcedureCode")
    },
    "declaration.items.$.nactCode.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.nactCodes.flatMap(_.lift(args(1)).map(_.nactCode)))
      override val amendKey: Option[String] = Some("declaration.summary.item.nationalAdditionalCodes")
    },
    "declaration.items.$.nactExemptionCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(_.nactExemptionCode.map(_.nactCode))
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.item.zeroRatedForVat.$code"))
      override val amendKey: Option[String] = Some("declaration.summary.item.zeroRatedForVat")
    },
    "declaration.items.$.packageInformation.$.shippingMarks" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.shippingMarks))
      override val pageLink2Param: Option[String => Call] = Some(PackageInformationSummaryController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.packageInformation.markings")
    },
    "declaration.items.$.packageInformation.$.numberOfPackages" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        getItem(dec, args(0)).flatMap(getPackageInformation(_, args(1)).flatMap(_.numberOfPackages.map(_.toString)))
      override val pageLink2Param: Option[String => Call] = Some(PackageInformationSummaryController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.item.packageInformation.number")
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
      override val amendKey: Option[String] = Some("declaration.summary.item.packageInformation.type")
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
    pointerToDucr -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.consignmentReferences.flatMap(_.ducr.map(_.ducr))
      override val pageLink1Param: Option[Call] = Some(DucrEntryController.displayPage)
    },
    "declaration.totalNumberOfItems.totalAmountInvoiced" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.totalAmountInvoiced)
      override val pageLink1Param: Option[Call] = Some(InvoiceAndExchangeRateController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transaction.itemAmount")
    },
    "declaration.totalNumberOfItems.totalAmountInvoicedCurrency" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.totalAmountInvoicedCurrency)
      override val pageLink1Param: Option[Call] = Some(InvoiceAndExchangeRateController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transaction.currencyCode")
    },
    "declaration.totalNumberOfItems.totalPackage" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.totalPackage)
      override val pageLink1Param: Option[Call] = Some(TotalPackageQuantityController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transaction.totalNoOfPackages")
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
    "declaration.parties.declarationHolders.$.eori" -> declarationHoldersEori,
    "declaration.parties.declarationHolders.holders.$.eori" -> declarationHoldersEori,
    "declaration.parties.declarationHolders.$.authorisationTypeCode" -> declarationHoldersType,
    "declaration.parties.declarationHolders.holders.$.authorisationTypeCode" -> declarationHoldersType,
    "declaration.parties.declarationHolders.authorisationTypeCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.authorisationHolders.size.toString)
      override val pageLink1Param: Option[Call] = Some(AuthorisationHolderSummaryController.displayPage)
    },
    "declaration.transport.meansOfTransportCrossingTheBorderIDNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.meansOfTransportCrossingTheBorderIDNumber
      override val pageLink1Param: Option[Call] = Some(BorderTransportController.displayPage)
      override val amendKey = Some("declaration.summary.transport.departure.meansOfTransport.header")
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
    "declaration.transport.meansOfTransportCrossingTheBorderNationality" -> transportCrossingTheBorderNationality,
    "declaration.transport.transportCrossingTheBorderNationality.countryCode" -> transportCrossingTheBorderNationality,
    "declaration.transport.borderModeOfTransportCode.code" -> borderModeOfTransportCode,
    "declaration.borderTransport.modeCode" -> borderModeOfTransportCode,
    "declaration.parties.carrierDetails.details.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.carrierDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param: Option[Call] = Some(CarrierEoriNumberController.displayPage)
      override val amendKey = Some("declaration.carrierEori.eori.label")
    },
    "declaration.parties.carrierDetails.details.address" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map { address =>
          List(address.fullName, address.addressLine, address.postCode, address.country).mkString(", ")
        })
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        dec.parties.carrierDetails.flatMap(_.details.address.map { address =>
          List(address.fullName, address.addressLine, address.postCode, countryHelper.getShortNameForCountryCode(address.country).getOrElse(""))
            .mkString("<br/>")
        })
      override val pageLink1Param: Option[Call] = Some(CarrierDetailsController.displayPage)
      override val amendKey = Some("declaration.summary.parties.carrier.address")
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
      override val amendKey = Some("declaration.summary.transport.payment")
    },
    "declaration.transport.expressConsignment" -> new DefaultPointerRecord {
      override def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.transport.expressConsignment.map(_.answer)
      override val pageLink1Param: Option[Call] = Some(ExpressConsignmentController.displayPage)
      override val amendKey = Some("declaration.summary.transport.expressConsignment")
    },
    "declaration.locations.destinationCountries.countriesOfRouting" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Some(dec.locations.routingCountries.size.toString)
      override val pageLink1Param: Option[Call] = Some(RoutingCountriesController.displayRoutingCountry)
      override val amendKey: Option[String] = Some("declaration.summary.countries.routingCountry")
    },
    "declaration.locations.destinationCountries.countriesOfRouting.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getRoutingCountry(dec, args(0)).flatMap(_.country.code)
      override val pageLink1Param: Option[Call] = Some(RoutingCountriesController.displayRoutingCountry)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val amendKey: Option[String] = Some("declaration.summary.countries.routingCountry")
    },
    "declaration.locations.destinationCountries.countryOfDestination" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.destinationCountry.flatMap(_.code)
      override val pageLink1Param: Option[Call] = Some(DestinationCountryController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val amendKey: Option[String] = Some("declaration.summary.countries.countryOfDestination")
    },
    "declaration.totalNumberOfItems.exchangeRate" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.totalNumberOfItems.flatMap(_.exchangeRate)
      override val pageLink1Param: Option[Call] = Some(InvoiceAndExchangeRateController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transaction.exchangeRate")
    },
    "declaration.declarantDetails.details.eori" -> new DefaultPointerRecord() { // Alters if dec is CLEARANCE and isEXS and personPresentingGoodsDetails is nonEmpty
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.declarantDetails.flatMap(_.details.eori.map(_.value))
      override val pageLink1Param: Option[Call] = Some(DeclarantDetailsController.displayPage)
    },
    "declaration.locations.officeOfExit.circumstancesCode" -> officeOfExitRecord,
    "declaration.locations.officeOfExit.officeId" -> officeOfExitRecord,
    "declaration.parties.personPresentingGoodsDetails" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.isEntryIntoDeclarantsRecords.map(_.value)
      override val amendKey: Option[String] = Some("declaration.summary.parties.eidr")
    },
    "declaration.parties.personPresentingGoodsDetails.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.personPresentingGoodsDetails.map(_.eori.value)
      override val amendKey: Option[String] = Some("declaration.summary.parties.exporter.eori")
    },
    "declaration.parties.exporterDetails.eori" -> exporterEori,
    "declaration.parties.exporterDetails.details.eori" -> exporterEori,
    "declaration.parties.exporterDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.exporter.address.fullName")
    },
    "declaration.parties.exporterDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.exporter.address.townOrCity")
    },
    "declaration.parties.exporterDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val amendKey: Option[String] = Some("declaration.summary.parties.exporter.address.country")
    },
    "declaration.parties.exporterDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.exporterDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.exporter.address.addressLine")
    },
    "declaration.parties.exporterDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.exporterDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(ExporterDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.exporter.address.postCode")
    },
    "declaration.natureOfTransaction.natureType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.natureOfTransaction.map(_.natureType)
      override val pageLink1Param: Option[Call] = Some(NatureOfTransactionController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => msgs(s"declaration.summary.transaction.natureOfTransaction.$code"))

      override val amendKey: Option[String] = Some("declaration.summary.transaction.natureOfTransaction")
    },
    "declaration.parties.additionalActors" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.declarationAdditionalActorsData.map(_.actors.size.toString)
      override val pageLink1Param: Option[Call] = Some(AdditionalActorsSummaryController.displayPage)
    },
    "declaration.parties.additionalActors.actors.$" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.declarationAdditionalActorsData.map(_.actors.size.toString)
      override val pageLink1Param: Option[Call] = Some(AdditionalActorsSummaryController.displayPage)
    },
    "declaration.parties.additionalActors.actors.$.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.declarationAdditionalActorsData.flatMap(_.actors.lift(args(0)).flatMap(_.eori.map(_.value)))
      override val pageLink1Param: Option[Call] = Some(AdditionalActorsSummaryController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.actors.eori")
    },
    "declaration.parties.additionalActors.actors.$.type" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.declarationAdditionalActorsData.flatMap(_.actors.lift(args(0)).map(_.partyType.getOrElse("")))
      override val pageLink1Param: Option[Call] = Some(AdditionalActorsSummaryController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.actors.type")
    },
    "declaration.parties.consigneeDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignee.address.fullName")
    },
    "declaration.parties.consigneeDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignee.address.townOrCity")
    },
    "declaration.parties.consigneeDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignee.address.country")
    },
    "declaration.parties.consigneeDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consigneeDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignee.address.addressLine")
    },
    "declaration.parties.consigneeDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consigneeDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(ConsigneeDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignee.address.postCode")
    },
    "declaration.parties.consignorDetails.eori" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.eori.map(_.value))
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignor.eori")
    },
    "declaration.parties.consignorDetails.details.address.fullName" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consignorDetails.flatMap(_.details.address.map(_.fullName))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignor.address.fullName")
    },
    "declaration.parties.consignorDetails.details.address.townOrCity" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.townOrCity))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignor.address.townOrCity")
    },
    "declaration.parties.consignorDetails.details.address.country" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consignorDetails.flatMap(_.details.address.map(_.country))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).flatMap(countryHelper.getShortNameForCountryCode)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignor.address.country")
    },
    "declaration.parties.consignorDetails.details.address.addressLine" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.parties.consignorDetails.flatMap(_.details.address.map(_.addressLine))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignor.address.addressLine")
    },
    "declaration.parties.consignorDetails.details.address.postCode" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.parties.consignorDetails.flatMap(_.details.address.map(_.postCode))
      override val pageLink1Param: Option[Call] = Some(ConsignorDetailsController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.parties.consignor.address.postCode")
    },
    "declaration.transport.meansOfTransportOnDepartureIDNumber" -> meansOfTransportOnDepartureIDNumber,
    "declaration.departureTransport.meansOfTransportOnDepartureIDNumber" -> meansOfTransportOnDepartureIDNumber,
    "declaration.locations.inlandModeOfTransportCode.inlandModeOfTransportCode" -> inlandModeOfTransportCode,
    "declaration.departureTransport.borderModeOfTransportCode" -> inlandModeOfTransportCode,
    "declaration.transport.meansOfTransportOnDepartureType" -> meansOfTransportOnDepartureType,
    "declaration.departureTransport.meansOfTransportOnDepartureType" -> meansOfTransportOnDepartureType,
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
    "declaration.transport.containers.$.id" -> containers,
    "declaration.containers.container.$.id" -> containers,
    "declaration.transport.containers.$.seals.seal.$.id" -> seals,
    "declaration.containers.container.$.seals.seal.$.id" -> seals,
    "declaration.transport.containers.$.seals.ids" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.containers.lift(args(0)).map(_.seals.map(_.id).mkString(", "))
      override val amendKey: Option[String] = Some("declaration.summary.container.securitySeals")
    },
    "declaration.previousDocuments.$.documentCategory" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = Option.empty[String]
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.previousDocuments.$.documentReference" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).map(_.documentReference)
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transaction.previousDocuments.reference")
    },
    "declaration.previousDocuments.$.documentType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).map(_.documentType)
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
      override def fetchReadableValue(
        dec: ExportsDeclaration,
        args: Int*
      )(implicit msgs: Messages, countryHelper: CountryHelper, codeListConnector: CodeListConnector): Option[String] =
        fetchRawValue(dec, args: _*).map(code => DocumentTypeService.findByCode(codeListConnector, code).asText)
      override val amendKey: Option[String] = Some("declaration.summary.transaction.previousDocuments.type")
    },
    "declaration.previousDocuments.$.goodsItemIdentifier" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = getPreviousDocument(dec, args(0)).flatMap(_.goodsItemIdentifier)
      override val pageLink1Param: Option[Call] = Some(PreviousDocumentsSummaryController.displayPage)
    },
    "declaration.locations.warehouseIdentification.identificationNumber" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
      override val pageLink1Param: Option[Call] = Some(WarehouseIdentificationController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transport.warehouse.id")
    },
    "declaration.locations.warehouseIdentification.identificationType" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] = dec.locations.warehouseIdentification.flatMap(_.identificationNumber)
      override val pageLink1Param: Option[Call] = Some(WarehouseIdentificationController.displayPage)
    },
    "declaration.locations.warehouseIdentification.supervisingCustomsOffice" -> new DefaultPointerRecord() {
      def fetchRawValue(dec: ExportsDeclaration, args: Int*): Option[String] =
        dec.locations.supervisingCustomsOffice.flatMap(_.supervisingCustomsOffice)
      override val pageLink1Param: Option[Call] = Some(SupervisingCustomsOfficeController.displayPage)
      override val amendKey: Option[String] = Some("declaration.summary.transport.supervisingOffice")
    }
  )
  // scalastyle:on
}
