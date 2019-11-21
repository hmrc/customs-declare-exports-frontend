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
import forms.declaration._
import models.DeclarationType.DeclarationType
import models.declaration.{Container, SupplementaryDeclarationData}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import models.{DeclarationType, ExportsDeclaration, Mode}
import org.jsoup.nodes.Document
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.cache.{ExportsDeclarationBuilder, ExportsItemBuilder}
import unit.tools.Stubs
import utils.FakeRequestCSRFSupport._
import views.declaration.spec.ViewMatchers
import views.html.declaration.summary.{summary_page, summary_page_no_data}

class SummaryPageViewSpec extends WordSpec with MustMatchers with ExportsDeclarationBuilder with ExportsItemBuilder with Stubs with ViewMatchers {

  private val form: Form[LegalDeclaration] = LegalDeclaration.form()
  val declaration: ExportsDeclaration = createDeclaration()
  val request: JourneyRequest[AnyContentAsEmpty.type] = createJourneyRequest(declaration)
  val summaryPage: String = createPage(request, declaration)
  val summaryNoDataPage = contentAsString(new summary_page_no_data(mainTemplate)()(request, stubMessages()))
  val amendSummaryPage = contentAsString(
    new summary_page(mainTemplate)(Mode.Amend, SupplementaryDeclarationData(declaration), form)(request, stubMessages(), minimalAppConfig)
  )

  "Summary page" should {
    def view(mode: Mode, declaration: ExportsDeclaration = declaration, legalForm: Form[LegalDeclaration] = form): Document =
      new summary_page(mainTemplate)(mode, SupplementaryDeclarationData(declaration), legalForm)(
        new JourneyRequest(new AuthenticatedRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")), declaration),
        stubMessages(),
        minimalAppConfig
      )

    "contain back button" when {
      "Draft Mode" in {
        val document = view(Mode.Draft)
        document must containElementWithID("link-back")
        document.getElementById("link-back") must haveHref(controllers.routes.SavedDeclarationsController.displayDeclarations())
        document.getElementById("link-back") must containText("site.back")
      }

      "Amend Mode" when {
        "source id populated" in {
          val model = aDeclaration(withSourceId("source-id"))
          val document = view(Mode.Amend, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(controllers.routes.SubmissionsController.displayListOfSubmissions())
          document.getElementById("link-back") must containText("supplementary.summary.back")
        }
      }

      "Normal Mode" when {
        "standard declaration with containers" in {
          val model = aDeclaration(withType(DeclarationType.STANDARD), withContainerData(Container("1234", Seq.empty)))
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
          )
          document.getElementById("link-back") must containText("site.back")
        }

        "standard declaration without containers" in {
          val model = aDeclaration(withType(DeclarationType.STANDARD))
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage(Mode.Normal))
        }

        "supplementary declaration with containers" in {
          val model = aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withContainerData(Container("1234", Seq.empty)))
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(
            controllers.declaration.routes.TransportContainerController.displayContainerSummary(Mode.Normal)
          )
        }

        "supplementary declaration without containers" in {
          val model = aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withoutContainerData())
          val document = view(Mode.Normal, model)
          document must containElementWithID("link-back")
          document.getElementById("link-back") must haveHref(controllers.declaration.routes.BorderTransportController.displayPage(Mode.Normal))
        }
      }

