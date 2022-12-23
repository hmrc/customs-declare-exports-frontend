/*
 * Copyright 2022 HM Revenue & Customs
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

import base.Injector
import connectors.CodeListConnector
import controllers.declaration.routes.CarrierEoriNumberController
import forms.common.Address
import forms.common.AddressSpec._
import forms.declaration.EntityDetails
import forms.declaration.carrier.CarrierDetails
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD}
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.i18n.MessagesApi
import views.declaration.spec.{AddressViewSpec, PageWithButtonsSpec}
import views.html.declaration.carrier_details
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class CarrierDetailsViewSpec extends AddressViewSpec with PageWithButtonsSpec with Injector {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  def form(implicit request: JourneyRequest[_]): Form[CarrierDetails] = CarrierDetails.form(request.declarationType)

  val page = instanceOf[carrier_details]

  override val typeAndViewInstance = (STANDARD, page(CarrierDetails.form(STANDARD))(_, _))

  def createView(form: Form[CarrierDetails])(implicit request: JourneyRequest[_]): Document =
    page(form)(request, messages)

  "Carrier Details View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())

      messages must haveTranslationFor("declaration.address.fullName")
      messages must haveTranslationFor("declaration.address.fullName.empty")
      messages must haveTranslationFor("declaration.address.fullName.error")
      messages must haveTranslationFor("declaration.address.addressLine")
      messages must haveTranslationFor("declaration.address.addressLine.empty")
      messages must haveTranslationFor("declaration.address.addressLine.error")
      messages must haveTranslationFor("declaration.address.townOrCity")
      messages must haveTranslationFor("declaration.address.townOrCity.empty")
      messages must haveTranslationFor("declaration.address.townOrCity.error")
      messages must haveTranslationFor("declaration.address.postCode")
      messages must haveTranslationFor("declaration.address.postCode.empty")
      messages must haveTranslationFor("declaration.address.postCode.error")
      messages must haveTranslationFor("declaration.address.country")
      messages must haveTranslationFor("declaration.address.country.empty")
      messages must haveTranslationFor("declaration.address.country.error")
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val view = createView(form)

      "display 'Back' button that links to 'Carrier Eori Number' page" in {
        val backButton = createView(form).getElementById("back-link")
        backButton.text mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe CarrierEoriNumberController.displayPage.url
      }

      "display section header" in {
        view.getElementById("section-header").text must include(messages("declaration.section.2"))
      }

      "display the expected page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.carrierAddress.title")
      }

      "display the expected notification banner" in {
        val banner = view.getElementsByClass("govuk-notification-banner").get(0)

        val title = banner.getElementsByClass("govuk-notification-banner__title").text
        title mustBe messages("declaration.carrierAddress.notification.title")

        val content = banner.getElementsByClass("govuk-notification-banner__content").get(0)
        content.text mustBe messages("declaration.carrierAddress.notification.body")
      }

      "display the expected body" in {
        val paragraphs = view.getElementsByClass("govuk-body")

        paragraphs.get(1).text mustBe messages(s"declaration.carrierAddress.body.1")

        val placeholder = messages(s"declaration.carrierAddress.body.2.link")
        paragraphs.get(2).text mustBe messages(s"declaration.carrierAddress.body.2", placeholder)

        val link = paragraphs.get(2).child(0)
        link.tagName mustBe "a"
        link.attr("target") mustBe "_blank"
        link must haveHref("https://find-and-update.company-information.service.gov.uk")
      }

      "display empty input with label for Full name" in {
        view.getElementsByAttributeValue("for", "details_address_fullName").first.text mustBe messages("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {
        view.getElementsByAttributeValue("for", "details_address_addressLine").first.text mustBe messages("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {
        view.getElementsByAttributeValue("for", "details_address_townOrCity").first.text mustBe messages("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {
        view.getElementsByClass("govuk-hint").first.text mustBe messages("declaration.address.postCode.hint")
        view.getElementsByAttributeValue("for", "details_address_postCode").first.text mustBe messages("declaration.address.postCode")
        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {
        view.getElementsByClass("govuk-hint").last.text mustBe messages("declaration.country.dropdown.hint")
        view.getElementsByAttributeValue("for", "details_address_country").first.text mustBe messages("declaration.address.country")
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display the expected tariff details" in {
        val declType = if (request.isType(CLEARANCE)) "clearance" else "common"

        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.text mustBe messages(s"tariff.expander.title.$declType")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first

        val prefix = "tariff.declaration.carrierAddress"
        val expectedText = messages(s"$prefix.$declType.text", messages(s"$prefix.$declType.linkText.0"))

        val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
        actualText mustBe removeLineBreakIfAny(expectedText)
      }

      checkAllSaveButtonsAreDisplayed(createView(form))
    }
  }

  "Carrier Details View when filled with valid data" should {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display data in Business address inputs" in {
        val entityDetails = EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "Ukraine")))
        val view = createView(form.fill(CarrierDetails(entityDetails)))

        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "Ukraine"
      }
    }
  }

  "Carrier Details View when filled with invalid data" should {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display error for empty fullName" in {
        assertElementIsIncorrect(validAddress.copy(fullName = ""), "fullName", "empty")
      }

      "display error for incorrect fullName" in {
        assertElementIsIncorrect(validAddress.copy(fullName = illegalField), "fullName", "error")
      }

      "display error for fullName too long" in {
        assertElementIsIncorrect(validAddress.copy(fullName = fieldWithLengthOver35), "fullName", "length")
      }

      "display error for empty addressLine" in {
        assertElementIsIncorrect(validAddress.copy(addressLine = ""), "addressLine", "empty")
      }

      "display error for incorrect addressLine" in {
        assertElementIsIncorrect(validAddress.copy(addressLine = illegalField), "addressLine", "error")
      }

      "display error for addressLine too long" in {
        assertElementIsIncorrect(validAddress.copy(addressLine = fieldWithLengthOver70), "addressLine", "length")
      }

      "display error for empty townOrCity" in {
        assertElementIsIncorrect(validAddress.copy(townOrCity = ""), "townOrCity", "empty")
      }

      "display error for incorrect townOrCity" in {
        assertElementIsIncorrect(validAddress.copy(townOrCity = illegalField), "townOrCity", "error")
      }

      "display error for townOrCity too long" in {
        assertElementIsIncorrect(validAddress.copy(townOrCity = fieldWithLengthOver35), "townOrCity", "length")
      }

      "display error for empty postCode" in {
        assertElementIsIncorrect(validAddress.copy(postCode = ""), "postCode", "empty")
      }

      "display error for incorrect postCode" in {
        assertElementIsIncorrect(validAddress.copy(postCode = illegalField), "postCode", "error")
      }

      "display error for postCode too long" in {
        assertElementIsIncorrect(validAddress.copy(postCode = fieldWithLengthOver35), "postCode", "length")
      }

      "display error for empty country" in {
        assertElementIsIncorrect(validAddress.copy(country = ""), "country", "empty")
      }

      "display error for incorrect country" in {
        assertElementIsIncorrect(validAddress.copy(country = "Barcelona"), "country", "error")
      }

      "display errors when everything except Full name is empty" in {
        assertElementsAreIncorrect(Address("Marco Polo", "", "", "", ""), List("addressLine", "townOrCity", "postCode", "country"), "empty")
      }

      "display errors when everything is empty" in {
        assertElementsAreIncorrect(emptyAddress, List("fullName", "addressLine", "townOrCity", "postCode", "country"), "empty")
      }

      "display errors when everything except country has illegal length" in {
        assertElementsAreIncorrect(addressWithIllegalLengths, List("fullName", "addressLine", "townOrCity", "postCode"), "length")
      }

      "display errors when everything is incorrect" in {
        assertElementsAreIncorrect(invalidAddress, List("fullName", "addressLine", "townOrCity", "postCode", "country"), "error")
      }
    }

    def assertElementIsIncorrect(address: Address, field: String, errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
      val view = createView(form.fillAndValidate(CarrierDetails(EntityDetails(None, Some(address)))))
      assertIncorrectElement(view, field, errorKey)
    }

    def assertElementsAreIncorrect(address: Address, fields: List[String], errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
      val view = createView(form.fillAndValidate(CarrierDetails(EntityDetails(None, Some(address)))))
      assertIncorrectElements(view, fields, errorKey)
    }
  }
}
