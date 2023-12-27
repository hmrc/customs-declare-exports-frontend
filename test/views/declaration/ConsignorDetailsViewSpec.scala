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

package views.declaration

import base.Injector
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.common.{Address, AddressSpec}
import forms.declaration.EntityDetails
import forms.declaration.consignor.ConsignorDetails
import forms.declaration.consignor.ConsignorDetails.form
import models.DeclarationType.CLEARANCE
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.i18n.MessagesApi
import views.declaration.spec.{AddressViewSpec, PageWithButtonsSpec}
import views.html.declaration.consignor_details
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class ConsignorDetailsViewSpec extends AddressViewSpec with Injector with PageWithButtonsSpec {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  val page = instanceOf[consignor_details]

  override val typeAndViewInstance = (CLEARANCE, page(form)(_, _))

  def createView(frm: Form[ConsignorDetails] = form)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Consignor Details View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.consignorAddress.title")
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
      messages must haveTranslationFor("site.save_and_continue")
      messages must haveTranslationFor("tariff.expander.title.clearance")
    }

    onClearance { implicit request =>
      "display page title" in {
        createView().getElementsByClass("govuk-fieldset__heading").first().text() mustBe messages("declaration.consignorAddress.title")
      }

      "display section header" in {
        val view = createView()

        view.getElementById("section-header").text() must include(messages("declaration.section.2"))
      }

      "display empty input with label for Full name" in {
        val view = createView()

        view.getElementsByAttributeValue("for", "details_address_fullName").first().text() mustBe messages("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {
        val view = createView()

        view.getElementsByAttributeValue("for", "details_address_addressLine").first().text() mustBe messages("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {
        val view = createView()

        view.getElementsByAttributeValue("for", "details_address_townOrCity").first().text() mustBe messages("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {
        val view = createView()

        view.getElementsByAttributeValue("for", "details_address_postCode").first().text() mustBe messages("declaration.address.postCode")
        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {
        val view = createView()

        view.getElementsByAttributeValue("for", "details_address_country").first().text() mustBe messages("declaration.address.country")
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }

  "Consignor Details View with invalid input" should {
    import AddressSpec._

    onClearance { implicit request =>
      "display error for empty fullName" in {
        assertIncorrectView(validAddress.copy(fullName = ""), "fullName", "empty")
      }

      "display error for incorrect fullName" in {
        assertIncorrectView(validAddress.copy(fullName = illegalField), "fullName", "error")
      }

      "display error for fullName too long" in {
        assertIncorrectView(validAddress.copy(fullName = fieldWithLengthOver35), "fullName", "length")
      }

      "display error for empty addressLine" in {
        assertIncorrectView(validAddress.copy(addressLine = ""), "addressLine", "empty")
      }

      "display error for incorrect addressLine" in {
        assertIncorrectView(validAddress.copy(addressLine = illegalField), "addressLine", "error")
      }

      "display error for addressLine too long" in {
        assertIncorrectView(validAddress.copy(addressLine = fieldWithLengthOver70), "addressLine", "length")
      }

      "display error for empty townOrCity" in {
        assertIncorrectView(validAddress.copy(townOrCity = ""), "townOrCity", "empty")
      }

      "display error for incorrect townOrCity" in {
        assertIncorrectView(validAddress.copy(townOrCity = illegalField), "townOrCity", "error")
      }

      "display error for townOrCity too long" in {
        assertIncorrectView(validAddress.copy(townOrCity = fieldWithLengthOver35), "townOrCity", "length")
      }

      "display error for empty postCode" in {
        assertIncorrectView(validAddress.copy(postCode = ""), "postCode", "empty")
      }

      "display error for incorrect postCode" in {
        assertIncorrectView(validAddress.copy(postCode = illegalField), "postCode", "error")
      }

      "display error for postCode too long" in {
        assertIncorrectView(validAddress.copy(postCode = fieldWithLengthOver35), "postCode", "length")
      }

      "display error for empty country" in {
        assertIncorrectView(validAddress.copy(country = ""), "country", "empty")
      }

      "display error for incorrect country" in {
        assertIncorrectView(validAddress.copy(country = "Barcelona"), "country", "error")
      }

      "display errors when everything except Full name is empty" in {
        assertIncorrectElements(Address("Marco Polo", "", "", "", ""), List("addressLine", "townOrCity", "postCode", "country"), "empty")
      }

      "display errors when everything is empty" in {
        assertIncorrectElements(emptyAddress, List("fullName", "addressLine", "townOrCity", "postCode", "country"), "empty")
      }

      "display errors when everything except country has illegal length" in {
        assertIncorrectElements(addressWithIllegalLengths, List("fullName", "addressLine", "townOrCity", "postCode"), "length")
      }

      "display errors when everything is incorrect" in {
        assertIncorrectElements(invalidAddress, List("fullName", "addressLine", "townOrCity", "postCode", "country"), "error")
      }
    }
  }

  "Consignor Details View when filled" should {
    onClearance { implicit request =>
      "display data in Business address inputs" in {

        val form = ConsignorDetails.form
          .fill(ConsignorDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "Ukraine")))))
        val view = createView(form)

        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "Ukraine"
      }
    }
  }

  "Consignor Details View back links" should {
    onClearance { implicit request =>
      "display 'Back' button that links to 'Consignor Eori Number' page" in {
        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe routes.ConsignorEoriNumberController.displayPage.url
      }
    }
  }

  private def assertIncorrectView(address: Address, field: String, errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView(form.fillAndValidate(ConsignorDetails(EntityDetails(None, Some(address)))))
    assertIncorrectElement(view, field, errorKey)
  }

  private def assertIncorrectElements(address: Address, fields: List[String], errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView(form.fillAndValidate(ConsignorDetails(EntityDetails(None, Some(address)))))
    assertIncorrectElements(view, fields, errorKey)
  }
}
