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

import base.ExportsTestData.newUser
import forms.declaration.{GoodsLocation, TransportInformationContainer}
import models.Mode
import models.declaration.{Items, SupplementaryDeclarationData}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._
import views.html.declaration.summary.{summary_page, summary_page_no_data}

class SummaryPageViewSpec
    extends WordSpec with MustMatchers with ExportsDeclarationBuilder with ExportsItemBuilder with Stubs {

  val declaration = aDeclaration(
    withConsignmentReferences(),
    withDestinationCountries(),
    withGoodsLocation(GoodsLocation("PL", "type", "id", Some("a"), Some("b"), Some("c"), Some("d"), Some("e"))),
    withWarehouseIdentification(Some("a"), Some("b"), Some("c"), Some("d")),
    withOfficeOfExit("id", Some("office"), Some("code")),
    withContainerData(TransportInformationContainer("id")),
    withTotalNumberOfItems(Some("123"), Some("123")),
    withNatureOfTransaction("nature"),
    withItem(anItem())
  )
  val request =
    JourneyRequest(AuthenticatedRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")), declaration)
  val summaryPage = contentAsString(
    new summary_page(mainTemplate)(Mode.Normal, SupplementaryDeclarationData(declaration))(
      request,
      stubMessages(),
      minimalAppConfig
    )
  )
  val summaryNoDataPage = contentAsString(new summary_page_no_data(mainTemplate)()(request, stubMessages()))

  "Summary page" should {

    "has correct main buttons" in {

      summaryPage must include("site.back")
      summaryPage must include("site.acceptAndSubmitDeclaration")
      summaryPage must include("button id=\"submit\" class=\"button\"")
    }

    "has correct declaration type" in {

      summaryPage must include("supplementary.summary.declarationType.header")
      summaryPage must include("supplementary.summary.declarationType.dispatchLocation")
      summaryPage must include("supplementary.summary.declarationType.supplementaryDeclarationType")
    }

    "has correct your references" in {
      summaryPage must include("supplementary.summary.yourReferences.header")
      summaryPage must include("supplementary.summary.yourReferences.ducr")
      summaryPage must include("supplementary.summary.yourReferences.lrn")
    }

    "has correct parties" in {
      summaryPage must include("supplementary.summary.parties.header")
      summaryPage must include("supplementary.summary.parties.exporterId")
      summaryPage must include("supplementary.summary.parties.exporterAddress")
      summaryPage must include("supplementary.summary.parties.declarantId")
      summaryPage must include("supplementary.summary.parties.declarantAddress")
      summaryPage must include("supplementary.summary.parties.representativeId")
      summaryPage must include("supplementary.summary.parties.representativeAddress")
      summaryPage must include("supplementary.summary.parties.representationType")
      summaryPage must include("supplementary.summary.parties.additionalParties.id")
      summaryPage must include("supplementary.summary.parties.additionalParties.type")
      summaryPage must include("supplementary.summary.parties.idStatusNumberAuthorisationCode")
      summaryPage must include("supplementary.summary.parties.authorizedPartyEori")
    }

    "has correct locations" in {
      summaryPage must include("declaration.summary.locations.header")
      summaryPage must include("supplementary.summary.locations.dispatchCountry")
      summaryPage must include("supplementary.summary.locations.destinationCountry")
      summaryPage must include("supplementary.summary.locations.goodsExaminationAddress")
      summaryPage must include("supplementary.summary.locations.goodsExaminationLocationType")
      summaryPage must include("supplementary.summary.locations.qualifierCode")
      summaryPage must include("supplementary.summary.locations.additionalQualifier")
      summaryPage must include("supplementary.summary.locations.warehouseType")
      summaryPage must include("supplementary.summary.locations.warehouseId")
      summaryPage must include("supplementary.summary.locations.supervisingCustomsOffice")
      summaryPage must include("supplementary.summary.locations.officeOfExit")
    }

    "has correct items" in {

      summaryPage must include("supplementary.summary.items.header")
      summaryPage must include("supplementary.summary.items.amountInvoiced")
      summaryPage must include("supplementary.summary.items.exchangeRate")
      summaryPage must include("supplementary.summary.items.transactionType")
    }

    "has correct transport info" in {

      summaryPage must include("supplementary.transportInfo.containers.title")
      summaryPage must include("supplementary.transportInfo.containerId.title")
    }

    "return no data page" when {

      "there is no mandatory information" in {

        summaryNoDataPage must include("supplementary.summary.title")
        summaryNoDataPage must include("supplementary.summary.noData.header")
        summaryNoDataPage must include("supplementary.summary.noData.header.secondary")
        summaryNoDataPage must include("Make an export declaration")
        summaryNoDataPage must include("/customs-declare-exports/start")
      }
    }
  }
}
