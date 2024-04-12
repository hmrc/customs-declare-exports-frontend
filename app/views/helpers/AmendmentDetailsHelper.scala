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

import forms.common.{Address, Eori}
import forms.declaration.CommodityDetails.{combinedNomenclatureCodePointer, descriptionOfGoodsPointer}
import forms.declaration.Document.documentTypePointer
import forms.declaration.PackageInformation._
import forms.declaration._
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.carrier.CarrierDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.countries.Country
import forms.declaration.exporter.ExporterDetails
import forms.declaration.officeOfExit.OfficeOfExit
import models.AmendmentRow._
import models.ExportsFieldPointer.ExportsFieldPointer
import models.declaration.CommodityMeasure._
import models.declaration.InvoiceAndPackageTotals._
import models.declaration.Locations.{destinationCountryPointer, routingCountriesPointer}
import models.declaration.Parties.partiesPrefix
import models.declaration.ProcedureCodesData.{additionalProcedureCodesPointer, procedureCodesPointer}
import models.declaration._
import models.{Amendment, AmendmentOp, ExportsDeclaration}
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.Html
import services.DiffTools.ExportsDeclarationDiff
import services._
import views.helpers.AmendmentDetailsHelper._
import views.helpers.summary.SummaryHelper.classes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import javax.inject.{Inject, Singleton}

