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

import controllers.helpers.AmendmentInstance
import forms.section4.{NatureOfTransaction, PreviousDocumentsData}
import forms.section6._
import models.ExportsDeclaration
import models.declaration._
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.Html
import views.helpers.AmendmentDetailsHelper._
import views.helpers.summary.SummaryHelper.classes

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class AmendmentDetailsHelper extends Logging {

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

  private case class Section(id: String, amendmentFields: Seq[AmendmentInstance], sequenceId: String = "")

  def amendments(amendmentRows: Seq[AmendmentInstance])(implicit messages: Messages): Html =
    new Html(
      (sectionParties(amendmentRows) ++
        sectionRoutesAndLocations(amendmentRows) ++
        sectionTransaction(amendmentRows) ++
        sectionItems(amendmentRows) ++
        sectionTransport(amendmentRows))
        .filterNot(_.amendmentFields.isEmpty)
        .zipWithIndex
        .map { case (section, index) => cardOfAmendments(section, index == 0) }
    )

  private def section(sectionId: String, amendmentRows: Seq[AmendmentInstance]): Seq[Section] =
    List(Section(sectionId, amendmentRows.filter(_.pointer.pattern.startsWith(sectionId))))

  private def sectionParties(amendmentRows: Seq[AmendmentInstance]): Seq[Section] =
    section(parties, amendmentRows)

  private def sectionItems(amendmentRows: Seq[AmendmentInstance]): Seq[Section] =
    amendmentRows
      .filter(_.pointer.pattern.startsWith(items))
      .groupBy(_.pointer.sequenceArgs(0))
      .toList
      .sortBy(_._1)
      .map { itemDiffs =>
        Section(items, itemDiffs._2, itemDiffs._1.split('.').head)
      }

  // private lazy val routesAndLocationsIds = List(destinationCountryPointer, routingCountriesPointer, GoodsLocation.pointer, OfficeOfExit.pointerBase)

  private def sectionRoutesAndLocations(amendmentRows: Seq[AmendmentInstance]): Seq[Section] = {
    amendmentRows.foreach(println)

    val alteredFields =
      amendmentRows
        .filter{ai =>
          val pattern = ai.pointer.pattern
          pattern.startsWith(locations) && !(pattern.contains("inlandModeOfTransportCode") || pattern.contains("warehouseIdentification"))
        }
        .filter { instance =>
          instance.originalValue.isDefined || instance.amendedValue.isDefined
        }

    List(Section(locations, alteredFields))
  }

  private lazy val transactionIds = List(InvoiceAndPackageTotals.pointer, NatureOfTransaction.pointerBase, PreviousDocumentsData.pointer)

  private def sectionTransaction(amendmentRows: Seq[AmendmentInstance]): Seq[Section] = {
    val alteredFields =
      amendmentRows.filter { instance =>
        val parts = instance.pointer.pattern.split('.')
        parts.length > 1 && transactionIds.contains(parts(1))
      }

    List(Section(transaction, alteredFields))
  }

  private lazy val transportIds = List(inlandModeOfTransport, supervisingCustomsOffice, warehouseIdentification)

  private def sectionTransport(amendmentRows: Seq[AmendmentInstance]): Seq[Section] = {
    val alteredFields =
      amendmentRows
        .filter(af => af.pointer.pattern.startsWith(transport) || transportIds.exists(af.pointer.pattern.startsWith))

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
         |    ${tableOfAmendments(section.amendmentFields)}
         |  </div>
         |</div>
         |""".stripMargin)

  private def tableOfAmendments(alteredFields: Seq[AmendmentInstance])(implicit messages: Messages): String =
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
       |    ${alteredFields.map(getIndividualRow).mkString}
       |  </tbody>
       |</table>
       |""".stripMargin

  private def getIndividualRow(amendmentInstance: AmendmentInstance)(implicit messages: Messages): String =
    s"""<tr class="govuk-table__row ${amendmentInstance.pointer.pattern.replaceAll("\\.#?", "-")}">
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${messages(amendmentInstance.fieldId)}</th>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${amendmentInstance.originalValue.getOrElse("-")}</td>
       |  <td class="govuk-table__cell govuk-table__cell_break-word">${amendmentInstance.amendedValue.getOrElse("-")}</td>
       |</tr>""".stripMargin
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
  private val supervisingCustomsOffice = s"$locations.${SupervisingCustomsOffice.pointer}"
  private val warehouseIdentification = s"$locations.${WarehouseIdentification.pointer}"

  private val h2Mappings = Map(
    parties -> s"$summary.section.2",
    locations -> s"$summary.section.3",
    transaction -> s"$summary.section.4",
    items -> s"$summary.section.5.item",
    transport -> s"$summary.section.6"
  )

  private val classMappings = Map(parties -> 1, locations -> 2, transaction -> 3, items -> 4, transport -> 5)

//  private val fieldIdMappings: Map[String, String] = Map(
//    s"$carrier.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.carrier.eori",
//    s"${address(CarrierDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("carrier")}.fullName",
//    s"${address(CarrierDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("carrier")}.addressLine",
//    s"${address(CarrierDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("carrier")}.townOrCity",
//    s"${address(CarrierDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("carrier")}.postCode",
//    s"${address(CarrierDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("carrier")}.country",
//    s"$consignee.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.consignee.eori",
//    s"${address(ConsigneeDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("consignee")}.fullName",
//    s"${address(ConsigneeDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("consignee")}.addressLine",
//    s"${address(ConsigneeDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("consignee")}.townOrCity",
//    s"${address(ConsigneeDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("consignee")}.postCode",
//    s"${address(ConsigneeDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("consignee")}.country",
//    s"$consignor.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.consignor.eori",
//    s"${address(ConsignorDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("consignor")}.fullName",
//    s"${address(ConsignorDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("consignor")}.addressLine",
//    s"${address(ConsignorDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("consignor")}.townOrCity",
//    s"${address(ConsignorDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("consignor")}.postCode",
//    s"${address(ConsignorDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("consignor")}.country",
//    s"$declarant.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.declarant.eori",
//    s"${address(DeclarantDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("declarant")}.fullName",
//    s"${address(DeclarantDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("declarant")}.addressLine",
//    s"${address(DeclarantDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("declarant")}.townOrCity",
//    s"${address(DeclarantDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("declarant")}.postCode",
//    s"${address(DeclarantDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("declarant")}.country",
//    s"$exporter.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.exporter.eori",
//    s"${address(ExporterDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("exporter")}.fullName",
//    s"${address(ExporterDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("exporter")}.addressLine",
//    s"${address(ExporterDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("exporter")}.townOrCity",
//    s"${address(ExporterDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("exporter")}.postCode",
//    s"${address(ExporterDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("exporter")}.country",
//    s"$representative.${RepresentativeDetails.statusCodePointer}" -> s"$partiesPrefix.representative.type",
//    s"$representative.${EntityDetails.eoriPointer}" -> s"$partiesPrefix.representative.eori",
//    s"${address(RepresentativeDetails.pointer)}.${Address.fullNamePointer}" -> s"${addressMsg("representative")}.fullName",
//    s"${address(RepresentativeDetails.pointer)}.${Address.addressLinePointer}" -> s"${addressMsg("representative")}.addressLine",
//    s"${address(RepresentativeDetails.pointer)}.${Address.townOrCityPointer}" -> s"${addressMsg("representative")}.townOrCity",
//    s"${address(RepresentativeDetails.pointer)}.${Address.postCodePointer}" -> s"${addressMsg("representative")}.postCode",
//    s"${address(RepresentativeDetails.pointer)}.${Address.countryPointer}" -> s"${addressMsg("representative")}.country",
//    AdditionalActors.eoriPointerForAmend -> AdditionalActor.keyForEori,
//    AdditionalActors.partyTypePointerForAmend -> AdditionalActor.keyForPartyType,
//    AuthorisationHolders.eoriPointerForAmend -> AuthorisationHolder.keyForEori,
//    AuthorisationHolders.typeCodePointerForAmend -> AuthorisationHolder.keyForTypeCode,
  // ****s"$parties.${PersonPresentingGoodsDetails.pointer}.${Eori.pointer}" -> s"$summary.parties.personPresentingGoods",
//    s"$totals.$totalAmountInvoicedPointer" -> s"$summary.transaction.itemAmount",
//    s"$totals.$totalAmountInvoicedCurrencyPointer" -> s"$summary.transaction.currencyCode",
//    s"$totals.$exchangeRatePointer" -> s"$summary.transaction.exchangeRate",
//    s"$totals.$totalPackagePointer" -> s"$summary.transaction.totalNoOfPackages",
//    s"$previousDocuments.${Document.pointer}.${Document.documentTypePointer}" -> Document.keyForType,
//    s"$previousDocuments.${Document.pointer}.${Document.documentReferencePointer}" -> Document.keyForReference,
//    s"$previousDocuments.${Document.pointer}.${Document.goodsItemIdentifierPointer}" -> Document.keyForItemNumber,
//    s"$transport.${TransportPayment.pointer}" -> TransportPayment.keyForAmend,
//    s"$transport.${Transport.transportOnDeparturePointer}" -> s"$summary.transport.departure.meansOfTransport.type",
//    s"$transport.${Transport.transportOnDepartureIdPointer}" -> s"$summary.transport.departure.meansOfTransport.id",
//    s"$transport.${Transport.transportCrossingTheBorderPointer}" -> s"$summary.transport.border.meansOfTransport.type",
//    s"$transport.${Transport.transportCrossingTheBorderIdPointer}" -> s"$summary.transport.border.meansOfTransport.id",
//    s"$transport.${Container.pointer}" -> Container.keyForAmend,
//    s"$transport.${Container.pointer}.${Container.idPointer}" -> Container.keyForContainerId,
//    s"$transport.${Container.pointer}.${Seal.pointer}" -> Seal.keyForAmend,
//    s"$transport.${Transport.expressConsignmentPointer}" -> s"$summary.transport.expressConsignment",
  //  s"item.${ProcedureCodesData.pointer}.$procedureCodesPointer" -> ProcedureCodesData.keyForPC,
//    s"item.${ProcedureCodesData.pointer}.$additionalProcedureCodesPointer" -> ProcedureCodesData.keyForAPC,
//    s"item.${CommodityDetails.pointer}.$combinedNomenclatureCodePointer" -> CommodityDetails.keyForCode,
//    s"item.${CommodityDetails.pointer}.$descriptionOfGoodsPointer" -> CommodityDetails.keyForDescription,
//    s"item.${PackageInformation.pointer}.$numberOfPackagesPointer" -> keyForNumberOfPackages,
//    s"item.${PackageInformation.pointer}.$shippingMarksPointer" -> keyForShippingMarksPointer,
//    s"item.${PackageInformation.pointer}.$typesOfPackagesPointer" -> keyForTypesOfPackages,
//    s"item.${CommodityMeasure.pointer}.$grossMassPointer" -> keyForGrossMass,
//    s"item.${CommodityMeasure.pointer}.$netMassPointer" -> keyForNetMass,
//    s"item.${CommodityMeasure.pointer}.$supplementaryUnitsPointer" -> keyForSupplementaryUnits,
//    AdditionalInformationData.codePointerForAmend -> AdditionalInformation.keyForCode,
//    AdditionalInformationData.descriptionPointerForAmend -> AdditionalInformation.keyForDescription,
//    AdditionalDocuments.dateOfValidityPointerForAmend -> AdditionalDocument.keyForDateOfValidity,
//    AdditionalDocuments.documentQuantityPointerForAmend -> DocumentWriteOff.keyForDocumentQuantity,
//    AdditionalDocuments.identifierPointerForAmend -> AdditionalDocument.keyForIdentifier,
//    AdditionalDocuments.issuingAuthorityNamePointerForAmend -> AdditionalDocument.keyForIssuingAuthorityName,
//    AdditionalDocuments.measurementUnitPointerForAmend -> DocumentWriteOff.keyForMeasurementUnit,
//    AdditionalDocuments.statusPointerForAmend -> AdditionalDocument.keyForStatus,
//    AdditionalDocuments.statusReasonPointerForAmend -> AdditionalDocument.keyForStatusReason,
//    AdditionalDocuments.typeCodePointerForAmend -> AdditionalDocument.keyForTypeCode
//  )

  /*private val valueMappings: Map[String, (Any, Messages) => String] = Map(
    DeclarationAdditionalActorsData.partyTypePointerForAmend -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.parties.actors.$v", v)(messages)
      ),
    s"$representative.${RepresentativeDetails.statusCodePointer}" -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.parties.representative.type.$v", v)(messages)
      ),
    s"$transport.${Transport.transportOnDeparturePointer}" -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.transport.departure.meansOfTransport.$v", v)(messages)
      ),
    s"$transport.${Transport.transportCrossingTheBorderPointer}" -> ((v: Any, messages: Messages) =>
      safeMessage(s"$summary.transport.border.meansOfTransport.$v", v)(messages)
      )
  )*/
}
