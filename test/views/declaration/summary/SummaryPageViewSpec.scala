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
import forms.Choice
import forms.declaration.{GoodsLocation, LegalDeclaration}
import models.declaration.{Container, SupplementaryDeclarationData}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import models.{ExportsDeclaration, Mode}
import org.jsoup.nodes.Document
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._
import views.declaration.spec.ViewMatchers
import views.html.declaration.summary.{summary_page, summary_page_no_data}

class SummaryPageViewSpec
    extends WordSpec with MustMatchers with ExportsDeclarationBuilder with ExportsItemBuilder with Stubs
    with ViewMatchers {

  private val form: Form[LegalDeclaration] = LegalDeclaration.form()

  val declaration = aDeclaration(
    withConsignmentReferences(),
    withDestinationCountries(),
    withGoodsLocation(GoodsLocation("PL", "type", "id", Some("a"), Some("b"), Some("c"), Some("d"), Some("e"))),
    withWarehouseIdentification(Some("a"), Some("b"), Some("c"), Some("d")),
    withOfficeOfExit("id", Some("office"), Some("code")),
    withContainerData(Container("id", Seq.empty)),
    withTotalNumberOfItems(Some("123"), Some("123")),
    withNatureOfTransaction("nature"),
    withItem(anItem())
  )
  val request =
    new JourneyRequest(
      new AuthenticatedRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")),
      declaration
    )
  val summaryPage = contentAsString(
    new summary_page(mainTemplate)(Mode.Normal, SupplementaryDeclarationData(declaration), form)(
      request,
      stubMessages(),
      minimalAppConfig
    )
  )
  val summaryNoDataPage = contentAsString(new summary_page_no_data(mainTemplate)()(request, stubMessages()))

  "Summary page" should {
    def view(mode: Mode, declaration: ExportsDeclaration = declaration): Document =
      new summary_page(mainTemplate)(mode, SupplementaryDeclarationData(declaration), form)(
        new JourneyRequest(
          new AuthenticatedRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")),
          declaration
        ),
        stubMessages(),
        minimalAppConfig
      )

    "contain back button" when {
      "Draft Mode" when {
        "source Id is populated" in {
          val model = aDeclaration(withSourceId("source-id"))
          val document = view(Mode.Draft, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.routes.NotificationsController.listOfNotificationsForSubmission("source-id")
          )
        }

        "source Id is empty" in {
          val model = aDeclaration(withoutSourceId())
          val document = view(Mode.Draft, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.routes.SavedDeclarationsController.displayDeclarations()
          )
        }
      }

      "Normal Mode" when {
        "standard declaration with containers" in {
          val model = aDeclaration(
            withChoice(Choice.AllowedChoiceValues.StandardDec),
            withContainerData(Container("1234", Seq.empty))
          )
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
          )
        }

        "standard declaration without containers" in {
          val model = aDeclaration(withChoice(Choice.AllowedChoiceValues.StandardDec))
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.declaration.routes.TransportDetailsController.displayPage(Mode.Normal)
          )
        }

        "supplementary declaration with containers" in {
          val model = aDeclaration(
            withChoice(Choice.AllowedChoiceValues.SupplementaryDec),
            withContainerData(Container("1234", Seq.empty))
          )
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
          )
        }

        "supplementary declaration without containers" in {
          val model = aDeclaration(withChoice(Choice.AllowedChoiceValues.SupplementaryDec), withoutContainerData())
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.declaration.routes.TransportDetailsController.displayPage(Mode.Normal)
          )
        }
      }
    }

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
