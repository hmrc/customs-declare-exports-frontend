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
import forms.common.{Address, Eori}
import forms.declaration._
import forms.declaration.consignor.ConsignorDetails
import helpers.views.declaration.CommonMessages
import models.DeclarationType._
import models.Mode
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec2
import views.html.declaration.carrier_details
import views.tags.ViewTest

@ViewTest
class CarrierDetailsViewSpec extends UnitViewSpec2 with CommonMessages with Stubs with Injector {

  val form: Form[CarrierDetails] = CarrierDetails.form(STANDARD)
  private val carrierDetailsPage = instanceOf[carrier_details]

  private def createView(form: Form[CarrierDetails] = form)(implicit request: JourneyRequest[_]): Document =
    carrierDetailsPage(Mode.Normal, form)(request, messages)

  "Carrier Details" should {

    "have correct messages" in {

      messages must haveTranslationFor("declaration.carrier.title")
      messages must haveTranslationFor("declaration.carrier.title.hint")
      messages must haveTranslationFor("declaration.carrier.eori.info")
      messages must haveTranslationFor("declaration.carrier.address.info")
    }
  }

  "Carrier Details View on empty page" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      val view = createView()
      "display page title" in {

        view.getElementById("title") must containMessage("declaration.carrier.title")
      }

      "display empty input with label for EORI" in {

        view.getElementsByAttributeValue("for", "details_eori") must containMessageForElements("declaration.carrier.eori.info")
        view.getElementById("details_eori").attr("value") mustBe empty
      }