      "contains errors summary" when {

        "legal declaration form is incorrect" in {

          val formWithErrors: Form[LegalDeclaration] =
            LegalDeclaration.form().fillAndValidate(LegalDeclaration("", "", "", false))

          val document = view(Mode.Normal, legalForm = formWithErrors)

          document must containElementWithID("error-summary-heading")
          document must containElementWithID("fullName-error")
          document must containElementWithID("jobRole-error")
          document must containElementWithID("email-error")
          document must containElementWithID("confirmation-error")
        }
      }
    }

    "have correct main buttons" in {

      summaryPage must include("site.back")
      summaryPage must include("site.acceptAndSubmitDeclaration")
      summaryPage must include("button id=\"submit\" class=\"button\"")
    }

    "have correct declaration type" in {

      summaryPage must include("supplementary.summary.declarationType.header")
      summaryPage must include("supplementary.summary.declarationType.dispatchLocation")
      summaryPage must include("supplementary.summary.declarationType.supplementaryDeclarationType")
    }

    "have correct your references" in {
      summaryPage must include("supplementary.summary.yourReferences.header")
      summaryPage must include("supplementary.summary.yourReferences.ducr")
      summaryPage must include("supplementary.summary.yourReferences.lrn")
    }

    "have correct parties" in {
      summaryPage must include("supplementary.summary.parties.header")
      summaryPage must include("supplementary.summary.parties.exporterId")
      summaryPage must include("supplementary.summary.parties.exporterAddress")
      summaryPage must include("supplementary.summary.parties.declarantId")
      summaryPage must include("supplementary.summary.parties.representativeId")
      summaryPage must include("supplementary.summary.parties.representativeAddress")
      summaryPage must include("supplementary.summary.parties.representationType")
      summaryPage must include("supplementary.summary.parties.additionalParties.id")
      summaryPage must include("supplementary.summary.parties.additionalParties.type")
      summaryPage must include("supplementary.summary.parties.idStatusNumberAuthorisationCode")
      summaryPage must include("supplementary.summary.parties.authorizedPartyEori")
    }

    "have correct locations" in {
      summaryPage must include("declaration.summary.locations.header")
      summaryPage must include("supplementary.summary.locations.dispatchCountry")
      summaryPage must include("supplementary.summary.locations.destinationCountry")
      summaryPage must include("supplementary.summary.locations.goodsExaminationAddress")
      summaryPage must include("supplementary.summary.locations.goodsExaminationLocationType")
      summaryPage must include("supplementary.summary.locations.qualifierCode")
      summaryPage must include("supplementary.summary.locations.additionalIdentifier")
      summaryPage must include("supplementary.summary.locations.warehouseId")
      summaryPage must include("supplementary.summary.locations.supervisingCustomsOffice")
      summaryPage must include("supplementary.summary.locations.officeOfExit")
    }

    "have correct items" when {
      "on Standard journey" in {

        summaryPage must include("declaration.itemType.title")
        summaryPage must include("supplementary.summary.items.amountInvoiced")
        summaryPage must include("supplementary.summary.items.exchangeRate")
        summaryPage must include("supplementary.summary.items.numberOfPackages")
        summaryPage must include("supplementary.summary.items.transactionType")
      }

      "on Supplementary journey" in {
        val supplementaryDeclaration: ExportsDeclaration = createDeclaration(DeclarationType.SUPPLEMENTARY)
        val supplementaryRequest: JourneyRequest[AnyContentAsEmpty.type] = createJourneyRequest(supplementaryDeclaration)
        val supplementarySummaryPage: String = createPage(supplementaryRequest, supplementaryDeclaration)
        supplementarySummaryPage must include("declaration.itemType.title")
        supplementarySummaryPage must include("supplementary.summary.items.amountInvoiced")
        supplementarySummaryPage must include("supplementary.summary.items.exchangeRate")
        supplementarySummaryPage must include("supplementary.summary.items.numberOfPackages")
        supplementarySummaryPage must include("supplementary.summary.items.transactionType")
      }

      "on Simplified journey" in {
        val simplifiedDeclaration: ExportsDeclaration = createDeclaration(DeclarationType.SIMPLIFIED)
        val simplifiedRequest: JourneyRequest[AnyContentAsEmpty.type] = createJourneyRequest(simplifiedDeclaration)
        val simplifiedSummaryPage: String = createPage(simplifiedRequest, simplifiedDeclaration)

        simplifiedSummaryPage must include("declaration.itemType.title")
        simplifiedSummaryPage must not(include("supplementary.summary.items.amountInvoiced"))
        simplifiedSummaryPage must not(include("supplementary.summary.items.exchangeRate"))
        simplifiedSummaryPage must not(include("supplementary.summary.items.numberOfPackages"))
        simplifiedSummaryPage must include("supplementary.summary.items.transactionType")
      }
    }

    "have correct transport info" in {

      summaryPage must include("supplementary.transportInfo.containers.title")
      summaryPage must include("supplementary.transportInfo.containerId.title")
    }

    "return no data page" when {

      "there is no mandatory information" in {

        summaryNoDataPage must include("supplementary.summary.noData.header")
        summaryNoDataPage must include("supplementary.summary.noData.header.secondary")
        summaryNoDataPage must include("Make an export declaration")
        summaryNoDataPage must include("/customs-declare-exports/start")
      }
    }

    "amend summary page must include information about starting declaration from the beginning" in {

      amendSummaryPage must include("summary.amend.information")
    }
  }

  private def createDeclaration(declarationType: DeclarationType = DeclarationType.STANDARD): ExportsDeclaration = {
    val declaration = aDeclaration(
      withType(declarationType),
      withConsignmentReferences(),
      withDestinationCountries(),
      withGoodsLocation(GoodsLocation("PL", "type", "id", Some("a"), Some("b"), Some("c"), Some("d"), Some("e"))),
      withWarehouseIdentification(Some(WarehouseIdentification(Some("a")))),
      withSupervisingCustomsOffice(Some(SupervisingCustomsOffice(Some("b")))),
      withInlandModeOfTransportCode(Some(InlandModeOfTransportCode(Some("c")))),
      withOfficeOfExit("id", Some("code")),
      withContainerData(Container("id", Seq.empty)),
      withTotalNumberOfItems(Some("123"), Some("123")),
      withNatureOfTransaction("nature"),
      withItem(anItem())
    )
    declaration
  }

  private def createJourneyRequest(declaration: ExportsDeclaration) =
    new JourneyRequest(new AuthenticatedRequest(FakeRequest("", "").withCSRFToken, newUser("12345", "12345")), declaration)

  private def createPage(request: JourneyRequest[AnyContentAsEmpty.type], declaration: ExportsDeclaration) =
    contentAsString(
      new summary_page(mainTemplate)(Mode.Normal, SupplementaryDeclarationData(declaration), form)(request, stubMessages(), minimalAppConfig)
    )
}
