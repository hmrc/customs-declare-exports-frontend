/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.common.Address
import forms.declaration.ConsigneeDetailsSpec.{correctConsigneeDetailsAddressOnly, correctConsigneeDetailsEORIOnly}
import forms.declaration.DeclarantDetailsSpec.{correctDeclarantDetailsAddressOnly, correctDeclarantDetailsEORIOnly}
import forms.declaration.DeclarationHolder
import forms.declaration.ExporterDetailsSpec.{correctExporterDetailsAddressOnly, correctExporterDetailsEORIOnly}
import forms.declaration.RepresentativeDetailsSpec.{
  correctRepresentativeDetailsAddressOnly,
  correctRepresentativeDetailsEORIOnly
}
import helpers.views.declaration.summary.PartiesMessages
import models.declaration.DeclarationAdditionalActorsDataSpec.correctAdditionalActorsData
import models.declaration.{DeclarationHoldersData, Parties}
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.summary.parties_section
import views.tags.ViewTest

@ViewTest
class PartiesSectionViewSpec extends ViewSpec with PartiesMessages {

  private def createView(partiesOpt: Option[Parties] = None): Html = parties_section(partiesOpt)

  private def extractAddress(address: Address): String =
    Seq(address.fullName, address.addressLine, address.townOrCity, address.postCode, address.country).mkString(" ")

  private lazy val emptyView = createView()

  "Parties Section View" should {

    "have proper messages for labels" in {

      assertMessage(header, "Parties")
      assertMessage(exporterId, "Exporter ID")
      assertMessage(exporterAddress, "Exporter address")
      assertMessage(declarantId, "Declarant ID")
      assertMessage(declarantAddress, "Declarant address")
      assertMessage(representativeId, "Representative ID")
      assertMessage(representativeAddress, "Representative address")
      assertMessage(representationType, "Representation type")
      assertMessage(authorizedPartyEori, "Authorised party EORI")
      assertMessage(idStatusNumberAuthorisationCode, "ID status number authorisation code")
      assertMessage(additionalPartiesHeader, "Additional parties")
      assertMessage(additionalPartiesId, "Additional parties ID")
      assertMessage(additionalPartiesType, "Additional parties type")
    }
  }

