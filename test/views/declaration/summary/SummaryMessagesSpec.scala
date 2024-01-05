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

package views.declaration.summary

import views.declaration.spec.UnitViewSpec

class SummaryMessagesSpec extends UnitViewSpec {

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
      messages must haveTranslationFor("declaration.summary.draft")
    }

    "have defined references messages" in {
      messages must haveTranslationFor("declaration.summary.references.creation.date")
      messages must haveTranslationFor("declaration.summary.references.expiration.date")
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
      messages must haveTranslationFor("declaration.summary.parties.exporter.eori")
      messages must haveTranslationFor("declaration.summary.parties.exporter.address")
      messages must haveTranslationFor("declaration.summary.parties.consignee.eori")
      messages must haveTranslationFor("declaration.summary.parties.consignee.address")
      messages must haveTranslationFor("declaration.summary.parties.declarant.eori")
      messages must haveTranslationFor("declaration.summary.parties.declarant.address")
      messages must haveTranslationFor("declaration.summary.parties.representative.eori")
      messages must haveTranslationFor("declaration.summary.parties.representative.type")
      messages must haveTranslationFor("declaration.summary.parties.representative.type.1")
      messages must haveTranslationFor("declaration.summary.parties.representative.type.2")
      messages must haveTranslationFor("declaration.summary.parties.representative.type.3")
      messages must haveTranslationFor("declaration.summary.parties.carrier.eori")
      messages must haveTranslationFor("declaration.summary.parties.carrier.address")
      messages must haveTranslationFor("declaration.summary.parties.actors")
      messages must haveTranslationFor("declaration.summary.parties.holders")
      messages must haveTranslationFor("declaration.summary.parties.holders.type")
      messages must haveTranslationFor("declaration.summary.parties.holders.eori")
    }

    "have defined countries messages" in {
      messages must haveTranslationFor("declaration.summary.countries.routingCountries")
      messages must haveTranslationFor("declaration.summary.countries.countryOfDestination")
    }

    "have defined locations messages" in {
      messages must haveTranslationFor("declaration.summary.locations.goodsLocationCode")
      messages must haveTranslationFor("declaration.summary.locations.officeOfExit")
    }

    "have defined transaction messages" in {
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
      messages must haveTranslationFor("declaration.summary.items.empty")
      messages must haveTranslationFor("declaration.summary.item")
      messages must haveTranslationFor("declaration.summary.item.procedureCode")
      messages must haveTranslationFor("declaration.summary.item.onwardSupplyRelief")
      messages must haveTranslationFor("declaration.summary.item.VATdetails")
      messages must haveTranslationFor("declaration.summary.item.commodityCode")
      messages must haveTranslationFor("declaration.summary.item.goodsDescription")
      messages must haveTranslationFor("declaration.summary.item.unDangerousGoodsCode")
      messages must haveTranslationFor("declaration.summary.item.cusCode")
      messages must haveTranslationFor("declaration.summary.item.nationalAdditionalCodes")
      messages must haveTranslationFor("declaration.summary.item.itemValue")
      messages must haveTranslationFor("declaration.summary.item.supplementaryUnits")
      messages must haveTranslationFor("declaration.summary.item.grossWeight")
      messages must haveTranslationFor("declaration.summary.item.netWeight")
      messages must haveTranslationFor("declaration.summary.item.packageInformation")
      messages must haveTranslationFor("declaration.summary.item.packageInformation.type")
      messages must haveTranslationFor("declaration.summary.item.packageInformation.number")
      messages must haveTranslationFor("declaration.summary.item.packageInformation.markings")
      messages must haveTranslationFor("declaration.summary.item.additionalInformation")
      messages must haveTranslationFor("declaration.summary.item.additionalInformation.code")
      messages must haveTranslationFor("declaration.summary.item.additionalInformation.description")
      messages must haveTranslationFor("declaration.summary.item.additionalDocuments")
      messages must haveTranslationFor("declaration.summary.item.additionalDocuments.code")
      messages must haveTranslationFor("declaration.summary.item.additionalDocuments.identifier")
    }

    "have defined warehouse messages" in {
      messages must haveTranslationFor("declaration.summary.transport.warehouse.id")
      messages must haveTranslationFor("declaration.summary.transport.supervisingOffice")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.Maritime")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.Rail")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.Road")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.Air")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.PostalConsignment")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.FixedTransportInstallations")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.InlandWaterway")
      messages must haveTranslationFor("declaration.summary.transport.inlandModeOfTransport.Unknown")
    }

    "have defined transport messages" in {
      messages must haveTranslationFor("declaration.summary.transport.departure.transportCode.header")
      messages must haveTranslationFor("declaration.summary.transport.departure.meansOfTransport.header")
      messages must haveTranslationFor("declaration.summary.transport.border.meansOfTransport.header")
      messages must haveTranslationFor("declaration.summary.transport.payment")
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
  }
}
