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

package views.amendments

import base.{Injector, MockAuthAction}
import controllers.timeline.routes.DeclarationDetailsController
import controllers.helpers.AmendmentInstance
import models.Pointer
import models.declaration.submissions.RequestType.ExternalAmendmentRequest
import play.twirl.api.HtmlFormat.Appendable
import testdata.SubmissionsTestData.{action, submission}
import views.amendments.AmendmentDetailsViewSpec._
import views.common.UnitViewSpec
import views.helpers.{CommonMessages, ViewDates}
import views.html.amendments.amendment_details
import views.tags.ViewTest

case class Difference(
  amendmentRow: AmendmentInstance,
  expectedSection: String,
  expectedOldVal: String = "",
  expectedNewVal: String = "",
  expectedKeys: Seq[String]
)

case class MultiRowDifference(amendmentRow: AmendmentInstance, expectedSection: String, expectedKeysVals: Seq[(String, String)])

@ViewTest
class AmendmentDetailsViewSpec extends UnitViewSpec with CommonMessages with Injector with MockAuthAction {

  private val page = instanceOf[amendment_details]

  private val ducr = "ducr"
  private val reason = Some("Some reason")
  private val verifiedEmailRequest = buildVerifiedEmailRequest(request, exampleUser)

  def createView(differences: Seq[AmendmentInstance] = List.empty): Appendable =
    page(submission.uuid, ducr, reason, action, differences)(verifiedEmailRequest, messages)

  "AmendmentDetails page" should {
    val view = createView()

    "display 'Back' button that links to /submissions/:id/information" in {
      val backButton = view.getElementById("back-link")
      backButton must containMessage(backCaption)
      backButton must haveHref(DeclarationDetailsController.displayPage(submission.uuid).url)
    }

    "display the expected page title for user amendments" in {
      view.getElementsByTag("h1").text mustBe messages("amendment.details.title")
    }

    "display the expected page title for external amendments" in {
      val view = page(submission.uuid, ducr, reason, action.copy(requestType = ExternalAmendmentRequest), List.empty)(verifiedEmailRequest, messages)
      view.getElementsByTag("h1").text mustBe messages("amendment.details.title.external")
    }

    "display the expected DUCR header" in {
      view.getElementById("section-header").text mustBe messages("amendment.details.ducr.heading", ducr)
    }

    "display the date of the last update" in {
      view.getElementsByClass("govuk-heading-s").get(0).text mustBe messages("amendment.details.last.updated")
      view.getElementsByTag("time").text mustBe ViewDates.formatDateAtTime(action.requestTimestamp)
    }

    "display the amendment reason" in {
      view.getElementsByClass("govuk-heading-s").get(1).text mustBe messages("amendment.details.reason.amendment")
      view.getElementsByClass("reason-of-amendment").text mustBe reason.value
    }

    "display the heading before the amendments" in {
      view.getElementsByClass("govuk-heading-s").get(2).text mustBe messages("amendment.details.heading.lists")
    }

    "display the expected table structure" in {
      val cards = createView(List(amendments.head.amendmentRow)).getElementsByClass("govuk-summary-card")
      cards.size mustBe 1
      val card = cards.get(0)

      card.getElementsByTag("h2").text mustBe messages("declaration.summary.section.6")

      val table = card.getElementsByTag("table").get(0)
      val headers = table.getElementsByClass("govuk-table__header")
      headers.size() mustBe 3
      headers.get(0).text mustBe messages("amendment.details.description")
      headers.get(1).text mustBe messages("amendment.details.previous.value")
      headers.get(2).text mustBe messages("amendment.details.amended.value")
    }

    "display the expected amendment rows" in {
      amendments.foreach { difference =>
        withClue(s"testing $difference") {
          val card = createView(List(difference.amendmentRow)).getElementsByClass("govuk-summary-card").get(0)
          card.getElementsByTag("h2").text mustBe messages(h2(difference.expectedSection), "1")

          val tbody = card.getElementsByTag("tbody").get(0)

          val columns = tbody.getElementsByClass("govuk-table__cell")
          columns.size() mustBe 3

          columns.get(0).text mustBe messages(difference.expectedKeys.head, difference.expectedKeys.tail: _*)
          columns.get(1).text mustBe difference.expectedOldVal
          columns.get(2).text mustBe difference.expectedNewVal
        }
      }
    }
  }
}