@Singleton
class AmendmentDetailsHelper @Inject() (
  countryHelper: CountryHelper,
  documentTypeService: DocumentTypeService,
  packageTypesService: PackageTypesService
) extends Logging {

  def dateOfAmendment(timestamp: ZonedDateTime)(implicit messages: Messages): Html =
    Html(s"""
         |<h2 class="govuk-heading-s govuk-!-margin-top-6 govuk-!-margin-bottom-0">${messages("amendment.details.last.updated")}</h2>
         |<time class="govuk-body date-of-amendment" datetime="${ISO_OFFSET_DATE_TIME.format(timestamp)}">
         |  ${ViewDates.formatDateAtTime(timestamp)}
         |</time>
         |""".stripMargin)

  def reasonForAmend(reason: String)(implicit messages: Messages): Html =
    Html(s"""
         |<h2 class="govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-0">${messages("amendment.details.reason.amendment")}</h2>
         |<span class="govuk-body reason-of-amendment">$reason</span>
         |""".stripMargin)

  def headingOfAmendments(implicit messages: Messages): Html =
    Html(s"""
         |<h2 class="govuk-heading-s govuk-!-margin-top-4 govuk-!-margin-bottom-3">${messages("amendment.details.heading.lists")}</h2>
         |""".stripMargin)

  private case class Section(id: String, alteredFields: Seq[AlteredField], sequenceId: String = "")

  def amendments(differences: ExportsDeclarationDiff)(implicit messages: Messages): Html =
    new Html(
      (section(parties, differences) ++
        sectionRoutesAndLocations(differences) ++
        sectionTransaction(differences) ++
        sectionItems(differences) ++
        sectionTransport(differences))
        .filterNot(_.alteredFields.isEmpty)
        .zipWithIndex
        .map { case (section, index) => cardOfAmendments(section, index == 0) }
    )

  private def section(sectionId: String, differences: ExportsDeclarationDiff): Seq[Section] =
    List(Section(sectionId, differences.filter(_.fieldPointer.startsWith(sectionId))))

  private def sectionItems(differences: ExportsDeclarationDiff)(implicit messages: Messages): Seq[Section] = {
    def reducePointers(alteredFields: Seq[AlteredField]): Seq[AlteredField] =
      alteredFields.map(af => packageTypeToUserValue(af, af.fieldPointer.replace(items, "item")))

    val itemSections = differences
      .filter(_.fieldPointer.startsWith(items))
      .groupBy(_.fieldPointer.split("\\.#?")(2))
      .toList
      .sortBy(_._1)
      .map { itemDiffs =>
        Section(items, reducePointers(itemDiffs._2), itemDiffs._1.split('.').head)
      }
    itemSections
  }

  private lazy val routesAndLocationsIds = List(destinationCountryPointer, routingCountriesPointer, GoodsLocation.pointer, OfficeOfExit.pointerBase)

  private def sectionRoutesAndLocations(differences: ExportsDeclarationDiff)(implicit messages: Messages): Seq[Section] = {
    val alteredFields =
      differences
        .filter(_.fieldPointer.startsWith(locations))
        .filter { difference =>
          val parts = difference.fieldPointer.split('.')
          parts.length > 2 && routesAndLocationsIds.contains(parts(2))
        }
        .map(countryToUserValue)

    List(Section(locations, alteredFields))
  }

  private lazy val transactionIds = List(InvoiceAndPackageTotals.pointer, NatureOfTransaction.pointerBase, PreviousDocumentsData.pointer)

  private def sectionTransaction(differences: ExportsDeclarationDiff)(implicit messages: Messages): Seq[Section] = {
    val alteredFields =
      differences.filter { difference =>
        val parts = difference.fieldPointer.split('.')
        parts.length > 1 && transactionIds.contains(parts(1))
      }
        .map(af => if (af.fieldPointer.startsWith(previousDocuments)) documentTypeToUserValue(af) else af)

    List(Section(transaction, alteredFields))
  }

  private lazy val transportIds = List(inlandModeOfTransport, supervisingCustomsOffice, warehouseIdentification)

  private def sectionTransport(differences: ExportsDeclarationDiff)(implicit messages: Messages): Seq[Section] = {
    val alteredFields =
      differences
        .filter(af => af.fieldPointer.startsWith(transport) || transportIds.exists(af.fieldPointer.startsWith))
        .map(countryToUserValue)

    List(Section(transport, alteredFields))
  }

  private def cardOfAmendments(section: Section, isFirstSection: Boolean)(implicit messages: Messages): Html =
    Html(s"""
         |<div class="govuk-summary-card${if (isFirstSection) " govuk-summary-card-margin-top-0" else ""}">
         |  <div class="govuk-summary-card__title-wrapper">
         |    <h2 class="govuk-summary-card__title ${classes(classMappings(section.id))}-card">
         |      ${messages(h2Mappings(section.id), section.sequenceId)}
         |    </h2>
         |  </div>
         |  <div class="govuk-summary-card__content">
         |    ${tableOfAmendments(section.alteredFields)}
         |  </div>
         |</div>
         |""".stripMargin)

  private def tableOfAmendments(alteredFields: Seq[AlteredField])(implicit messages: Messages): String =
    s"""
     |<table class="govuk-table">
     |  <thead class="govuk-table__head">
     |    <tr class="govuk-table__row">
     |      <th scope="col" class="govuk-table__header">${messages("amendment.details.description")}</th>
     |      <th scope="col" class="govuk-table__header">${messages("amendment.details.previous.value")}</th>
     |      <th scope="col" class="govuk-table__header">${messages("amendment.details.amended.value")}</th>
     |    </tr>
     |  </thead>
     |  <tbody class="govuk-table__body">
     |    ${alteredFields.map(handleDifference).mkString}
     |  </tbody>
     |</table>
     |""".stripMargin

  private def handleDifference(alteredField: AlteredField)(implicit messages: Messages): String =
    (alteredField.values.originalVal, alteredField.values.newVal) match {
      case (Some(oldValue), Some(newValue)) => valueAmended(oldValue, newValue, alteredField.fieldPointer)
      case (None, Some(newValue))           => valueAdded(newValue, alteredField.fieldPointer)
      case (Some(oldValue), None)           => valueRemoved(oldValue, alteredField.fieldPointer)
      case _ =>
        logger.warn(s"'AlteredField(${alteredField.fieldPointer}) with no value difference??")
        ""
    }

  private def valueAdded(newValue: Any, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    newValue match {
      case newVal: AmendmentOp => newVal.valueAdded(pointer)
      case _                   => forAddedValue(pointer, toFieldId(pointer), toUserValue(pointer, newValue))
    }

  private def valueAmended(oldValue: Any, newValue: Any, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    (oldValue, newValue) match {
      case (oldVal: Amendment, newVal: Amendment) => oldVal.valueAmended(newVal, pointer)
      case _ => forAmendedValue(pointer, toFieldId(pointer), toUserValue(pointer, oldValue), toUserValue(pointer, newValue))
    }

  private def valueRemoved(oldValue: Any, pointer: ExportsFieldPointer)(implicit messages: Messages): String =
    oldValue match {
      case oldVal: AmendmentOp => oldVal.valueRemoved(pointer)
      case _                   => forRemovedValue(pointer, toFieldId(pointer), toUserValue(pointer, oldValue))
    }

  private def toFieldId(pointer: ExportsFieldPointer)(implicit messages: Messages): String = {
    val key = pointer.replaceAll("\\.#[0-9]+\\.?", ".")
    messages(fieldIdMappings(if (key.endsWith(".")) key.dropRight(1) else key))
  }

  private def toUserValue(pointer: ExportsFieldPointer, value: Any)(implicit messages: Messages): String = {
    val key = pointer.replaceAll("\\.#[0-9]+\\.?", ".")
    valueMappings.get(if (key.endsWith(".")) key.dropRight(1) else key).fold(value.toString)(_.apply(value, messages))
  }

  private def countryToUserValue(af: AlteredField)(implicit messages: Messages): AlteredField = {
    def fetchCountry(countryCode: Option[String]): Option[String] =
      Some(countryCode.fold("")(countryHelper.getShortNameForCountryCode))

    def updateRoutingCountry(routingCountry: RoutingCountry): Option[RoutingCountry] =
      Some(routingCountry.copy(country = Country(fetchCountry(routingCountry.country.code))))

    val values = (af.values.originalVal, af.values.newVal) match {
      case (None, Some(country: Country)) => OriginalAndNewValues(None, Some(Country(fetchCountry(country.code))))
      case (Some(country: Country), None) => OriginalAndNewValues(Some(Country(fetchCountry(country.code))), None)

      case (None, Some(country: TransportCountry)) => OriginalAndNewValues(None, Some(TransportCountry(fetchCountry(country.countryCode))))
      case (Some(country: TransportCountry), None) => OriginalAndNewValues(Some(TransportCountry(fetchCountry(country.countryCode))), None)
      case (Some(oldCountry: TransportCountry), Some(newCountry: TransportCountry)) =>
        OriginalAndNewValues(
          Some(TransportCountry(fetchCountry(oldCountry.countryCode))),
          Some(TransportCountry(fetchCountry(newCountry.countryCode)))
        )

      case (None, Some(routingCountry: RoutingCountry)) => OriginalAndNewValues(None, updateRoutingCountry(routingCountry))
      case (Some(routingCountry: RoutingCountry), None) => OriginalAndNewValues(updateRoutingCountry(routingCountry), None)

      case (Some(oldCountry: Country), Some(newCountry: Country)) =>
        OriginalAndNewValues(Some(Country(fetchCountry(oldCountry.code))), Some(Country(fetchCountry(newCountry.code))))

      case _ => af.values
    }

    af.copy(values = values)
  }

  private def documentTypeToUserValue(af: AlteredField)(implicit messages: Messages): AlteredField =
    (af.values.originalVal, af.values.newVal) match {
      case (None, Some(document: Document)) =>
        val newDoc = document.copy(documentTypeService.findByCode(document.documentType).asText)
        af.copy(values = OriginalAndNewValues(None, Some(newDoc)))

      case (Some(document: Document), None) =>
        val newDoc = document.copy(documentTypeService.findByCode(document.documentType).asText)
        af.copy(values = OriginalAndNewValues(Some(newDoc), None))

      case (None, Some(value: String)) if af.fieldPointer.endsWith(documentTypePointer) =>
        af.copy(values = OriginalAndNewValues(None, Some(documentTypeService.findByCode(value).asText)))

      case (Some(value: String), None) if af.fieldPointer.endsWith(documentTypePointer) =>
        af.copy(values = OriginalAndNewValues(Some(documentTypeService.findByCode(value).asText), None))

      case (Some(oldValue: String), Some(newValue: String)) if af.fieldPointer.endsWith(documentTypePointer) =>
        af.copy(values =
          OriginalAndNewValues(Some(documentTypeService.findByCode(oldValue).asText), Some(documentTypeService.findByCode(newValue).asText))
        )

      case _ => af
    }

  private def packageTypeToUserValue(af: AlteredField, pointer: String)(implicit messages: Messages): AlteredField =
    (af.values.originalVal, af.values.newVal) match {
      case (None, Some(packageInfo: PackageInformation)) =>
        packageInfo.typesOfPackages.fold(af) { code =>
          val newPackageInfo = packageInfo.copy(typesOfPackages = Some(packageTypesService.findByCode(code).asText))
          af.copy(pointer, OriginalAndNewValues(None, Some(newPackageInfo)))
        }

      case (Some(packageInfo: PackageInformation), None) =>
        packageInfo.typesOfPackages.fold(af) { code =>
          val newPackageInfo = packageInfo.copy(typesOfPackages = Some(packageTypesService.findByCode(code).asText))
          af.copy(pointer, OriginalAndNewValues(Some(newPackageInfo), None))
        }

      case (None, Some(value: String)) if af.fieldPointer.endsWith(typesOfPackagesPointer) =>
        af.copy(pointer, OriginalAndNewValues(None, Some(packageTypesService.findByCode(value).asText)))

      case (Some(value: String), None) if af.fieldPointer.endsWith(typesOfPackagesPointer) =>
        af.copy(pointer, OriginalAndNewValues(Some(packageTypesService.findByCode(value).asText), None))

      case (Some(oldValue: String), Some(newValue: String)) if af.fieldPointer.endsWith(typesOfPackagesPointer) =>
        af.copy(
          pointer,
          OriginalAndNewValues(Some(packageTypesService.findByCode(oldValue).asText), Some(packageTypesService.findByCode(newValue).asText))
        )

      case _ => af.copy(pointer)
    }
}

object AmendmentDetailsHelper {

  private val summary = "declaration.summary"

  private val declaration = ExportsDeclaration.pointer
  private val items = s"$declaration.${ExportItem.pointer}"
  private val locations = s"$declaration.${Locations.pointer}"
  private val parties = s"$declaration.${Parties.pointer}"
  private val transaction = "transaction"
  private val transport = s"$declaration.${Transport.pointer}"

  private val inlandModeOfTransport = s"$locations.${InlandModeOfTransportCode.pointer}"
  private val previousDocuments = s"$declaration.${PreviousDocumentsData.pointer}"
  private val supervisingCustomsOffice = s"$locations.${SupervisingCustomsOffice.pointer}"
  private val warehouseIdentification = s"$locations.${WarehouseIdentification.pointer}"

  private def address(key: String): String = s"$parties.$key.${Address.pointer}"
  private def addressMsg(key: String): String = s"$partiesPrefix.$key.address"

  private val carrier = s"$parties.${CarrierDetails.pointer}"
  private val consignee = s"$parties.${ConsigneeDetails.pointer}"
  private val consignor = s"$parties.${ConsignorDetails.pointer}"
  private val declarant = s"$parties.${DeclarantDetails.pointer}"
  private val exporter = s"$parties.${ExporterDetails.pointer}"
  private val representative = s"$parties.${RepresentativeDetails.pointer}"

  private val totals = s"$declaration.${InvoiceAndPackageTotals.pointer}"

  private val h2Mappings = Map(
    parties -> s"$summary.section.2",
    locations -> s"$summary.section.3",
    transaction -> s"$summary.section.4",
    items -> s"$summary.section.5.item",
    transport -> s"$summary.section.6"
  )

  private val classMappings = Map(parties -> 1, locations -> 2, transaction -> 3, items -> 4, transport -> 5)

  private val fieldIdMappings: Map[String, String] = Map(
    s"$carrier.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.carrier.eori",
    s"${address(CarrierDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("carrier")}.fullName",
    s"${address(CarrierDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("carrier")}.addressLine",
    s"${address(CarrierDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("carrier")}.townOrCity",
    s"${address(CarrierDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("carrier")}.postCode",
    s"${address(CarrierDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("carrier")}.country",
    s"$consignee.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.consignee.eori",
    s"${address(ConsigneeDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("consignee")}.fullName",
    s"${address(ConsigneeDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("consignee")}.addressLine",
    s"${address(ConsigneeDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("consignee")}.townOrCity",
    s"${address(ConsigneeDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("consignee")}.postCode",
    s"${address(ConsigneeDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("consignee")}.country",
    s"$consignor.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.consignor.eori",
    s"${address(ConsignorDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("consignor")}.fullName",
    s"${address(ConsignorDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("consignor")}.addressLine",
    s"${address(ConsignorDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("consignor")}.townOrCity",
    s"${address(ConsignorDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("consignor")}.postCode",
    s"${address(ConsignorDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("consignor")}.country",
    s"$declarant.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.declarant.eori",
    s"${address(DeclarantDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("declarant")}.fullName",
    s"${address(DeclarantDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("declarant")}.addressLine",
    s"${address(DeclarantDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("declarant")}.townOrCity",
    s"${address(DeclarantDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("declarant")}.postCode",
    s"${address(DeclarantDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("declarant")}.country",
    s"$exporter.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.exporter.eori",
    s"${address(ExporterDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("exporter")}.fullName",
    s"${address(ExporterDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("exporter")}.addressLine",
    s"${address(ExporterDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("exporter")}.townOrCity",
    s"${address(ExporterDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("exporter")}.postCode",
    s"${address(ExporterDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("exporter")}.country",
    s"$representative.${RepresentativeDetails.statusCodePointer}" -> s"$partiesPrefix.representative.type",
    s"$representative.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.representative.eori",
    s"${address(RepresentativeDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("representative")}.fullName",
    s"${address(RepresentativeDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("representative")}.addressLine",
    s"${address(RepresentativeDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("representative")}.townOrCity",
    s"${address(RepresentativeDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("representative")}.postCode",
    s"${address(RepresentativeDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("representative")}.country",
    AdditionalActors.eoriPointerForAmend -> AdditionalActor.keyForEori,
    AdditionalActors.partyTypePointerForAmend -> AdditionalActor.keyForPartyType,
    AuthorisationHolders.eoriPointerForAmend -> AuthorisationHolder.keyForEori,
    AuthorisationHolders.typeCodePointerForAmend -> AuthorisationHolder.keyForTypeCode,
    s"$parties.${PersonPresentingGoodsDetails.pointer}.${Eori.pointer}" -> s"$summary.parties.personPresentingGoods",
    s"$totals.$totalAmountInvoicedPointer" -> s"$summary.transaction.itemAmount",
    s"$totals.$totalAmountInvoicedCurrencyPointer" -> s"$summary.transaction.currencyCode",
    s"$totals.$exchangeRatePointer" -> s"$summary.transaction.exchangeRate",
    s"$totals.$totalPackagePointer" -> s"$summary.transaction.totalNoOfPackages",
    s"$previousDocuments.${Document.pointer}.${Document.documentTypePointer}" -> Document.keyForType,
    s"$previousDocuments.${Document.pointer}.${Document.documentReferencePointer}" -> Document.keyForReference,
    s"$previousDocuments.${Document.pointer}.${Document.goodsItemIdentifierPointer}" -> Document.keyForItemNumber,
    s"$transport.${Transport.transportOnDeparturePointer}" -> s"$summary.transport.departure.meansOfTransport.type",
    s"$transport.${Transport.transportOnDepartureIdPointer}" -> s"$summary.transport.departure.meansOfTransport.id",
    s"$transport.${Transport.transportCrossingTheBorderPointer}" -> s"$summary.transport.border.meansOfTransport.type",
    s"$transport.${Transport.transportCrossingTheBorderIdPointer}" -> s"$summary.transport.border.meansOfTransport.id",
    s"$transport.${Container.pointer}.${Container.idPointer}" -> Container.keyForContainerId,
    s"$transport.${Container.pointer}.${Seal.pointer}" -> Seal.keyForAmend,
    s"item.${ProcedureCodesData.pointer}.$procedureCodesPointer" -> ProcedureCodesData.keyForPC,
    s"item.${ProcedureCodesData.pointer}.$additionalProcedureCodesPointer" -> ProcedureCodesData.keyForAPC,
    s"item.${CommodityDetails.pointer}.$combinedNomenclatureCodePointer" -> CommodityDetails.keyForCode,
    s"item.${CommodityDetails.pointer}.$descriptionOfGoodsPointer" -> CommodityDetails.keyForDescription,
    s"item.${PackageInformation.pointer}.$numberOfPackagesPointer" -> keyForNumberOfPackages,
    s"item.${PackageInformation.pointer}.$shippingMarksPointer" -> keyForShippingMarksPointer,
    s"item.${PackageInformation.pointer}.$typesOfPackagesPointer" -> keyForTypesOfPackages,
    s"item.${CommodityMeasure.pointer}.$grossMassPointer" -> keyForGrossMass,
    s"item.${CommodityMeasure.pointer}.$netMassPointer" -> keyForNetMass,
    s"item.${CommodityMeasure.pointer}.$supplementaryUnitsPointer" -> keyForSupplementaryUnits,
    AdditionalInformationData.codePointerForAmend -> AdditionalInformation.keyForCode,
    AdditionalInformationData.descriptionPointerForAmend -> AdditionalInformation.keyForDescription,
    AdditionalDocuments.dateOfValidityPointerForAmend -> AdditionalDocument.keyForDateOfValidity,
    AdditionalDocuments.documentQuantityPointerForAmend -> DocumentWriteOff.keyForDocumentQuantity,
    AdditionalDocuments.identifierPointerForAmend -> AdditionalDocument.keyForIdentifier,
    AdditionalDocuments.issuingAuthorityNamePointerForAmend -> AdditionalDocument.keyForIssuingAuthorityName,
    AdditionalDocuments.measurementUnitPointerForAmend -> DocumentWriteOff.keyForMeasurementUnit,
    AdditionalDocuments.statusPointerForAmend -> AdditionalDocument.keyForStatus,
    AdditionalDocuments.statusReasonPointerForAmend -> AdditionalDocument.keyForStatusReason,
    AdditionalDocuments.typeCodePointerForAmend -> AdditionalDocument.keyForTypeCode
  )

  private val valueMappings: Map[String, (Any, Messages) => String] = Map(
    AdditionalActors.partyTypePointerForAmend -> ((v: Any, messages: Messages) => safeMessage(s"$summary.parties.actors.$v", v)(messages)),
    s"$representative.${RepresentativeDetails.statusCodePointer}" -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.parties.representative.type.$v", v)(messages)
    ),
    s"$transport.${Transport.transportOnDeparturePointer}" -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.transport.departure.meansOfTransport.$v", v)(messages)
    ),
    s"$transport.${Transport.transportCrossingTheBorderPointer}" -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.transport.border.meansOfTransport.$v", v)(messages)
    )
  )
}