  "Parties Section View" when {

    "provided with empty Parties data" should {

      "display Parties header" in {

        getElementByCss(emptyView, "table:nth-child(1)>caption").text() mustEqual messages(header)
      }

      "display 'Exporter ID' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").text() mustEqual messages(exporterId)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Exporter address' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(1)").text() mustEqual messages(exporterAddress)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Consignee ID' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(1)").text() mustEqual messages(consigneeId)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Consignee address' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(1)").text() mustEqual messages(consigneeAddress)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Declarant ID' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(1)").text() mustEqual messages(declarantId)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Declarant address' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(1)").text() mustEqual messages(declarantAddress)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Representative ID' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(1)").text() mustEqual messages(representativeId)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Representative address' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(1)").text() mustEqual messages(representativeAddress)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Representation type' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(1)").text() mustEqual messages(representationType)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'Authorised party EORI' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(1)").text() mustEqual messages(authorizedPartyEori)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(2)").text() mustEqual ""
      }

      "display 'ID status number authorisation code' table row with no value" in {

        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(11)>td:nth-child(1)").text() mustEqual messages(idStatusNumberAuthorisationCode)
        getElementByCss(emptyView, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(11)>td:nth-child(2)").text() mustEqual ""
      }

      "display Additional Parties header" in {

        getElementByCss(emptyView, "table:nth-child(2)>caption:nth-child(1)").text() mustEqual messages(additionalPartiesHeader)
      }

      "display 'Additional parties ID' table header" in {

        getElementByCss(emptyView, ".form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(1)").text() mustEqual messages(additionalPartiesId)
      }

      "display 'Additional parties type' table header" in {

        getElementByCss(emptyView, ".form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(2)").text() mustEqual messages(additionalPartiesType)
      }

      "not display data row in Additional Parties table" in {

        val exceptionThrown = the[Exception] thrownBy getElementByCss(
          emptyView,
          ".form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)"
        )
        exceptionThrown.getMessage must include("Can't find element")
      }

    }

    "provided with Parties data" should {

      "display 'Exporter ID' table row with proper value" in {

        val exporterDetails = correctExporterDetailsEORIOnly
        val view = createView(Some(Parties(exporterDetails = Some(exporterDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").text() mustEqual messages(exporterId)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)").text() mustEqual exporterDetails.details.eori.get
      }

      "display 'Exporter address' table row with proper value" in {

        val exporterDetails = correctExporterDetailsAddressOnly
        val view = createView(Some(Parties(exporterDetails = Some(exporterDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(1)").text() mustEqual messages(exporterAddress)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(2)").text() mustEqual extractAddress(exporterDetails.details.address.get)
      }

      "display 'Consignee ID' table row with proper value" in {

        val consigneeDetails = correctConsigneeDetailsEORIOnly
        val view = createView(Some(Parties(consigneeDetails = Some(consigneeDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(1)").text() mustEqual messages(consigneeId)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(2)").text() mustEqual consigneeDetails.details.eori.get
      }

      "display 'Consignee address' table row with proper value" in {

        val consigneeDetails = correctConsigneeDetailsAddressOnly
        val view = createView(Some(Parties(consigneeDetails = Some(consigneeDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(1)").text() mustEqual messages(consigneeAddress)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(2)").text() mustEqual extractAddress(consigneeDetails.details.address.get)
      }

      "display 'Declarant ID' table row with proper value" in {

        val declarantDetails = correctDeclarantDetailsEORIOnly
        val view = createView(Some(Parties(declarantDetails = Some(declarantDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(1)").text() mustEqual messages(declarantId)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(2)").text() mustEqual declarantDetails.details.eori.get
      }

      "display 'Declarant address' table row with proper value" in {

        val declarantDetails = correctDeclarantDetailsAddressOnly
        val view = createView(Some(Parties(declarantDetails = Some(declarantDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(1)").text() mustEqual messages(declarantAddress)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(2)").text() mustEqual extractAddress(declarantDetails.details.address.get)
      }

      "display 'Representative ID' table row with proper value" in {

        val representativeDetails = correctRepresentativeDetailsEORIOnly
        val view = createView(Some(Parties(representativeDetails = Some(representativeDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(1)").text() mustEqual messages(representativeId)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(2)").text() mustEqual representativeDetails.details.flatMap(_.eori).get
      }

      "display 'Representative address' table row with proper value" in {

        val representativeDetails = correctRepresentativeDetailsAddressOnly
        val view = createView(Some(Parties(representativeDetails = Some(representativeDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(1)").text() mustEqual messages(representativeAddress)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(2)").text() mustEqual extractAddress(representativeDetails.details.flatMap(_.address).get)
      }

      "display 'Representation type' table row with proper value" in {

        val representativeDetails = correctRepresentativeDetailsEORIOnly
        val view = createView(Some(Parties(representativeDetails = Some(representativeDetails))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(1)").text() mustEqual messages(representationType)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(2)").text() mustEqual representativeDetails.statusCode.get
      }

      "display 'Authorised party EORI' table row with proper value" in {

        val declarationHoldersData =
          DeclarationHoldersData(Seq(DeclarationHolder(authorisationTypeCode = None, eori = Some("PL213472539481923"))))
        val view = createView(Some(Parties(declarationHoldersData = Some(declarationHoldersData))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(1)").text() mustEqual messages(authorizedPartyEori)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(2)").text() mustEqual declarationHoldersData.holders.head.eori.get
      }

      "display 'ID status number authorisation code' table row with proper value" in {

        val declarationHoldersData =
          DeclarationHoldersData(Seq(DeclarationHolder(authorisationTypeCode = Some("1234"), eori = None)))
        val view = createView(Some(Parties(declarationHoldersData = Some(declarationHoldersData))))

        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(11)>td:nth-child(1)").text() mustEqual messages(idStatusNumberAuthorisationCode)
        getElementByCss(view, "table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(11)>td:nth-child(2)").text() mustEqual declarationHoldersData.holders.head.authorisationTypeCode.get
      }

      "display 2 data rows in Additional Parties table" in {

        val additionalActorsData = correctAdditionalActorsData
        val view = createView(Some(Parties(declarationAdditionalActorsData = Some(additionalActorsData))))

        getElementByCss(view, ".form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").text() mustEqual additionalActorsData.actors.head.eori.get
        getElementByCss(view, ".form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)").text() mustEqual additionalActorsData.actors.head.partyType.get
        getElementByCss(view, ".form-group>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(1)").text() mustEqual additionalActorsData.actors(1).eori.get
        getElementByCss(view, ".form-group>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(2)").text() mustEqual additionalActorsData.actors(1).partyType.get
      }
    }
  }

}
