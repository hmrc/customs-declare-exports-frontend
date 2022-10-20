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
import controllers.declaration.routes.{CarrierDetailsController, DeclarantExporterController, IsExsController, RepresentativeStatusController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Address, AddressSpec}
import forms.declaration.ConsigneeDetails.form
import forms.declaration.{ConsigneeDetails, DeclarantIsExporter, EntityDetails, IsExs}
import models.DeclarationType._
import models.codes.Country
import models.declaration.Parties
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.Assertion
import play.api.data.Form
import views.declaration.spec.{AddressViewSpec, PageWithButtonsSpec}
import views.html.declaration.consignee_details
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class ConsigneeDetailsViewSpec extends AddressViewSpec with PageWithButtonsSpec with Injector {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  val page = instanceOf[consignee_details]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView(frm: Form[ConsigneeDetails] = form(), mode: Mode = Normal)(implicit request: JourneyRequest[_]): Document =
    page(frm)(request, messages)

  "Consignee Details View on empty page" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("declaration.consignee.title")
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
      messages must haveTranslationFor("tariff.declaration.consignmentReferences.1.clearance.text")
      messages must haveTranslationFor("tariff.declaration.consigneeDetails.clearance.text")
    }

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display page title" in {
        view.getElementsByTag("h1").first().text() mustBe messages("declaration.consignee.title")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include(messages("declaration.section.2"))
      }

      "display empty input with label for fullName" in {
        view.getElementsByAttributeValue("for", "details_address_fullName").first().text() mustBe messages("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for addressLine" in {
        view.getElementsByAttributeValue("for", "details_address_addressLine").first().text() mustBe messages("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for townOrCity" in {
        view.getElementsByAttributeValue("for", "details_address_townOrCity").first().text() mustBe messages("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for postCode" in {
        view.getElementsByAttributeValue("for", "details_address_postCode").first().text() mustBe messages("declaration.address.postCode")
        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for country" in {
        view.getElementsByAttributeValue("for", "details_address_country").first().text() mustBe messages("declaration.address.country")
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }
  }

  "Consignee Details View with invalid input" should {

    import AddressSpec._

    onEveryDeclarationJourney() { implicit request =>
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

  "Consignee Details View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in Business address inputs" in {
        val view = createView(form().fill(ConsigneeDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "Ukraine"))))))

        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "Ukraine"
      }
    }
  }

  "Consignee Details View back links" should {
    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "display 'Back' button that links to 'Carrier Details' page" in {
        val backButton = createView().getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe CarrierDetailsController.displayPage().url
      }
    }

    onClearance { implicit request =>
      "display 'Back' button that links to 'Carrier Details' page" in {
        val cachedParties = Parties(isExs = Some(IsExs(YesNoAnswers.yes)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe CarrierDetailsController.displayPage().url
      }

      "display 'Back' button that links to 'Is Exs?' page" in {
        val cachedParties = Parties(isExs = Some(IsExs(YesNoAnswers.no)), declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.yes)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe IsExsController.displayPage().url
      }

      "display 'Back' button that links to 'Representative Status' page" in {
        val cachedParties = Parties(isExs = Some(IsExs(YesNoAnswers.no)), declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.no)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe RepresentativeStatusController.displayPage().url
      }
    }

    onSupplementary { implicit request =>
      "display 'Back' button that links to 'Representative Status' page" in {
        val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.no)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe RepresentativeStatusController.displayPage().url
      }

      "display 'Back' button that links to 'Declarant is exporter?' page" in {
        val cachedParties = Parties(declarantIsExporter = Some(DeclarantIsExporter(YesNoAnswers.yes)))
        val requestWithCachedParties = journeyRequest(request.cacheModel.copy(parties = cachedParties))

        val backButton = createView()(requestWithCachedParties).getElementById("back-link")

        backButton.text() mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe DeclarantExporterController.displayPage().url
      }
    }
  }

  private def assertIncorrectView(address: Address, field: String, errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView(form().fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(address)))))
    assertIncorrectElement(view, field, errorKey)
  }

  private def assertIncorrectElements(address: Address, fields: List[String], errorKey: String)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView(form().fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(address)))))
    assertIncorrectElements(view, fields, errorKey)
  }
}
