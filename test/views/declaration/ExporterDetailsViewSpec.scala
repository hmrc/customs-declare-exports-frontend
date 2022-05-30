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
import controllers.declaration.routes
import forms.common.{Address, AddressSpec}
import forms.declaration.EntityDetails
import forms.declaration.exporter.ExporterDetails
import models.DeclarationType._
import models.Mode
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{Assertion, BeforeAndAfterEach}
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.AddressViewSpec
import views.helpers.CommonMessages
import views.html.declaration.exporter_address
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class ExporterDetailsViewSpec extends AddressViewSpec with CommonMessages with Stubs with Injector with BeforeAndAfterEach {

  private val exporterDetailsPage = instanceOf[exporter_address]
  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private def form()(implicit request: JourneyRequest[_]): Form[ExporterDetails] = ExporterDetails.form(request.declarationType)

  private def createView(form: Form[ExporterDetails], mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Document =
    exporterDetailsPage(mode, form)(request, messages)

  "Exporter Details View on empty page" should {

    onEveryDeclarationJourney() { implicit request =>
      "display same page title as header" in {
        val viewWithMessage = createView(form())
        viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
      }

      "display section header" in {

        createView(form()).getElementById("section-header").text() must include(messages("declaration.section.2"))
      }

      "display empty input with label for Full name" in {

        val view = createView(form())

        view.getElementsByAttributeValue("for", "details_address_fullName").text() mustBe messages("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {

        val view = createView(form())
        view.getElementsByAttributeValue("for", "details_address_addressLine").text() mustBe messages("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {

        val view = createView(form())
        view.getElementsByAttributeValue("for", "details_address_townOrCity").text() mustBe messages("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {

        val view = createView(form())
        view.getElementsByAttributeValue("for", "details_address_postCode").text() mustBe messages("declaration.address.postCode")

        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {

        val view = createView(form())

        view.getElementsByAttributeValue("for", "details_address_country").text() mustBe messages("declaration.address.country")

        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      val createViewWithMode: Mode => Document = mode => createView(form(), mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }

    onEveryDeclarationJourney() { implicit request =>
      "display 'Back' button that links to 'Exporter Eori Number' page" in {

        val backButton = createView(form()).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.ExporterEoriNumberController.displayPage().url
      }
    }
  }

  "Exporter Details View for invalid input" should {

    import AddressSpec._

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
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
        assertIncorrectView(validAddress.copy(addressLine = fieldWithLengthOver35), "addressLine", "length")
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

  "Exporter Details View when filled" should {

    onEveryDeclarationJourney() { implicit request =>
      "display data in Business address inputs" in {

        val view = createView(
          form()
            .fill(ExporterDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
        )

        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "test4"
      }
    }
  }

  private def assertIncorrectView(address: Address, field: String, errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView(form().fillAndValidate(ExporterDetails(EntityDetails(None, Some(address)))))
    assertIncorrectElement(view, field, errorKey)
  }

  private def assertIncorrectElements(address: Address, fields: List[String], errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView(form().fillAndValidate(ExporterDetails(EntityDetails(None, Some(address)))))
    assertIncorrectElements(view, fields, errorKey)
  }
}
