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

import base.Injector
import forms.common.Address
import forms.declaration.ConsigneeDetailsSpec.{correctConsigneeDetailsAddressOnly, correctConsigneeDetailsEORIOnly}
import forms.declaration.DeclarantDetailsSpec.{correctDeclarantDetailsAddressOnly, correctDeclarantDetailsEORIOnly}
import forms.declaration.DeclarationHolder
import forms.declaration.ExporterDetailsSpec.{correctExporterDetailsAddressOnly, correctExporterDetailsEORIOnly}
import forms.declaration.RepresentativeDetailsSpec.{
  correctRepresentativeDetailsAddressOnly,
  correctRepresentativeDetailsEORIOnly
}
import models.declaration.DeclarationAdditionalActorsDataSpec.correctAdditionalActorsData
import models.declaration.{DeclarationHoldersData, Parties}
import play.api.i18n.MessagesApi
import play.twirl.api.Html
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.summary.parties_section
import views.tags.ViewTest

@ViewTest
class PartiesSectionViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private lazy val emptyView = createView()

  private def createView(partiesOpt: Option[Parties] = None): Html = parties_section(partiesOpt)

  private def extractAddress(address: Address): String =
    Seq(address.fullName, address.addressLine, address.townOrCity, address.postCode, address.country).mkString(" ")

  "Parties Section View" when {

    "have correct message keys" in {
      val messages = instanceOf[MessagesApi].preferred(request)

      messages must haveTranslationFor("supplementary.summary.parties.header")
      messages must haveTranslationFor("supplementary.summary.parties.exporterId")
      messages must haveTranslationFor("supplementary.summary.parties.exporterAddress")
      messages must haveTranslationFor("supplementary.summary.parties.consigneeId")
      messages must haveTranslationFor("supplementary.summary.parties.consigneeAddress")
      messages must haveTranslationFor("supplementary.summary.parties.declarantId")
      messages must haveTranslationFor("supplementary.summary.parties.declarantAddress")
      messages must haveTranslationFor("supplementary.summary.parties.representativeId")
      messages must haveTranslationFor("supplementary.summary.parties.representativeAddress")
      messages must haveTranslationFor("supplementary.summary.parties.representationType")
      messages must haveTranslationFor("supplementary.summary.parties.idStatusNumberAuthorisationCode")
      messages must haveTranslationFor("supplementary.summary.parties.authorizedPartyEori")
      messages must haveTranslationFor("supplementary.summary.parties.additionalParties.header")
      messages must haveTranslationFor("supplementary.summary.parties.additionalParties.id")
      messages must haveTranslationFor("supplementary.summary.parties.additionalParties.type")
    }

    "provided with empty Parties data" should {

      "display Parties header" in {
        emptyView.select("table:nth-child(1)>caption").text() mustBe "supplementary.summary.parties.header"
      }

      "display 'Exporter ID' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.exporterId"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Exporter address' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.exporterAddress"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Consignee ID' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.consigneeId"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Consignee address' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.consigneeAddress"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Declarant ID' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.declarantId"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Representative ID' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.representativeId"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Representative address' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.representativeAddress"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Representation type' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.representationType"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'Authorised party EORI' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.authorizedPartyEori"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display 'ID status number authorisation code' table row with no value" in {

        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.idStatusNumberAuthorisationCode"
        emptyView
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(2)")
          .text() mustBe empty
      }

      "display Additional Parties header" in {

        emptyView
          .select("table:nth-child(2)>caption:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.additionalParties.header"
      }

      "display 'Additional parties ID' table header" in {

        emptyView
          .select(".form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.additionalParties.id"
      }

      "display 'Additional parties type' table header" in {

        emptyView
          .select(".form-group>thead:nth-child(1)>tr:nth-child(1)>th:nth-child(2)")
          .text() mustBe "supplementary.summary.parties.additionalParties.type"
      }

      "not display data row in Additional Parties table" in {

        emptyView.select(".form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)").size() mustBe 0
      }

    }

    "provided with Parties data" should {

      "display 'Exporter ID' table row with proper value" in {

        val exporterDetails = correctExporterDetailsEORIOnly
        val view = createView(Some(Parties(exporterDetails = Some(exporterDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.exporterId"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)")
          .text() mustBe exporterDetails.details.eori.get
      }

      "display 'Exporter address' table row with proper value" in {

        val exporterDetails = correctExporterDetailsAddressOnly
        val view = createView(Some(Parties(exporterDetails = Some(exporterDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.exporterAddress"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(2)")
          .text() must equal(extractAddress(exporterDetails.details.address.get))
      }

      "display 'Consignee ID' table row with proper value" in {

        val consigneeDetails = correctConsigneeDetailsEORIOnly
        val view = createView(Some(Parties(consigneeDetails = Some(consigneeDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.consigneeId"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(3)>td:nth-child(2)")
          .text() must equal(consigneeDetails.details.eori.get)
      }

      "display 'Consignee address' table row with proper value" in {

        val consigneeDetails = correctConsigneeDetailsAddressOnly
        val view = createView(Some(Parties(consigneeDetails = Some(consigneeDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.consigneeAddress"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(4)>td:nth-child(2)")
          .text() must equal(extractAddress(consigneeDetails.details.address.get))
      }

      "display 'Declarant ID' table row with proper value" in {

        val declarantDetails = correctDeclarantDetailsEORIOnly
        val view = createView(Some(Parties(declarantDetails = Some(declarantDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.declarantId"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(5)>td:nth-child(2)")
          .text() must equal(declarantDetails.details.eori.get)
      }

      "display 'Representative ID' table row with proper value" in {

        val representativeDetails = correctRepresentativeDetailsEORIOnly
        val view = createView(Some(Parties(representativeDetails = Some(representativeDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.representativeId"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(6)>td:nth-child(2)")
          .text() must equal(representativeDetails.details.flatMap(_.eori).get)
      }

      "display 'Representative address' table row with proper value" in {

        val representativeDetails = correctRepresentativeDetailsAddressOnly
        val view = createView(Some(Parties(representativeDetails = Some(representativeDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.representativeAddress"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(7)>td:nth-child(2)")
          .text() must equal(extractAddress(representativeDetails.details.flatMap(_.address).get))
      }

      "display 'Representation type' table row with proper value" in {

        val representativeDetails = correctRepresentativeDetailsEORIOnly
        val view = createView(Some(Parties(representativeDetails = Some(representativeDetails))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.representationType"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(8)>td:nth-child(2)")
          .text() must equal(representativeDetails.statusCode.get)
      }

      "display 'Authorised party EORI' table row with proper value" in {

        val declarationHoldersData =
          DeclarationHoldersData(Seq(DeclarationHolder(authorisationTypeCode = None, eori = Some("PL213472539481923"))))
        val view = createView(Some(Parties(declarationHoldersData = Some(declarationHoldersData))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.authorizedPartyEori"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(9)>td:nth-child(2)")
          .text() must equal(declarationHoldersData.holders.head.eori.get)
      }

      "display 'ID status number authorisation code' table row with proper value" in {

        val declarationHoldersData =
          DeclarationHoldersData(Seq(DeclarationHolder(authorisationTypeCode = Some("1234"), eori = None)))
        val view = createView(Some(Parties(declarationHoldersData = Some(declarationHoldersData))))

        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(1)")
          .text() mustBe "supplementary.summary.parties.idStatusNumberAuthorisationCode"
        view
          .select("table:nth-child(1)>tbody:nth-child(2)>tr:nth-child(10)>td:nth-child(2)")
          .text() mustBe declarationHoldersData.holders.head.authorisationTypeCode.get
      }

      "display 2 data rows in Additional Parties table" in {

        val additionalActorsData = correctAdditionalActorsData
        val view = createView(Some(Parties(declarationAdditionalActorsData = Some(additionalActorsData))))

        view
          .select(".form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(1)")
          .text() mustBe additionalActorsData.actors.head.eori.get
        view
          .select(".form-group>tbody:nth-child(2)>tr:nth-child(1)>td:nth-child(2)")
          .text() mustBe additionalActorsData.actors.head.partyType.get
        view
          .select(".form-group>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(1)")
          .text() mustBe additionalActorsData.actors(1).eori.get
        view
          .select(".form-group>tbody:nth-child(2)>tr:nth-child(2)>td:nth-child(2)")
          .text() mustBe additionalActorsData.actors(1).partyType.get
      }
    }
  }

}
