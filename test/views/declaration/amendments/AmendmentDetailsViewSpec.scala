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

package views.declaration.amendments

import base.{Injector, MockAuthAction, OverridableInjector}
import controllers.routes.DeclarationDetailsController
import forms.common.YesNoAnswer.YesNoAnswers.{no, yes}
import forms.common.{Address, Date, Eori, YesNoAnswer}
import forms.declaration.additionaldocuments.{AdditionalDocument, DocumentWriteOff}
import forms.declaration.ModeOfTransportCode.{InlandWaterway, Maritime, RoRo, Road}
import forms.declaration.TransportPayment.{cash, cheque}
import forms.declaration.carrier.CarrierDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.countries.Country
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.exporter.ExporterDetails
import forms.declaration.officeOfExit.OfficeOfExit
import forms.declaration.{
  AdditionalActor,
  AdditionalFiscalReference,
  AdditionalFiscalReferencesData,
  AdditionalInformation,
  CommodityDetails,
  ConsigneeDetails,
  CusCode,
  DeclarantDetails,
  Document,
  EntityDetails,
  InlandModeOfTransportCode,
  NactCode,
  NatureOfTransaction,
  PackageInformation,
  PersonPresentingGoodsDetails,
  Seal,
  StatisticalValue,
  SupervisingCustomsOffice,
  TaricCode,
  TransportCountry,
  TransportLeavingTheBorder,
  TransportPayment,
  UNDangerousGoodsCode,
  WarehouseIdentification
}
import models.declaration.{
  AdditionalDocuments,
  AdditionalInformationData,
  CommodityMeasure,
  Container,
  ExportItem,
  GoodsLocation,
  InvoiceAndPackageTotals,
  ProcedureCodesData,
  RepresentativeDetails,
  RoutingCountry
}
import models.declaration.submissions.RequestType.ExternalAmendmentRequest
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.when
import play.api.inject.bind
import play.twirl.api.HtmlFormat.Appendable
import services.DiffTools.ExportsDeclarationDiff
import services.model.PackageType
import services.{AlteredField, DocumentType, DocumentTypeService, OriginalAndNewValues, PackageTypesService}
import testdata.SubmissionsTestData.{action, submission}
import views.declaration.amendments.AmendmentDetailsViewSpec._
import views.declaration.spec.UnitViewSpec
import views.helpers.{CommonMessages, ViewDates}
import views.html.declaration.amendments.amendment_details
import views.tags.ViewTest

import scala.jdk.CollectionConverters._

case class Difference(
  alteredField: AlteredField,
  expectedSection: String,
  expectedOldVal: String = "",
  expectedNewVal: String = "",
  expectedKeys: Seq[String]
)

case class MultiRowDifference(alteredField: AlteredField, expectedSection: String, expectedKeysVals: Seq[(String, String)])

@ViewTest
class AmendmentDetailsViewSpec extends UnitViewSpec with CommonMessages with Injector with MockAuthAction {

  val documentTypeService = mock[DocumentTypeService]
  when(documentTypeService.findByCode(refEq("T2M"))(any())).thenReturn(DocumentType("T2M Proof", "T2M"))
  when(documentTypeService.findByCode(refEq("952"))(any())).thenReturn(DocumentType("TIR Carnet", "952"))

  val packageTypesService = mock[PackageTypesService]
  when(packageTypesService.findByCode(refEq("ZB"))(any())).thenReturn(PackageType("ZB", "Bag, large"))
  when(packageTypesService.findByCode(refEq("ZZ"))(any())).thenReturn(PackageType("ZZ", "Mutually defined"))

  private val injector =
    new OverridableInjector(bind[DocumentTypeService].toInstance(documentTypeService), bind[PackageTypesService].toInstance(packageTypesService))

  private val page = injector.instanceOf[amendment_details]

  private val verifiedEmailRequest = buildVerifiedEmailRequest(request, exampleUser)

  def createView(differences: ExportsDeclarationDiff = List.empty): Appendable =
    page(submission, action, differences)(verifiedEmailRequest, messages)

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
      val view = page(submission, action.copy(requestType = ExternalAmendmentRequest), List.empty)(verifiedEmailRequest, messages)
      view.getElementsByTag("h1").text mustBe messages("amendment.details.title.external")
    }

    "display the expected MRN paragraph" in {
      view.getElementById("section-header").text mustBe messages("mrn.heading", submission.mrn.value)
    }

    "display the expected date paragraph" in {
      view.getElementsByTag("time").text mustBe ViewDates.formatDateAtTime(action.requestTimestamp)
    }

    "display the expected table structure" in {
      val view = createView(List(singleRowDifferences.head.alteredField))
      val table = view.getElementsByTag("table").get(0)
      val headers = table.getElementsByClass("govuk-table__header")
      headers.size() mustBe 3
      headers.get(0).text mustBe ""
      headers.get(1).text mustBe messages("amendment.details.previous.value")
      headers.get(2).text mustBe messages("amendment.details.amended.value")
    }

    "display the expected differences in single rows" in {
      singleRowDifferences.foreach { difference =>
        val view = createView(List(difference.alteredField))
        view.getElementsByTag("h2").get(0).text mustBe messages(h2(difference.expectedSection), "1")

        val tbody = view.getElementsByTag("tbody").get(0)

        val columns = tbody.getElementsByClass("govuk-table__cell")
        columns.size() mustBe 3

        columns.get(0).text mustBe messages(difference.expectedKeys.head, difference.expectedKeys.tail: _*)
        columns.get(1).text mustBe difference.expectedOldVal
        columns.get(2).text mustBe difference.expectedNewVal
      }
    }

    "display the expected differences in multiple rows" in {
      multiRowDifferences.foreach { difference =>
        val expectedRows = difference.expectedKeysVals.size
        val view = createView(List(difference.alteredField))
        view.getElementsByTag("h2").get(0).text mustBe messages(h2(difference.expectedSection), "1")

        val tbody = view.getElementsByTag("tbody").get(0)

        val cells = tbody.getElementsByClass("govuk-table__cell")
        cells.size() mustBe expectedRows * 3

        val actualKeysVals = cells.eachText().asScala.toList.grouped(2)
        actualKeysVals.zip(difference.expectedKeysVals).foreach { case (actualFieldId :: actualValue, expectedKey -> expectedValue) =>
          actualFieldId mustBe messages(expectedKey)
          actualValue.head mustBe expectedValue
        }
      }
    }
  }
}