      "display empty input with label for Full name" in {

        view.getElementsByAttributeValue("for", "details_address_fullName") must containMessageForElements("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {

        view.getElementsByAttributeValue("for", "details_address_addressLine") must containMessageForElements("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {

        view.getElementsByAttributeValue("for", "details_address_townOrCity") must containMessageForElements("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {

        view.getElementsByAttributeValue("for", "details_address_postCode") must containMessageForElements("declaration.address.postCode")
        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {

        view.getElementsByAttributeValue("for", "details_address_country") must containMessageForElements("declaration.address.country")
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return") must containMessage(saveAndReturnCaption)
        view.getElementById("submit_and_return").attr("name") mustBe SaveAndReturn.toString
      }
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Representative Status' page" in {

        val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.no)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton.attr("href") mustBe routes.RepresentativeStatusController.displayPage().url
      }

      "display 'Back' button that links to 'Is Declarant Exporter' page" in {

        val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.yes)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton.attr("href") mustBe routes.DeclarantExporterController.displayPage().url
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Consignor Eori Number' page" in {

        val cachedParties = Parties(
          isExs = Some(IsExs(YesNoAnswers.yes)),
          declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.yes)),
          consignorDetails = Some(ConsignorDetails(EntityDetails(eori = Some(Eori("GB1234567890000")), None)))
        )
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.ConsignorEoriNumberController.displayPage().url
      }

      "display 'Back' button that links to 'Consignor Address' page" in {

        val cachedParties = Parties(
          isExs = Some(IsExs(YesNoAnswers.yes)),
          declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.yes)),
          consignorDetails =
            Some(ConsignorDetails(EntityDetails(None, address = Some(Address("fullName", "addressLine", "townOrCity", "postCode", "country")))))
        )
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.ConsignorDetailsController.displayPage().url
      }

      "display 'Back' button that links to 'Representative Status' page" in {

        val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.no)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.RepresentativeStatusController.displayPage().url
      }
    }
  }

  "Carrier Details View with invalid input" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display error when both EORI and Address are supplied" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .bind(CarrierDetailsSpec.correctCarrierDetailsJSON)
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details")

        view.getElementsByClass("govuk-list govuk-error-summary__list") must containMessageForElements("declaration.carrier.error.addressAndEori")
      }

      "display error when both EORI and business details are empty" in {

        val view = createView(CarrierDetails.form(request.declarationType).bind(Map[String, String]()))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details")
        view.getElementsByClass("govuk-list govuk-error-summary__list") must containMessageForElements("declaration.namedEntityDetails.error")
      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(Some(Eori(TestHelper.createRandomAlphanumericString(18))), None)))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_eori")
        view.getElementById("details_eori-error") must containMessage("declaration.eori.error.format")
      }

      "display error for empty Full name" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("declaration.address.fullName.empty")
      }

      "display error for incorrect Full name" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(
              CarrierDetails(
                EntityDetails(None, Some(Address(TestHelper.createRandomAlphanumericString(71), "Test Street", "Leeds", "LS18BN", "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("declaration.address.fullName.error")
      }

      "display error for empty Address" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("declaration.address.addressLine.empty")
      }

      "display error for incorrect Address" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(
              CarrierDetails(
                EntityDetails(None, Some(Address("Marco Polo", TestHelper.createRandomAlphanumericString(71), "Leeds", "LS18BN", "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("declaration.address.addressLine.error")
      }

      "display error for empty Town or city" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("declaration.address.townOrCity.empty")
      }

      "display error for incorrect Town or city" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(
              CarrierDetails(
                EntityDetails(None, Some(Address("Marco Polo", "Test Street", TestHelper.createRandomAlphanumericString(71), "LS18BN", "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("declaration.address.townOrCity.error")
      }

      "display error for empty Postcode" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("declaration.address.postCode.empty")
      }

      "display error for incorrect Postcode" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(
              CarrierDetails(
                EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", TestHelper.createRandomAlphanumericString(71), "England")))
              )
            )
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("declaration.address.postCode.error")
      }

      "display error for empty Country" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("declaration.address.country.empty")
      }

      "display error for incorrect Country" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("declaration.address.country.error")
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("declaration.address.addressLine.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("declaration.address.townOrCity.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("declaration.address.postCode.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("declaration.address.country.empty")
      }

      "display errors when everything except Country is empty" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
        )

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#details_address_fullName")
        view.getElementById("details_address_fullName-error") must containMessage("declaration.address.fullName.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("declaration.address.addressLine.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("declaration.address.townOrCity.empty")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("declaration.address.postCode.empty")
      }

      "display errors when everything except Full name is incorrect" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(
              CarrierDetails(
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
        view.getElementById("details_address_addressLine-error") must containMessage("declaration.address.addressLine.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("declaration.address.townOrCity.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("declaration.address.postCode.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_country")
        view.getElementById("error-message-details.address.country-input") must containMessage("declaration.address.country.error")
      }

      "display errors when everything except Country is incorrect" in {

        val view = createView(
          CarrierDetails
            .form(request.declarationType)
            .fillAndValidate(
              CarrierDetails(
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
        view.getElementById("details_address_fullName-error") must containMessage("declaration.address.fullName.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_addressLine")
        view.getElementById("details_address_addressLine-error") must containMessage("declaration.address.addressLine.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_townOrCity")
        view.getElementById("details_address_townOrCity-error") must containMessage("declaration.address.townOrCity.error")
        view must containErrorElementWithTagAndHref("a", "#details_address_postCode")
        view.getElementById("details_address_postCode-error") must containMessage("declaration.address.postCode.error")
      }
    }
  }

  "Carrier Details View when filled" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display data in EORI input" in {

        val form = CarrierDetails.form(request.declarationType).fill(CarrierDetails(EntityDetails(Some(Eori("1234")), None)))
        val view = createView(form)

        view.getElementById("details_eori").attr("value") mustBe "1234"
        view.getElementById("details_address_fullName").attr("value") mustBe empty
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
        view.getElementById("details_address_postCode").attr("value") mustBe empty
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display data in Business address inputs" in {

        val form = CarrierDetails
          .form(request.declarationType)
          .fill(CarrierDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
        val view = createView(form)

        view.getElementById("details_eori").attr("value") mustBe empty
        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "test4"
      }

      "display data in both EORI and Business address inputs" in {

        val form = CarrierDetails
          .form(request.declarationType)
          .fill(CarrierDetails(EntityDetails(Some(Eori("1234")), Some(Address("test", "test1", "test2", "test3", "test4")))))
        val view = createView(form)

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
