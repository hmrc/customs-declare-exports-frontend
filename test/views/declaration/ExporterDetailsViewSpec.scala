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

package views.declaration

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, Eori, YesNoAnswer}
import forms.declaration.{EntityDetails, ExporterDetails}
import helpers.views.declaration.CommonMessages
import models.DeclarationType._
import models.Mode
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.exporter_details
import views.tags.ViewTest

@ViewTest
class ExporterDetailsViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  private val exporterDetailsPage = instanceOf[exporter_details]

  private def form(journeyType: DeclarationType): Form[ExporterDetails] = ExporterDetails.form(journeyType)

  private def createView(form: Form[ExporterDetails], messages: Messages = stubMessages())(implicit request: JourneyRequest[_]): Document =
    exporterDetailsPage(Mode.Normal, form)(request, messages)

  "Exporter Details View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display same page title as header" in {
        val viewWithMessage = createView(form(request.declarationType), messages = realMessagesApi.preferred(request))
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {

        createView(form(request.declarationType)).getElementById("section-header").text() must include(messages("declaration.summary.parties.header"))
      }

      "display empty input with label for EORI" in {

        val view = createView(form(request.declarationType))

        view.getElementsByAttributeValue("for", "details_eori").text() mustBe messages("declaration.exporter-detail.eori")
        view.getElementById("details_eori").attr("value") mustBe empty
      }

      "display empty input with label for Full name" in {

        val view = createView(form(request.declarationType))

        view.getElementsByAttributeValue("for", "details_address_fullName").text() mustBe messages("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {

        val view = createView(form(request.declarationType))
        view.getElementsByAttributeValue("for", "details_address_addressLine").text() mustBe messages("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {

        val view = createView(form(request.declarationType))
        view.getElementsByAttributeValue("for", "details_address_townOrCity").text() mustBe messages("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {

        val view = createView(form(request.declarationType))
        view.getElementsByAttributeValue("for", "details_address_postCode").text() mustBe messages("declaration.address.postCode")

        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {

        val view = createView(form(request.declarationType))

        view.getElementsByAttributeValue("for", "details_address_country").text() mustBe messages("declaration.address.country")

        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display 'Save and continue' button" in {
        val saveButton = createView(form(request.declarationType)).getElementById("submit")
        saveButton.text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button" in {
        val saveButton = createView(form(request.declarationType)).getElementById("submit_and_return")
        saveButton.text() mustBe messages(saveAndReturnCaption)
        saveButton.attr("name") mustBe SaveAndReturn.toString
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Declarant Is Exporter' page" in {

        val backButton = createView(form(request.declarationType)).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.DeclarantExporterController.displayPage().url
      }
    }

    onClearance { request =>
      "display 'Back' button that links to 'Declarant Is Exporter' page" when {
        "user have answered 'No' on 'Entry into Declarant's Records' page" in {

          val cachedParties = Parties(isEntryIntoDeclarantsRecords = Some(YesNoAnswer(YesNoAnswers.no)))
          val requestWithCachedEidr = journeyRequest(simpleClearanceDeclaration.copy(parties = cachedParties))

          val backButton = createView(form(request.declarationType))(requestWithCachedEidr).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe routes.DeclarantExporterController.displayPage().url
        }
      }

      "display 'Back' button that links to 'Person Presenting Goods Details' page" when {
        "user have answered 'Yes' on 'Entry into Declarant's Records' page" in {

          val cachedParties = Parties(isEntryIntoDeclarantsRecords = Some(YesNoAnswer(YesNoAnswers.yes)))
          val requestWithCachedEidr = journeyRequest(simpleClearanceDeclaration.copy(parties = cachedParties))

          val backButton = createView(form(request.declarationType))(requestWithCachedEidr).getElementById("back-link")

          backButton.text() mustBe messages(backCaption)
          backButton.attr("href") mustBe routes.PersonPresentingGoodsDetailsController.displayPage().url
        }
      }
    }
  }

  "Exporter Details View for invalid input" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display error when both EORI and business details are empty" in {

        val view = createView(form(request.declarationType).bind(Map[String, String]()))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details")
        view.getElementsByClass("govuk-list govuk-error-summary__list").attr("value") mustBe empty

      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(Some(Eori(TestHelper.createRandomAlphanumericString(18))), None)))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_eori")
        view.getElementById("details_eori-error") must containMessage("supplementary.eori.error.format")
      }

      "display error for empty Full name" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("supplementary.address.fullName.empty")

      }

      "display error for incorrect Full name" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(
              ExporterDetails(
                EntityDetails(None, Some(Address(TestHelper.createRandomAlphanumericString(71), "Test Street", "Leeds", "LS18BN", "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("supplementary.address.fullName.error")
      }

      "display error for empty Address" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error").attr("value") mustBe empty

      }

      "display error for incorrect Address" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(
              ExporterDetails(
                EntityDetails(None, Some(Address("Marco Polo", TestHelper.createRandomAlphanumericString(71), "Leeds", "LS18BN", "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("supplementary.address.addressLine.error")
      }

      "display error for empty Town or city" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("supplementary.address.townOrCity.empty")
      }

      "display error for incorrect Town or city" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(
              ExporterDetails(
                EntityDetails(None, Some(Address("Marco Polo", "Test Street", TestHelper.createRandomAlphanumericString(71), "LS18BN", "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("supplementary.address.townOrCity.error")
      }

      "display error for empty Postcode" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("supplementary.address.postCode.empty")
      }

      "display error for incorrect Postcode" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(
              ExporterDetails(
                EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", TestHelper.createRandomAlphanumericString(71), "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("supplementary.address.postCode.error")
      }

      "display error for empty Country" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("supplementary.address.country.empty")
      }

      "display error for incorrect Country" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("supplementary.address.country.error")
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("supplementary.address.addressLine.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("supplementary.address.townOrCity.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("supplementary.address.postCode.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("supplementary.address.country.empty")

      }

      "display errors when everything except Country is empty" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("supplementary.address.fullName.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("supplementary.address.addressLine.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("supplementary.address.townOrCity.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("supplementary.address.postCode.empty")

      }

      "display errors when everything except Full name is incorrect" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(
              ExporterDetails(
                EntityDetails(
                  None,
                  Some(
                    Address(
                      "Marco Polo",
                      TestHelper.createRandomAlphanumericString(71),
                      TestHelper.createRandomAlphanumericString(71),
                      TestHelper.createRandomAlphanumericString(71),
                      TestHelper.createRandomAlphanumericString(71)
                    )
                  )
                )
              )
            )
        )

        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("supplementary.address.addressLine.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("supplementary.address.townOrCity.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("supplementary.address.postCode.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("supplementary.address.country.error")
      }

      "display errors when everything except Country is incorrect" in {

        val view = createView(
          form(request.declarationType)
            .fillAndValidate(
              ExporterDetails(
                EntityDetails(
                  None,
                  Some(
                    Address(
                      TestHelper.createRandomAlphanumericString(71),
                      TestHelper.createRandomAlphanumericString(71),
                      TestHelper.createRandomAlphanumericString(71),
                      TestHelper.createRandomAlphanumericString(71),
                      "Ukraine"
                    )
                  )
                )
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("supplementary.address.fullName.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("supplementary.address.addressLine.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("supplementary.address.townOrCity.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("supplementary.address.postCode.error")
      }
    }
  }

  "Exporter Details View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in EORI input" in {

        val view = createView(form(request.declarationType).fill(ExporterDetails(EntityDetails(Some(Eori("1234")), None))))

        view.getElementById("details_eori").attr("value") mustBe "1234"
        view.getElementById("details_address_fullName").attr("value") mustBe empty
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
        view.getElementById("details_address_postCode").attr("value") mustBe empty
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display data in Business address inputs" in {

        val view = createView(
          form(request.declarationType)
            .fill(ExporterDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
        )

        view.getElementById("details_eori").attr("value") mustBe empty
        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "test4"
      }

      "display data in both EORI and Business address inputs" in {

        val view = createView(
          form(request.declarationType)
            .fill(ExporterDetails(EntityDetails(Some(Eori("1234")), Some(Address("test", "test1", "test2", "test3", "test4")))))
        )

        view.getElementById("details_eori").attr("value") mustBe "1234"
        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "test4"
      }
    }
  }
}