object AmendmentDetailsViewSpec {

  private def amendment(pointer: String, expectedSection: String, expectedKeys: String*): Difference =
    Difference(AmendmentInstance(Pointer(pointer), expectedKeys.head, Some("old"), Some("new")), expectedSection, "old", "new", expectedKeys)
  private def addition(pointer: String, expectedSection: String, expectedKeys: String*): Difference =
    Difference(AmendmentInstance(Pointer(pointer), expectedKeys.head, None, Some("new")), expectedSection, "", "new", expectedKeys)

  private val items = "declaration.items.#1"
  private val locations = "declaration.locations"
  private val parties = "declaration.parties"
  private val routeOfGoods = "routeOfGoods"
  private val transaction = "transaction"
  private val transport = "declaration.transport"

  private val summary = "declaration.summary"
  private val container = "declaration.summary.container"

  private val item = s"$summary.item"

  private val h2 = Map(
    parties -> s"$summary.section.2",
    locations -> s"$summary.section.3",
    transaction -> s"$summary.section.4",
    items -> s"$summary.section.5.item",
    transport -> s"$summary.section.6"
  )

  private val keys = Map(
    items -> s"$summary.item",
    locations -> s"$summary.locations",
    parties -> s"$summary.parties",
    routeOfGoods -> s"$summary.countries",
    transaction -> s"$summary.transaction",
    transport -> s"$summary.transport"
  )