object AmendmentDetailsViewSpec {

  private def addOrRemove[T](pointer: String, value: T, expectedSection: String, expectedVal: String, expectedKeys: String*): List[Difference] =
    List(
      Difference(AlteredField(pointer, OriginalAndNewValues(None, Some(value))), expectedSection, "", expectedVal, expectedKeys),
      Difference(AlteredField(pointer, OriginalAndNewValues(Some(value), None)), expectedSection, expectedVal, "", expectedKeys)
    )

  private def addOrRemove[T](pointer: String, value: T, expectedSection: String, expectedKeysVals: Seq[(String, String)]): List[MultiRowDifference] =
    List(
      MultiRowDifference(AlteredField(pointer, OriginalAndNewValues(None, Some(value))), expectedSection, expectedKeysVals),
      MultiRowDifference(AlteredField(pointer, OriginalAndNewValues(Some(value), None)), expectedSection, expectedKeysVals)
    )

  private def amendment[T](
    pointer: String,
    oldVal: T,
    newVal: T,
    expectedSection: String,
    expectedOldVal: String,
    expectedNewVal: String,
    expectedKeys: String*
  ): List[Difference] =
    List(
      Difference(AlteredField(pointer, OriginalAndNewValues(None, Some(newVal))), expectedSection, "", expectedNewVal, expectedKeys),
      Difference(
        AlteredField(pointer, OriginalAndNewValues(Some(oldVal), Some(newVal))),
        expectedSection,
        expectedOldVal,
        expectedNewVal,
        expectedKeys
      ),
      Difference(AlteredField(pointer, OriginalAndNewValues(Some(oldVal), None)), expectedSection, expectedOldVal, "", expectedKeys)
    )

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
    items -> s"$summary.item",
    locations -> s"$summary.locations",
    parties -> s"$summary.parties",
    routeOfGoods -> s"$summary.countries",
    transaction -> s"$summary.transaction",
    transport -> s"$summary.transport"
  )

  private val fullNameOld = "Bags Export"
  private val addressLineOld = "1 Bags Avenue"
  private val townOrCityOld = "Paris"
  private val postCodeOld = "10001"
  private val countryOld = "France"

  private val eori1 = "HU168908061782401"
  private val eori2 = "HU168908061782402"
  private val eori = Eori(eori1)

  private val fullNameNew = "Bags Export"
  private val addressLineNew = "1 Bags Avenue"
  private val townOrCityNew = "Paris"
  private val postCodeNew = "10001"
  private val countryNew = "France"
  private val address = Address(fullNameNew, addressLineNew, townOrCityNew, postCodeNew, countryNew)
  private val expectedAddress = "Bags Export 1 Bags Avenue Paris 10001 France"

  // scalastyle:off
  lazy val singleRowDifferences =
    // =========================== Transport's fields
    amendment(s"$transport.expressConsignment", YesNoAnswer(no), YesNoAnswer(yes), transport, "No", "Yes", s"${h2(transport)}.expressConsignment") ++
      amendment(
        s"$transport.transportPayment.paymentMethod",
        TransportPayment(cash),
        TransportPayment(cheque),
        transport,
        "Payment in cash",
        "Payment by cheque",
        s"${h2(transport)}.payment"
      ) ++
      addOrRemove(
        s"$transport.containers.#1",
        Container(1, "1234", List(Seal(1, "Seal1"))),
        transport,
        "Container ID: 1234 Security seals: Seal1",
        s"${container}.information"
      ) ++
      amendment(s"$transport.containers.#1.id", "1234", "4321", transport, "1234", "4321", s"${container}.id") ++
      amendment(s"$transport.containers.#1.seals.#1", "Seal1", "Seal2", transport, "Seal1", "Seal2", s"${container}.securitySeal") ++
      amendment(
        s"$transport.borderModeOfTransportCode.code",
        TransportLeavingTheBorder(Some(RoRo)),
        TransportLeavingTheBorder(Some(InlandWaterway)),
        transport,
        "Roll on Roll off (RoRo)",
        "Inland waterway transport",
        s"${h2(transport)}.departure.transportCode.header"
      ) ++
      amendment(
        s"$transport.meansOfTransportOnDepartureType",
        "40",
        "30",
        transport,
        "Flight number",
        "Vehicle registration",
        s"${h2(transport)}.departure.meansOfTransport.type"
      ) ++
      amendment(
        s"$transport.meansOfTransportOnDepartureIDNumber",
        "1234",
        "4321",
        transport,
        "1234",
        "4321",
        s"${h2(transport)}.departure.meansOfTransport.id"
      ) ++
      amendment(
        s"$transport.meansOfTransportCrossingTheBorderNationality",
        TransportCountry(Some("France")),
        TransportCountry(Some("Italy")),
        transport,
        "France",
        "Italy",
        s"${h2(transport)}.registrationCountry"
      ) ++
      amendment(
        s"$transport.meansOfTransportCrossingTheBorderType",
        "20",
        "11",
        transport,
        "Train",
        "Ship name",
        s"${h2(transport)}.border.meansOfTransport.type"
      ) ++
      amendment(
        s"$transport.meansOfTransportCrossingTheBorderIDNumber",
        "An old ship",
        "A new ship",
        transport,
        "An old ship",
        "A new ship",
        s"${h2(transport)}.border.meansOfTransport.id"
      ) ++
      // =========================== Parties' fields
      addOrRemove(s"$parties.exporterDetails", ExporterDetails(EntityDetails(Some(eori), None)), parties, eori1, s"${h2(parties)}.exporter.eori") ++
      addOrRemove(
        s"$parties.exporterDetails",
        ExporterDetails(EntityDetails(None, Some(address))),
        parties,
        expectedAddress,
        s"${h2(parties)}.exporter.address"
      ) ++
      amendment(s"$parties.exporterDetails.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.exporter.eori") ++
      addOrRemove(s"$parties.exporterDetails.address", address, parties, expectedAddress, s"${h2(parties)}.exporter.address") ++
      amendment(
        s"$parties.exporterDetails.address.fullName",
        fullNameOld,
        fullNameNew,
        parties,
        fullNameOld,
        fullNameNew,
        s"${h2(parties)}.exporter.address.fullName"
      ) ++
      amendment(
        s"$parties.exporterDetails.address.addressLine",
        addressLineOld,
        addressLineNew,
        parties,
        addressLineOld,
        addressLineNew,
        s"${h2(parties)}.exporter.address.addressLine"
      ) ++
      amendment(
        s"$parties.exporterDetails.address.townOrCity",
        townOrCityOld,
        townOrCityNew,
        parties,
        townOrCityOld,
        townOrCityNew,
        s"${h2(parties)}.exporter.address.townOrCity"
      ) ++
      amendment(
        s"$parties.exporterDetails.address.postCode",
        postCodeOld,
        postCodeNew,
        parties,
        postCodeOld,
        postCodeNew,
        s"${h2(parties)}.exporter.address.postCode"
      ) ++
      amendment(
        s"$parties.exporterDetails.address.country",
        countryOld,
        countryNew,
        parties,
        countryOld,
        countryNew,
        s"${h2(parties)}.exporter.address.country"
      ) ++
      addOrRemove(
        s"$parties.consigneeDetails",
        ConsigneeDetails(EntityDetails(Some(eori), None)),
        parties,
        eori1,
        s"${h2(parties)}.consignee.eori"
      ) ++
      addOrRemove(
        s"$parties.consigneeDetails",
        ConsigneeDetails(EntityDetails(None, Some(address))),
        parties,
        expectedAddress,
        s"${h2(parties)}.consignee.address"
      ) ++
      amendment(s"$parties.consigneeDetails.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.consignee.eori") ++
      addOrRemove(s"$parties.consigneeDetails.address", address, parties, expectedAddress, s"${h2(parties)}.consignee.address") ++
      amendment(
        s"$parties.consigneeDetails.address.fullName",
        fullNameOld,
        fullNameNew,
        parties,
        fullNameOld,
        fullNameNew,
        s"${h2(parties)}.consignee.address.fullName"
      ) ++
      amendment(
        s"$parties.consigneeDetails.address.addressLine",
        addressLineOld,
        addressLineNew,
        parties,
        addressLineOld,
        addressLineNew,
        s"${h2(parties)}.consignee.address.addressLine"
      ) ++
      amendment(
        s"$parties.consigneeDetails.address.townOrCity",
        townOrCityOld,
        townOrCityNew,
        parties,
        townOrCityOld,
        townOrCityNew,
        s"${h2(parties)}.consignee.address.townOrCity"
      ) ++
      amendment(
        s"$parties.consigneeDetails.address.postCode",
        postCodeOld,
        postCodeNew,
        parties,
        postCodeOld,
        postCodeNew,
        s"${h2(parties)}.consignee.address.postCode"
      ) ++
      amendment(
        s"$parties.consigneeDetails.address.country",
        countryOld,
        countryNew,
        parties,
        countryOld,
        countryNew,
        s"${h2(parties)}.consignee.address.country"
      ) ++
      addOrRemove(
        s"$parties.consignorDetails",
        ConsignorDetails(EntityDetails(Some(eori), None)),
        parties,
        eori1,
        s"${h2(parties)}.consignor.eori"
      ) ++
      addOrRemove(
        s"$parties.consignorDetails",
        ConsignorDetails(EntityDetails(None, Some(address))),
        parties,
        expectedAddress,
        s"${h2(parties)}.consignor.address"
      ) ++
      amendment(s"$parties.consignorDetails.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.consignor.eori") ++
      addOrRemove(s"$parties.consignorDetails.address", address, parties, expectedAddress, s"${h2(parties)}.consignor.address") ++
      amendment(
        s"$parties.consignorDetails.address.fullName",
        fullNameOld,
        fullNameNew,
        parties,
        fullNameOld,
        fullNameNew,
        s"${h2(parties)}.consignor.address.fullName"
      ) ++
      amendment(
        s"$parties.consignorDetails.address.addressLine",
        addressLineOld,
        addressLineNew,
        parties,
        addressLineOld,
        addressLineNew,
        s"${h2(parties)}.consignor.address.addressLine"
      ) ++
      amendment(
        s"$parties.consignorDetails.address.townOrCity",
        townOrCityOld,
        townOrCityNew,
        parties,
        townOrCityOld,
        townOrCityNew,
        s"${h2(parties)}.consignor.address.townOrCity"
      ) ++
      amendment(
        s"$parties.consignorDetails.address.postCode",
        postCodeOld,
        postCodeNew,
        parties,
        postCodeOld,
        postCodeNew,
        s"${h2(parties)}.consignor.address.postCode"
      ) ++
      amendment(
        s"$parties.consignorDetails.address.country",
        countryOld,
        countryNew,
        parties,
        countryOld,
        countryNew,
        s"${h2(parties)}.consignor.address.country"
      ) ++
      addOrRemove(
        s"$parties.declarantDetails",
        DeclarantDetails(EntityDetails(Some(eori), None)),
        parties,
        eori1,
        s"${h2(parties)}.declarant.eori"
      ) ++
      addOrRemove(
        s"$parties.declarantDetails",
        DeclarantDetails(EntityDetails(None, Some(address))),
        parties,
        expectedAddress,
        s"${h2(parties)}.declarant.address"
      ) ++
      amendment(s"$parties.declarantDetails.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.declarant.eori") ++
      addOrRemove(s"$parties.declarantDetails.address", address, parties, expectedAddress, s"${h2(parties)}.declarant.address") ++
      amendment(
        s"$parties.declarantDetails.address.fullName",
        fullNameOld,
        fullNameNew,
        parties,
        fullNameOld,
        fullNameNew,
        s"${h2(parties)}.declarant.address.fullName"
      ) ++
      amendment(
        s"$parties.declarantDetails.address.addressLine",
        addressLineOld,
        addressLineNew,
        parties,
        addressLineOld,
        addressLineNew,
        s"${h2(parties)}.declarant.address.addressLine"
      ) ++
      amendment(
        s"$parties.declarantDetails.address.townOrCity",
        townOrCityOld,
        townOrCityNew,
        parties,
        townOrCityOld,
        townOrCityNew,
        s"${h2(parties)}.declarant.address.townOrCity"
      ) ++
      amendment(
        s"$parties.declarantDetails.address.postCode",
        postCodeOld,
        postCodeNew,
        parties,
        postCodeOld,
        postCodeNew,
        s"${h2(parties)}.declarant.address.postCode"
      ) ++
      amendment(
        s"$parties.declarantDetails.address.country",
        countryOld,
        countryNew,
        parties,
        countryOld,
        countryNew,
        s"${h2(parties)}.declarant.address.country"
      ) ++
      addOrRemove(
        s"$parties.representativeDetails",
        RepresentativeDetails(Some(EntityDetails(Some(eori), None)), None, None),
        parties,
        eori1,
        s"${h2(parties)}.representative.eori"
      ) ++
      addOrRemove(
        s"$parties.representativeDetails",
        RepresentativeDetails(Some(EntityDetails(None, Some(address))), None, None),
        parties,
        expectedAddress,
        s"${h2(parties)}.representative.address"
      ) ++
      amendment(s"$parties.representativeDetails.statusCode", "1", "2", parties, "Declarant", "Direct", s"${h2(parties)}.representative.type") ++
      amendment(s"$parties.representativeDetails.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.representative.eori") ++
      addOrRemove(s"$parties.representativeDetails.address", address, parties, expectedAddress, s"${h2(parties)}.representative.address") ++
      amendment(
        s"$parties.representativeDetails.address.fullName",
        fullNameOld,
        fullNameNew,
        parties,
        fullNameOld,
        fullNameNew,
        s"${h2(parties)}.representative.address.fullName"
      ) ++
      amendment(
        s"$parties.representativeDetails.address.addressLine",
        addressLineOld,
        addressLineNew,
        parties,
        addressLineOld,
        addressLineNew,
        s"${h2(parties)}.representative.address.addressLine"
      ) ++
      amendment(
        s"$parties.representativeDetails.address.townOrCity",
        townOrCityOld,
        townOrCityNew,
        parties,
        townOrCityOld,
        townOrCityNew,
        s"${h2(parties)}.representative.address.townOrCity"
      ) ++
      amendment(
        s"$parties.representativeDetails.address.postCode",
        postCodeOld,
        postCodeNew,
        parties,
        postCodeOld,
        postCodeNew,
        s"${h2(parties)}.representative.address.postCode"
      ) ++
      amendment(
        s"$parties.representativeDetails.address.country",
        countryOld,
        countryNew,
        parties,
        countryOld,
        countryNew,
        s"${h2(parties)}.representative.address.country"
      ) ++
      amendment(s"$parties.declarationAdditionalActorsData.actors.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.actors.eori") ++
      amendment(
        s"$parties.declarationAdditionalActorsData.actors.partyType",
        "CS",
        "MF",
        parties,
        "Consolidator",
        "Manufacturer",
        s"${h2(parties)}.actors.type"
      ) ++
      amendment(
        s"$parties.declarationHoldersData.holders.authorisationTypeCode",
        "AEOC",
        "AEOS",
        parties,
        "AEOC",
        "AEOS",
        s"${h2(parties)}.holders.holder.type"
      ) ++
      amendment(s"$parties.declarationHoldersData.holders.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.holders.holder.eori") ++
      addOrRemove(s"$parties.carrierDetails", CarrierDetails(EntityDetails(Some(eori), None)), parties, eori1, s"${h2(parties)}.carrier.eori") ++
      addOrRemove(
        s"$parties.carrierDetails",
        CarrierDetails(EntityDetails(None, Some(address))),
        parties,
        expectedAddress,
        s"${h2(parties)}.carrier.address"
      ) ++
      amendment(s"$parties.carrierDetails.eori", eori1, eori2, parties, eori1, eori2, s"${h2(parties)}.carrier.eori") ++
      addOrRemove(s"$parties.carrierDetails.address", address, parties, expectedAddress, s"${h2(parties)}.carrier.address") ++
      amendment(
        s"$parties.carrierDetails.address.fullName",
        fullNameOld,
        fullNameNew,
        parties,
        fullNameOld,
        fullNameNew,
        s"${h2(parties)}.carrier.address.fullName"
      ) ++
      amendment(
        s"$parties.carrierDetails.address.addressLine",
        addressLineOld,
        addressLineNew,
        parties,
        addressLineOld,
        addressLineNew,
        s"${h2(parties)}.carrier.address.addressLine"
      ) ++
      amendment(
        s"$parties.carrierDetails.address.townOrCity",
        townOrCityOld,
        townOrCityNew,
        parties,
        townOrCityOld,
        townOrCityNew,
        s"${h2(parties)}.carrier.address.townOrCity"
      ) ++
      amendment(
        s"$parties.carrierDetails.address.postCode",
        postCodeOld,
        postCodeNew,
        parties,
        postCodeOld,
        postCodeNew,
        s"${h2(parties)}.carrier.address.postCode"
      ) ++
      amendment(
        s"$parties.carrierDetails.address.country",
        countryOld,
        countryNew,
        parties,
        countryOld,
        countryNew,
        s"${h2(parties)}.carrier.address.country"
      ) ++
      addOrRemove(
        s"$parties.personPresentingGoodsDetails",
        PersonPresentingGoodsDetails(eori),
        parties,
        eori1,
        s"${h2(parties)}.personPresentingGoods"
      ) ++
      amendment(s"$parties.personPresentingGoodsDetails.eori", YesNoAnswer(no), YesNoAnswer(yes), parties, "No", "Yes", s"${h2(parties)}.eidr") ++
      // =========================== Locations' fields
      amendment(
        s"$locations.destinationCountry",
        Country(Some("FR")),
        Country(Some("IT")),
        routeOfGoods,
        "France",
        "Italy",
        s"${h2(routeOfGoods)}.countryOfDestination"
      ) ++
      addOrRemove(
        s"$locations.routingCountries.#1",
        RoutingCountry(1, Country(Some("IT"))),
        routeOfGoods,
        "Italy",
        s"${h2(routeOfGoods)}.routingCountry"
      ) ++
      amendment(
        s"$locations.routingCountries.#1",
        Country(Some("FR")),
        Country(Some("IT")),
        routeOfGoods,
        "France",
        "Italy",
        s"${h2(routeOfGoods)}.routingCountry"
      ) ++
      amendment(
        s"$locations.goodsLocation",
        GoodsLocation("GB", "C", "U", "ASDDOVAPF"),
        GoodsLocation("GB", "A", "U", "ABDABDABDGVM"),
        locations,
        "GBCUASDDOVAPF",
        "GBAUABDABDABDGVM",
        s"${h2(locations)}.goodsLocationCode"
      ) ++
      amendment(
        s"$locations.officeOfExit.officeId",
        OfficeOfExit("GB000434"),
        OfficeOfExit("GB000435"),
        locations,
        "GB000434",
        "GB000435",
        s"${h2(locations)}.officeOfExit"
      ) ++
      amendment(
        s"$locations.supervisingCustomsOffice",
        SupervisingCustomsOffice(Some("GBBEL001")),
        SupervisingCustomsOffice(Some("GBBEL002")),
        transport,
        "GBBEL001",
        "GBBEL002",
        s"${h2(transport)}.supervisingOffice"
      ) ++
      amendment(
        s"$locations.warehouseIdentification.identificationNumber",
        WarehouseIdentification(Some("R1234567GA")),
        WarehouseIdentification(Some("R1234567GB")),
        transport,
        "R1234567GA",
        "R1234567GB",
        s"${h2(transport)}.warehouse.id"
      ) ++
      amendment(
        s"$locations.inlandModeOfTransportCode.inlandModeOfTransportCode",
        InlandModeOfTransportCode(Some(Maritime)),
        InlandModeOfTransportCode(Some(Road)),
        transport,
        "Sea transport",
        "Road transport",
        s"${h2(transport)}.inlandModeOfTransport"
      ) ++
      // =========================== totalNumberOfItems' fields
      amendment(
        "declaration.totalNumberOfItems.totalAmountInvoiced",
        "123456",
        "654321",
        transaction,
        "123456",
        "654321",
        s"${h2(transaction)}.itemAmount"
      ) ++
      amendment(
        "declaration.totalNumberOfItems.totalAmountInvoicedCurrency",
        "GBP",
        "EUR",
        transaction,
        "GBP",
        "EUR",
        s"${h2(transaction)}.currencyCode"
      ) ++
      amendment("declaration.totalNumberOfItems.exchangeRate", "1.49", "94.1", transaction, "1.49", "94.1", s"${h2(transaction)}.exchangeRate") ++
      amendment("declaration.totalNumberOfItems.totalPackage", "10", "20", transaction, "10", "20", s"${h2(transaction)}.totalNoOfPackages") ++
      // =========================== natureOfTransaction
      amendment(
        "declaration.natureOfTransaction.natureType",
        NatureOfTransaction("1"),
        NatureOfTransaction("6"),
        transaction,
        "Goods being sold",
        "National purposes",
        s"${h2(transaction)}.natureOfTransaction"
      ) ++
      // =========================== previousDocuments' fields
      amendment(
        "declaration.previousDocuments.documents.#1.documentType",
        "T2M",
        "952",
        transaction,
        "T2M Proof - T2M",
        "TIR Carnet - 952",
        s"${h2(transaction)}.previousDocuments.type"
      ) ++
      amendment(
        "declaration.previousDocuments.documents.#1.documentReference",
        "OldRef",
        "NewRef",
        transaction,
        "OldRef",
        "NewRef",
        s"${h2(transaction)}.previousDocuments.reference"
      ) ++
      amendment(
        "declaration.previousDocuments.documents.#1.goodsItemIdentifier",
        "OldId",
        "NewId",
        transaction,
        "OldId",
        "NewId",
        s"${h2(transaction)}.previousDocuments.goodsItemIdentifier"
      ) ++
      // =========================== items' fields
      amendment(s"$items.procedureCodes.procedure.code", "1040", "1042", items, "1040", "1042", s"$item.procedureCode") ++
      amendment(s"$items.procedureCodes.additionalProcedureCodes", "000", "001", items, "000", "001", s"$item.additionalProcedureCode") ++
      amendment(
        s"$items.additionalFiscalReferencesData.references.#1",
        AdditionalFiscalReference("AD", "987654321"),
        AdditionalFiscalReference("DZ", "123456789"),
        items,
        "AD987654321",
        "DZ123456789",
        s"$item.VATdetails"
      ) ++
      amendment(
        s"$items.statisticalValue.statisticalValue",
        StatisticalValue("1000"),
        StatisticalValue("2000"),
        items,
        "1000",
        "2000",
        s"$item.itemValue"
      ) ++
      amendment(
        s"$items.commodityDetails.combinedNomenclatureCode",
        "4106920000",
        "4106920001",
        items,
        "4106920000",
        "4106920001",
        s"$item.commodityCode"
      ) ++
      amendment(s"$items.commodityDetails.descriptionOfGoods", "Bottles", "Shoes", items, "Bottles", "Shoes", s"$item.goodsDescription") ++
      amendment(
        s"$items.dangerousGoodsCode.dangerousGoodsCode",
        UNDangerousGoodsCode(Some("1234")),
        UNDangerousGoodsCode(Some("4321")),
        items,
        "1234",
        "4321",
        s"$item.unDangerousGoodsCode"
      ) ++
      amendment(s"$items.cusCode", CusCode(Some("ABCD1234")), CusCode(Some("DCBA4321")), items, "ABCD1234", "DCBA4321", s"$item.cusCode") ++
      amendment(s"$items.taricCode.#1", TaricCode("9SLQ"), TaricCode("9SLP"), items, "9SLQ", "9SLP", s"$item.taricAdditionalCode") ++
      amendment(s"$items.nactCode.#1", NactCode("X511"), NactCode("X512"), items, "X511", "X512", s"$item.nationalAdditionalCode") ++
      amendment(s"$items.nactExemptionCode", NactCode("VATR"), NactCode("VATE"), items, "No, reduced", "No, exempt", s"$item.zeroRatedForVat") ++
      amendment(
        s"$items.packageInformation.#1.typesOfPackages",
        "ZB",
        "ZZ",
        items,
        "Bag, large (ZB)",
        "Mutually defined (ZZ)",
        s"$item.packageInformation.type"
      ) ++
      amendment(s"$items.packageInformation.#1.numberOfPackages", "10", "20", items, "10", "20", s"$item.packageInformation.number") ++
      amendment(s"$items.packageInformation.#1.shippingMarks", "SM1", "SM2", items, "SM1", "SM2", s"$item.packageInformation.markings") ++
      amendment(s"$items.commodityMeasure.supplementaryUnits", "10", "11", items, "10", "11", s"$item.supplementaryUnits") ++
      amendment(s"$items.commodityMeasure.grossMass", "100", "200", items, "100", "200", s"$item.grossWeight") ++
      amendment(s"$items.commodityMeasure.netMass", "500", "600", items, "500", "600", s"$item.netWeight") ++
      amendment(s"$items.additionalInformation.items.#1.code", "LIC99", "AAA11", items, "LIC99", "AAA11", s"$item.additionalInformation.code") ++
      amendment(
        s"$items.additionalInformation.items.#1.description",
        "EXPORTER1",
        "EXPORTER2",
        items,
        "EXPORTER1",
        "EXPORTER2",
        s"$item.additionalInformation.description"
      ) ++
      amendment(
        s"$items.additionalDocuments.documents.#1.documentTypeCode",
        "X002",
        "9104",
        items,
        "X002",
        "9104",
        s"$item.additionalDocuments.code"
      ) ++
      amendment(
        s"$items.additionalDocuments.documents.#1.documentIdentifier",
        "R1234",
        "A1234",
        items,
        "R1234",
        "A1234",
        s"$item.additionalDocuments.identifier"
      ) ++
      amendment(s"$items.additionalDocuments.documents.#1.documentStatus", "AF", "XF", items, "AF", "XF", s"$item.additionalDocuments.status") ++
      amendment(
        s"$items.additionalDocuments.documents.#1.documentStatusReason",
        "Reason1",
        "Reason2",
        items,
        "Reason1",
        "Reason2",
        s"$item.additionalDocuments.statusReason"
      ) ++
      amendment(
        s"$items.additionalDocuments.documents.#1.issuingAuthorityName",
        "Authority1",
        "Authority2",
        items,
        "Authority1",
        "Authority2",
        s"$item.additionalDocuments.issuingAuthorityName"
      ) ++
      amendment(
        s"$items.additionalDocuments.documents.#1.dateOfValidity",
        "2023-06-10",
        "2023-07-10",
        items,
        "2023-06-10",
        "2023-07-10",
        s"$item.additionalDocuments.dateOfValidity"
      ) ++
      amendment(
        s"$items.additionalDocuments.documents.#1.documentWriteOff.measurementUnit",
        "LTR#A",
        "LTR#B",
        items,
        "LTR#A",
        "LTR#B",
        s"$item.additionalDocuments.measurementUnit"
      ) ++
      amendment(
        s"$items.additionalDocuments.documents.#1.documentWriteOff.documentQuantity",
        "246,000.50",
        "247,000.60",
        items,
        "246,000.50",
        "247,000.60",
        s"$item.additionalDocuments.measurementUnitQuantity"
      )
  // scalastyle:on

  // scalastyle:off
  lazy val multiRowDifferences =
    addOrRemove(
      s"$parties.representativeDetails",
      RepresentativeDetails(Some(EntityDetails(Some(eori), None)), Some("2"), None),
      parties,
      List(s"${h2(parties)}.representative.eori" -> eori1, s"${h2(parties)}.representative.type" -> "Direct")
    ) ++
      addOrRemove(
        s"$parties.declarationAdditionalActorsData.actors",
        AdditionalActor(Some(eori), Some("MF")),
        parties,
        List(s"${h2(parties)}.actors.eori" -> eori1, s"${h2(parties)}.actors.type" -> "Manufacturer")
      ) ++
      addOrRemove(
        s"$parties.declarationHoldersData.holders",
        AuthorisationHolder(Some("AEOC"), Some(eori), None),
        parties,
        List(s"${h2(parties)}.holders.holder.type" -> "AEOC", s"${h2(parties)}.holders.holder.eori" -> eori1)
      ) ++
      addOrRemove(
        "declaration.totalNumberOfItems",
        InvoiceAndPackageTotals(Some("567640"), Some("GBP"), None, Some("1.49"), Some("10")),
        transaction,
        List(
          s"${h2(transaction)}.itemAmount" -> "567640",
          s"${h2(transaction)}.currencyCode" -> "GBP",
          s"${h2(transaction)}.exchangeRate" -> "1.49",
          s"${h2(transaction)}.totalNoOfPackages" -> "10"
        )
      ) ++
      addOrRemove(
        "declaration.previousDocuments.documents.#2",
        Document("952", "Some reference", Some("Some id")),
        transaction,
        List(
          s"${h2(transaction)}.previousDocuments.type" -> "TIR Carnet - 952",
          s"${h2(transaction)}.previousDocuments.reference" -> "Some reference",
          s"${h2(transaction)}.previousDocuments.goodsItemIdentifier" -> "Some id"
        )
      ) ++
      addOrRemove(
        s"$items.procedureCodes",
        ProcedureCodesData(Some("1040"), List("000", "001")),
        items,
        List(s"$item.procedureCode" -> "1040", s"$item.additionalProcedureCodes" -> "000 001")
      ) ++
      addOrRemove(
        s"$items.commodityDetails",
        CommodityDetails(Some("4106920000"), Some("Bottles")),
        items,
        List(s"$item.commodityCode" -> "4106920000", s"$item.goodsDescription" -> "Bottles")
      ) ++
      addOrRemove(
        s"$items.packageInformation.#1",
        PackageInformation(1, "SomeId", Some("ZB"), Some(10), Some("SM1")),
        items,
        List(
          s"$item.packageInformation.type" -> "Bag, large (ZB)",
          s"$item.packageInformation.number" -> "10",
          s"$item.packageInformation.markings" -> "SM1"
        )
      ) ++
      addOrRemove(
        s"$items.commodityMeasure",
        CommodityMeasure(Some("10"), Some(false), Some("100"), Some("50")),
        items,
        List(s"$item.grossWeight" -> "100", s"$item.netWeight" -> "50", s"$item.supplementaryUnits" -> "10")
      ) ++
      addOrRemove(
        s"$items.additionalInformation.items.#1",
        AdditionalInformation("LIC99", "EXPORTER"),
        items,
        List(s"$item.additionalInformation.code" -> "LIC99", s"$item.additionalInformation.description" -> "EXPORTER")
      ) ++
      addOrRemove(
        s"$items.additionalDocuments.documents.#1",
        AdditionalDocument(
          Some("X002"),
          Some("Some identifier"),
          Some("AF"),
          Some("Some Reason"),
          Some("Some Authority"),
          Some(Date(Some(10), Some(6), Some(2023))),
          Some(DocumentWriteOff(Some("LTR#A"), Some(BigDecimal("246000.5"))))
        ),
        items,
        List(
          s"$item.additionalDocuments.code" -> "X002",
          s"$item.additionalDocuments.identifier" -> "Some identifier",
          s"$item.additionalDocuments.status" -> "AF",
          s"$item.additionalDocuments.statusReason" -> "Some Reason",
          s"$item.additionalDocuments.issuingAuthorityName" -> "Some Authority",
          s"$item.additionalDocuments.dateOfValidity" -> "2023-06-10",
          s"$item.additionalDocuments.measurementUnit" -> "LTR#A",
          s"$item.additionalDocuments.measurementUnitQuantity" -> "246000.5"
        )
      ) ++
      addOrRemove(
        s"$items.additionalDocuments.documents.#1.documentWriteOff",
        DocumentWriteOff(Some("LTR#A"), Some(BigDecimal("246000.5"))),
        items,
        List(s"$item.additionalDocuments.measurementUnit" -> "LTR#A", s"$item.additionalDocuments.measurementUnitQuantity" -> "246000.5")
      ) ++
      addOrRemove(
        s"$items",
        ExportItem(
          "ee7533e7",
          1,
          Some(ProcedureCodesData(Some("1040"), List("000", "001"))),
          None,
          Some(AdditionalFiscalReferencesData(List(AdditionalFiscalReference("AD", "987654321")))),
          Some(StatisticalValue("1000")),
          Some(CommodityDetails(Some("4106920000"), Some("Straw for bottles"))),
          Some(UNDangerousGoodsCode(Some("1234"))),
          Some(CusCode(Some("ABCD1234"))),
          Some(List(TaricCode("9SLQ"))),
          Some(List(NactCode("X511"))),
          Some(NactCode("VATR")),
          Some(List(PackageInformation(1, "123456789", Some("ZB"), Some(100), Some("Shipping marks")))),
          Some(CommodityMeasure(Some("10"), Some(false), Some("700"), Some("500"))),
          Some(AdditionalInformationData(Some(YesNoAnswer("Yes")), List(AdditionalInformation("LIC99", "EXPORTER")))),
          Some(
            AdditionalDocuments(
              Some(YesNoAnswer("Yes")),
              List(
                AdditionalDocument(
                  Some("X002"),
                  Some("Some identifier"),
                  Some("AF"),
                  Some("Some Reason"),
                  Some("Some Authority"),
                  Some(Date(Some(10), Some(6), Some(2023))),
                  Some(DocumentWriteOff(Some("LTR#A"), Some(BigDecimal("246000.5"))))
                )
              )
            )
          ),
          Some(true)
        ),
        items,
        List(
          s"$item.procedureCode" -> "1040",
          s"$item.additionalProcedureCodes" -> "000 001",
          s"$item.VATdetails" -> "AD987654321",
          s"$item.itemValue" -> "1000",
          s"$item.commodityCode" -> "4106920000",
          s"$item.goodsDescription" -> "Straw for bottles",
          s"$item.unDangerousGoodsCode" -> "1234",
          s"$item.cusCode" -> "ABCD1234",
          s"$item.taricAdditionalCode" -> "9SLQ",
          s"$item.nationalAdditionalCode" -> "X511",
          s"$item.zeroRatedForVat" -> "No, reduced",
          s"$item.packageInformation.type" -> "ZB",
          s"$item.packageInformation.number" -> "100",
          s"$item.packageInformation.markings" -> "Shipping marks",
          s"$item.grossWeight" -> "700",
          s"$item.netWeight" -> "500",
          s"$item.supplementaryUnits" -> "10",
          s"$item.additionalInformation.code" -> "LIC99",
          s"$item.additionalInformation.description" -> "EXPORTER",
          s"$item.additionalDocuments.code" -> "X002",
          s"$item.additionalDocuments.identifier" -> "Some identifier",
          s"$item.additionalDocuments.status" -> "AF",
          s"$item.additionalDocuments.statusReason" -> "Some Reason",
          s"$item.additionalDocuments.issuingAuthorityName" -> "Some Authority",
          s"$item.additionalDocuments.dateOfValidity" -> "2023-06-10",
          s"$item.additionalDocuments.measurementUnit" -> "LTR#A",
          s"$item.additionalDocuments.measurementUnitQuantity" -> "246000.5",
          s"$item.licences" -> "Yes"
        )
      )
  // scalastyle:on
}
