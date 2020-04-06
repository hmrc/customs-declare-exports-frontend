/*
 * Copyright 2020 HM Revenue & Customs
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

package views.declaration.summary

import views.declaration.spec.UnitViewSpec

class SummaryMessagesSpec extends UnitViewSpec {

  override val messages = realMessagesApi.preferred(request)

  "Summary page" should {

    "have defined general messages" in {

      messages must haveTranslationFor("declaration.summary.normal-header")
      messages must haveTranslationFor("declaration.summary.amend-header")
      messages must haveTranslationFor("declaration.summary.saved-header")
      messages must haveTranslationFor("declaration.summary.noData.header")
      messages must haveTranslationFor("declaration.summary.noData.header.secondary")
      messages must haveTranslationFor("declaration.summary.noData.button")
    }

    "have defined draft messages" in {

      messages must haveTranslationFor("declaration.summary.draft.ducr")
      messages must haveTranslationFor("declaration.summary.draft.createdDate")
      messages must haveTranslationFor("declaration.summary.draft.expireDate")
      messages must haveTranslationFor("declaration.summary.draft")
    }

    "have defined references messages" in {

      messages must haveTranslationFor("declaration.summary.references")
      messages must haveTranslationFor("declaration.summary.references.type")
      messages must haveTranslationFor("declaration.summary.references.location")
      messages must haveTranslationFor("declaration.summary.references.additionalType")
      messages must haveTranslationFor("declaration.summary.references.additionalType.Y")
      messages must haveTranslationFor("declaration.summary.references.additionalType.Z")
      messages must haveTranslationFor("declaration.summary.references.additionalType.D")
      messages must haveTranslationFor("declaration.summary.references.additionalType.A")
      messages must haveTranslationFor("declaration.summary.references.additionalType.C")
      messages must haveTranslationFor("declaration.summary.references.additionalType.F")
      messages must haveTranslationFor("declaration.summary.references.additionalType.B")
      messages must haveTranslationFor("declaration.summary.references.additionalType.E")
      messages must haveTranslationFor("declaration.summary.references.ducr")
      messages must haveTranslationFor("declaration.summary.references.lrn")
    }

    "have defined parties messages" in {

      messages must haveTranslationFor("declaration.summary.parties")
      messages must haveTranslationFor("declaration.summary.parties.exporter.eori")
      messages must haveTranslationFor("declaration.summary.parties.exporter.address")
      messages must haveTranslationFor("declaration.summary.parties.consignee.eori")
      messages must haveTranslationFor("declaration.summary.parties.consignee.address")
      messages must haveTranslationFor("declaration.summary.parties.declarant.eori")
      messages must haveTranslationFor("declaration.summary.parties.declarant.address")
      messages must haveTranslationFor("declaration.summary.parties.representative.eori")
      messages must haveTranslationFor("declaration.summary.parties.representative.address")
      messages must haveTranslationFor("declaration.summary.parties.representative.type")
      messages must haveTranslationFor("declaration.summary.parties.representative.type.1")
      messages must haveTranslationFor("declaration.summary.parties.representative.type.2")
      messages must haveTranslationFor("declaration.summary.parties.representative.type.3")
      messages must haveTranslationFor("declaration.summary.parties.carrier.eori")
      messages must haveTranslationFor("declaration.summary.parties.carrier.address")
      messages must haveTranslationFor("declaration.summary.parties.additional")
      messages must haveTranslationFor("declaration.summary.parties.holders")
      messages must haveTranslationFor("declaration.summary.parties.holders.type")
      messages must haveTranslationFor("declaration.summary.parties.holders.eori")
    }

    "have defined countries messages" in {

      messages must haveTranslationFor("declaration.summary.countries")
      messages must haveTranslationFor("declaration.summary.countries.countryOfDispatch")
      messages must haveTranslationFor("declaration.summary.countries.routingCountries")
      messages must haveTranslationFor("declaration.summary.countries.countryOfDestination")
    }

    "have defined locations messages" in {

      messages must haveTranslationFor("declaration.summary.locations")
      messages must haveTranslationFor("declaration.summary.locations.goodsLocationCode")
      messages must haveTranslationFor("declaration.summary.locations.officeOfExit")
      messages must haveTranslationFor("declaration.summary.locations.expressConsignment")
    }

    "have defined transaction messages" in {

      messages must haveTranslationFor("declaration.summary.transaction")
      messages must haveTranslationFor("declaration.summary.transaction.itemAmount")
      messages must haveTranslationFor("declaration.summary.transaction.exchangeRate")
      messages must haveTranslationFor("declaration.summary.transaction.totalNoOfPackages")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.1")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.2")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.3")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.4")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.5")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.6")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.7")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.8")
      messages must haveTranslationFor("declaration.summary.transaction.natureOfTransaction.9")
      messages must haveTranslationFor("declaration.summary.transaction.previousDocuments")
      messages must haveTranslationFor("declaration.summary.transaction.previousDocuments.type")
      messages must haveTranslationFor("declaration.summary.transaction.previousDocuments.reference")
    }

    "have defined items messages" in {

      messages must haveTranslationFor("declaration.summary.items")
      messages must haveTranslationFor("declaration.summary.items.empty")
      messages must haveTranslationFor("declaration.summary.items.item.sequenceId")
      messages must haveTranslationFor("declaration.summary.items.item.procedureCode")
      messages must haveTranslationFor("declaration.summary.items.item.onwardSupplyRelief")
      messages must haveTranslationFor("declaration.summary.items.item.VATdetails")
      messages must haveTranslationFor("declaration.summary.items.item.commodityCode")
      messages must haveTranslationFor("declaration.summary.items.item.goodsDescription")
      messages must haveTranslationFor("declaration.summary.items.item.unDangerousGoodsCode")
      messages must haveTranslationFor("declaration.summary.items.item.cusCode")
      messages must haveTranslationFor("declaration.summary.items.item.taricAdditionalCodes")
      messages must haveTranslationFor("declaration.summary.items.item.nationalAdditionalCodes")
      messages must haveTranslationFor("declaration.summary.items.item.itemValue")
      messages must haveTranslationFor("declaration.summary.items.item.supplementaryUnits")
      messages must haveTranslationFor("declaration.summary.items.item.grossWeight")
      messages must haveTranslationFor("declaration.summary.items.item.netWeight")
      messages must haveTranslationFor("declaration.summary.items.item.packageInformation")
      messages must haveTranslationFor("declaration.summary.items.item.packageInformation.type")
      messages must haveTranslationFor("declaration.summary.items.item.packageInformation.number")
      messages must haveTranslationFor("declaration.summary.items.item.packageInformation.markings")
      messages must haveTranslationFor("declaration.summary.items.item.additionalInformation")
      messages must haveTranslationFor("declaration.summary.items.item.additionalInformation.code")
      messages must haveTranslationFor("declaration.summary.items.item.additionalInformation.information")
      messages must haveTranslationFor("declaration.summary.items.item.supportingDocuments")
      messages must haveTranslationFor("declaration.summary.items.item.supportingDocuments.code")
      messages must haveTranslationFor("declaration.summary.items.item.supportingDocuments.information")
    }

    "have defined warehouse messages" in {

      messages must haveTranslationFor("declaration.summary.warehouse")
      messages must haveTranslationFor("declaration.summary.warehouse.id")
      messages must haveTranslationFor("declaration.summary.warehouse.supervisingOffice")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.Maritime")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.Rail")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.Road")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.Air")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.PostalConsignment")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.FixedTransportInstallations")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.InlandWaterway")
      messages must haveTranslationFor("declaration.summary.warehouse.inlandModeOfTransport.Unknown")
    }

    "have defined transport messages" in {

      messages must haveTranslationFor("declaration.summary.transport")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.header")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.header")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.header")
      messages must haveTranslationFor("declaration.summary.transport.payment")
      messages must haveTranslationFor("declaration.summary.transport.containers")
    }

    "have defined transport codes messages" in {
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.1")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.2")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.3")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.4")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.5")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.7")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.8")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.9")
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.no-code")
    }

    "have defined departure means of transport codes" in {

      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.10")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.11")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.20")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.30")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.40")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.41")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.80")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.81")
    }

    "have defined border means of transport codes" in {

      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.10")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.11")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.20")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.30")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.40")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.41")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.80")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.81")
    }

    "have defined payment methods" in {

      messages must haveTranslationFor("declaration.summary.transport.payment.A")
      messages must haveTranslationFor("declaration.summary.transport.payment.B")
      messages must haveTranslationFor("declaration.summary.transport.payment.C")
      messages must haveTranslationFor("declaration.summary.transport.payment.D")
      messages must haveTranslationFor("declaration.summary.transport.payment.H")
      messages must haveTranslationFor("declaration.summary.transport.payment.Y")
      messages must haveTranslationFor("declaration.summary.transport.payment.Z")
    }

    "have defined container messages" in {

      messages must haveTranslationFor("declaration.summary.container")
      messages must haveTranslationFor("declaration.summary.container.id")
      messages must haveTranslationFor("declaration.summary.container.securitySeals")
    }

    "have defined deprecated messages used in different places than summary" in {

      messages must haveTranslationFor("declaration.summary.locations.header")
      messages must haveTranslationFor("supplementary.summary.parties.header")
      messages must haveTranslationFor("supplementary.summary.yourReferences.header")
    }
  }
}