  // scalastyle:off
  lazy val amendments: List[Difference] = List(
    // =========================== Transport's fields
    amendment(s"$transport.expressConsignment", transport, s"${keys(transport)}.expressConsignment"),
    amendment(s"$transport.transportPayment.paymentMethod", transport, s"${keys(transport)}.payment"),
    addition(s"$transport.containers.#1", transport, s"${container}.information"),
    amendment(s"$transport.containers.#1.id", transport, s"${container}.id"),
    amendment(s"$transport.containers.#1.seals.#1", transport, s"${container}.securitySeal"),
    amendment(s"$transport.borderModeOfTransportCode.code", transport, s"${keys(transport)}.departure.transportCode.header"),
    amendment(s"$transport.meansOfTransportOnDepartureType", transport, s"${keys(transport)}.departure.meansOfTransport.type"),
    amendment(s"$transport.meansOfTransportOnDepartureIDNumber", transport, s"${keys(transport)}.departure.meansOfTransport.id"),
    amendment(s"$transport.meansOfTransportCrossingTheBorderNationality", transport, s"${keys(transport)}.registrationCountry"),
    amendment(s"$transport.meansOfTransportCrossingTheBorderType", transport, s"${keys(transport)}.border.meansOfTransport.type"),
    amendment(s"$transport.meansOfTransportCrossingTheBorderIDNumber", transport, s"${keys(transport)}.border.meansOfTransport.id"),

    // =========================== Parties' fields
    addition(s"$parties.exporterDetails", parties, s"${keys(parties)}.exporter.eori"),
    addition(s"$parties.exporterDetails", parties, s"${keys(parties)}.exporter.address"),
    amendment(s"$parties.exporterDetails.eori", parties, s"${keys(parties)}.exporter.eori"),
    addition(s"$parties.exporterDetails.address", parties, s"${keys(parties)}.exporter.address"),
    amendment(s"$parties.exporterDetails.address.fullName", parties, s"${keys(parties)}.exporter.address.fullName"),
    amendment(s"$parties.exporterDetails.address.addressLine", parties, s"${keys(parties)}.exporter.address.addressLine"),
    amendment(s"$parties.exporterDetails.address.townOrCity", parties, s"${keys(parties)}.exporter.address.townOrCity"),
    amendment(s"$parties.exporterDetails.address.postCode", parties, s"${keys(parties)}.exporter.address.postCode"),
    amendment(s"$parties.exporterDetails.address.country", parties, s"${keys(parties)}.exporter.address.country"),
    addition(s"$parties.consigneeDetails", parties, s"${keys(parties)}.consignee.eori"),
    addition(s"$parties.consigneeDetails", parties, s"${keys(parties)}.consignee.address"),
    amendment(s"$parties.consigneeDetails.eori", parties, s"${keys(parties)}.consignee.eori"),
    addition(s"$parties.consigneeDetails.address", parties, s"${keys(parties)}.consignee.address"),
    amendment(s"$parties.consigneeDetails.address.fullName", parties, s"${keys(parties)}.consignee.address.fullName"),
    amendment(s"$parties.consigneeDetails.address.addressLine", parties, s"${keys(parties)}.consignee.address.addressLine"),
    amendment(s"$parties.consigneeDetails.address.townOrCity", parties, s"${keys(parties)}.consignee.address.townOrCity"),
    amendment(s"$parties.consigneeDetails.address.postCode", parties, s"${keys(parties)}.consignee.address.postCode"),
    amendment(s"$parties.consigneeDetails.address.country", parties, s"${keys(parties)}.consignee.address.country"),
    addition(s"$parties.consignorDetails", parties, s"${keys(parties)}.consignor.eori"),
    addition(s"$parties.consignorDetails", parties, s"${keys(parties)}.consignor.address"),
    amendment(s"$parties.consignorDetails.eori", parties, s"${keys(parties)}.consignor.eori"),
    addition(s"$parties.consignorDetails.address", parties, s"${keys(parties)}.consignor.address"),
    amendment(s"$parties.consignorDetails.address.fullName", parties, s"${keys(parties)}.consignor.address.fullName"),
    amendment(s"$parties.consignorDetails.address.addressLine", parties, s"${keys(parties)}.consignor.address.addressLine"),
    amendment(s"$parties.consignorDetails.address.townOrCity", parties, s"${keys(parties)}.consignor.address.townOrCity"),
    amendment(s"$parties.consignorDetails.address.postCode", parties, s"${keys(parties)}.consignor.address.postCode"),
    amendment(s"$parties.consignorDetails.address.country", parties, s"${keys(parties)}.consignor.address.country"),
    addition(s"$parties.declarantDetails", parties, s"${keys(parties)}.declarant.eori"),
    amendment(s"$parties.declarantDetails.eori", parties, s"${keys(parties)}.declarant.eori"),
    addition(s"$parties.representativeDetails", parties, s"${keys(parties)}.representative.eori"),
    amendment(s"$parties.representativeDetails.statusCode", parties, s"${keys(parties)}.representative.type"),
    amendment(s"$parties.representativeDetails.eori", parties, s"${keys(parties)}.representative.eori"),
    amendment(s"$parties.declarationAdditionalActorsData.actors.eori", parties, s"${keys(parties)}.actors.eori"),
    amendment(s"$parties.declarationAdditionalActorsData.actors.partyType", parties, s"${keys(parties)}.actors.type"),
    amendment(s"$parties.declarationHoldersData.holders.authorisationTypeCode", parties, s"${keys(parties)}.holders.holder.type"),
    amendment(s"$parties.declarationHoldersData.holders.eori", parties, s"${keys(parties)}.holders.holder.eori"),
    addition(s"$parties.carrierDetails", parties, s"${keys(parties)}.carrier.eori"),
    addition(s"$parties.carrierDetails", parties, s"${keys(parties)}.carrier.address"),
    amendment(s"$parties.carrierDetails.eori", parties, s"${keys(parties)}.carrier.eori"),
    addition(s"$parties.carrierDetails.address", parties, s"${keys(parties)}.carrier.address"),
    amendment(s"$parties.carrierDetails.address.fullName", parties, s"${keys(parties)}.carrier.address.fullName"),
    amendment(s"$parties.carrierDetails.address.addressLine", parties, s"${keys(parties)}.carrier.address.addressLine"),
    amendment(s"$parties.carrierDetails.address.townOrCity", parties, s"${keys(parties)}.carrier.address.townOrCity"),
    amendment(s"$parties.carrierDetails.address.postCode", parties, s"${keys(parties)}.carrier.address.postCode"),
    amendment(s"$parties.carrierDetails.address.country", parties, s"${keys(parties)}.carrier.address.country"),
    addition(s"$parties.personPresentingGoodsDetails", parties, s"${keys(parties)}.personPresentingGoods"),
    amendment(s"$parties.personPresentingGoodsDetails.eori", parties, s"${keys(parties)}.eidr"),
    addition(s"$parties.additionalActors.actors.#1.eori", parties, s"${keys(parties)}.actors.eori"),
    addition(s"$parties.additionalActors.actors.#1.type", parties, s"${keys(parties)}.actors.type"),

    // =========================== Locations' fields
    amendment(s"$locations.destinationCountries.countryOfDestination", locations, s"${keys(routeOfGoods)}.countryOfDestination"),
    addition(s"$locations.routingCountries.#1", locations, s"${keys(routeOfGoods)}.routingCountry"),
    amendment(s"$locations.routingCountries.#1", locations, s"${keys(routeOfGoods)}.routingCountry"),
    amendment(s"$locations.goodsLocation", locations, s"${keys(locations)}.goodsLocationCode"),
    amendment(s"$locations.officeOfExit.officeId", locations, s"${keys(locations)}.officeOfExit"),
    amendment(s"$locations.supervisingCustomsOffice", locations, s"${keys(transport)}.supervisingOffice"),
    amendment(s"$locations.warehouseIdentification.identificationNumber", transport, s"${keys(transport)}.warehouse.id"),
    amendment(s"$locations.inlandModeOfTransportCode.inlandModeOfTransportCode", transport, s"${keys(transport)}.inlandModeOfTransport"),

    // =========================== totalNumberOfItems' fields
    amendment("declaration.totalNumberOfItems.totalAmountInvoiced", transaction, s"${keys(transaction)}.itemAmount"),
    amendment("declaration.totalNumberOfItems.totalAmountInvoicedCurrency", transaction, s"${keys(transaction)}.currencyCode"),
    amendment("declaration.totalNumberOfItems.exchangeRate", transaction, s"${keys(transaction)}.exchangeRate"),
    amendment("declaration.totalNumberOfItems.totalPackage", transaction, s"${keys(transaction)}.totalNoOfPackages"),

    // =========================== natureOfTransaction
    amendment("declaration.natureOfTransaction.natureType", transaction, s"${keys(transaction)}.natureOfTransaction"),

    // =========================== previousDocuments' fields
    amendment("declaration.previousDocuments.documents.#1.documentType", transaction, s"${keys(transaction)}.previousDocuments.type"),
    amendment("declaration.previousDocuments.documents.#1.documentReference", transaction, s"${keys(transaction)}.previousDocuments.reference"),
    amendment(
      "declaration.previousDocuments.documents.#1.goodsItemIdentifier",
      transaction,
      s"${keys(transaction)}.previousDocuments.goodsItemIdentifier"
    ),

    // =========================== items' fields
    amendment(s"$items.procedureCodes.procedure.code", items, s"$item.procedureCode"),
    amendment(s"$items.procedureCodes.additionalProcedureCodes", items, s"$item.additionalProcedureCode"),
    amendment(s"$items.additionalFiscalReferencesData.references.#1", items, s"$item.VATdetails"),
    amendment(s"$items.statisticalValue.statisticalValue", items, s"$item.itemValue"),
    amendment(s"$items.commodityDetails.combinedNomenclatureCode", items, s"$item.commodityCode"),
    amendment(s"$items.commodityDetails.descriptionOfGoods", items, s"$item.goodsDescription"),
    amendment(s"$items.dangerousGoodsCode.dangerousGoodsCode", items, s"$item.unDangerousGoodsCode"),
    amendment(s"$items.cusCode", items, s"$item.cusCode"),
    amendment(s"$items.nactCode.#1", items, s"$item.nationalAdditionalCode"),
    amendment(s"$items.nactExemptionCode", items, s"$item.zeroRatedForVat"),
    amendment(s"$items.packageInformation.#1.typesOfPackages", items, s"$item.packageInformation.type"),
    amendment(s"$items.packageInformation.#1.numberOfPackages", items, s"$item.packageInformation.number"),
    amendment(s"$items.packageInformation.#1.shippingMarks", items, s"$item.packageInformation.markings"),
    amendment(s"$items.commodityMeasure.supplementaryUnits", items, s"$item.supplementaryUnits"),
    amendment(s"$items.commodityMeasure.grossMass", items, s"$item.grossWeight"),
    amendment(s"$items.commodityMeasure.netMass", items, s"$item.netWeight"),
    amendment(s"$items.additionalInformation.items.#1.code", items, s"$item.additionalInformation.code"),
    amendment(s"$items.additionalInformation.items.#1.description", items, s"$item.additionalInformation.description"),
    amendment(s"$items.additionalDocuments.documents.#1.documentTypeCode", items, s"$item.additionalDocuments.code"),
    amendment(s"$items.additionalDocuments.documents.#1.documentIdentifier", items, s"$item.additionalDocuments.identifier"),
    amendment(s"$items.additionalDocuments.documents.#1.documentStatus", items, s"$item.additionalDocuments.status"),
    amendment(s"$items.additionalDocuments.documents.#1.documentStatusReason", items, s"$item.additionalDocuments.statusReason"),
    amendment(s"$items.additionalDocuments.documents.#1.issuingAuthorityName", items, s"$item.additionalDocuments.issuingAuthorityName"),
    amendment(s"$items.additionalDocuments.documents.#1.dateOfValidity", items, s"$item.additionalDocuments.dateOfValidity"),
    amendment(s"$items.additionalDocuments.documents.#1.documentWriteOff.measurementUnit", items, s"$item.additionalDocuments.measurementUnit"),
    amendment(
      s"$items.additionalDocuments.documents.#1.documentWriteOff.documentQuantity",
      items,
      s"$item.additionalDocuments.measurementUnitQuantity"
    ),
    addition(s"$parties.declarationHoldersData.holders.type", parties, s"${keys(parties)}.holders.holder.type"),
    addition(s"$parties.declarationHoldersData.holders.eori", parties, s"${keys(parties)}.holders.holder.eori"),
    addition(s"$items.procedureCodes.procedureCode.code", items, s"$item.procedureCode"),
    addition(s"$items.procedureCodes.additionalProcedureCodes", items, s"$item.additionalProcedureCodes"),
    addition(s"$items.commodityDetails", items, s"$item.commodityCode"),
    addition(s"$items.commodityDetails.descriptionOfGoods", items, s"$item.goodsDescription"),
    addition(s"$items.packageInformation.#1.typesOfPackages", items, s"$item.packageInformation.type"),
    addition(s"$items.packageInformation.#1.numberOfPackages", items, s"$item.packageInformation.number"),
    addition(s"$items.packageInformation.#1.shippingMarks", items, s"$item.packageInformation.markings"),
    addition(s"$items.commodityMeasure.grossMass", items, s"$item.grossWeight"),
    addition(s"$items.commodityMeasure.supplementaryUnits", items, s"$item.supplementaryUnits"),
    addition(s"$items.additionalInformation.items.#1.code", items, s"$item.additionalInformation.code"),
    addition(s"$items.additionalInformation.items.#1.description", items, s"$item.additionalInformation.description"),
    addition(s"$items.additionalDocuments.documents.#1.documentTypeCode", items, s"$item.additionalDocuments.code"),
    addition(s"$items.additionalDocuments.documents.#1.documentIdentifier", items, s"$item.additionalDocuments.identifier"),
    addition(s"$items.additionalDocuments.documents.#1.documentStatus", items, s"$item.additionalDocuments.status"),
    addition(s"$items.additionalDocuments.documents.#1.documentStatusReason", items, s"$item.additionalDocuments.statusReason"),
    addition(s"$items.additionalDocuments.documents.#1.issuingAuthorityName", items, s"$item.additionalDocuments.issuingAuthorityName"),
    addition(s"$items.additionalDocuments.documents.#1.dateOfValidity", items, s"$item.additionalDocuments.dateOfValidity"),
    addition(s"$items.additionalDocuments.documents.#1.measurementUnit", items, s"$item.additionalDocuments.measurementUnit"),
    addition(s"$items.additionalDocuments.documents.#1.measurementUnitQuantity", items, s"$item.additionalDocuments.measurementUnitQuantity"),
    addition("declaration.totalNumberOfItems.totalAmountInvoiced", transaction, s"${keys(transaction)}.itemAmount"),
    addition("declaration.totalNumberOfItems.totalAmountInvoicedCurrency", transaction, s"${keys(transaction)}.currencyCode"),
    // addition("declaration.totalNumberOfItems", transaction, s"${keys(transaction)}.exchangeRate"), ???????????????
    addition("declaration.totalNumberOfItems.totalPackage", transaction, s"${keys(transaction)}.totalNoOfPackages"),
    addition("declaration.previousDocuments.#2.documentType", transaction, s"${keys(transaction)}.previousDocuments.type"),
    addition("declaration.previousDocuments.#2.documentReference", transaction, s"${keys(transaction)}.previousDocuments.reference")
    // addition("declaration.previousDocuments.documents.#2", transaction, s"${keys(transaction)}.previousDocuments.goodsItemIdentifier"), ????????
  )
  // scalastyle:on
}
