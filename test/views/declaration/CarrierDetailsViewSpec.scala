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
import forms.common.Address
import forms.declaration.EntityDetails
import forms.declaration.carrier.CarrierDetails
import helpers.views.declaration.CommonMessages
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD}
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.carrier_details
import views.tags.ViewTest

@ViewTest
class CarrierDetailsViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val carrierDetailsPage = instanceOf[carrier_details]
  val allFields = Seq("fullName", "addressLine", "townOrCity", "addressLine", "postCode", "country")
  val validAddress = Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "England")
  val emptyAddress = Address("", "", "", "", "")
  val randomAddress = Address(
    TestHelper.createRandomAlphanumericString(71),
    TestHelper.createRandomAlphanumericString(71),
    TestHelper.createRandomAlphanumericString(71),
    TestHelper.createRandomAlphanumericString(71),
    TestHelper.createRandomAlphanumericString(71)
  )

  def assertAllAddressValidation(errorKey: String, fieldName: String, address: Address, fields: Seq[String] = allFields)(
    implicit request: JourneyRequest[_]
  ): Unit = {

    val view = createViewWithAddressError(address)
    fields.filterNot(_ == fieldName).foreach { key =>
      view must containErrorElementWithTagAndHref("a", s"#details_address_$key")
      view must containErrorElementWithMessageKey(s"declaration.address.$key.$errorKey")
    }
  }

  def assertAddressValidation(errorKey: String, fieldName: String, address: Address)(implicit request: JourneyRequest[_]): Unit = {

    val view = createViewWithAddressError(address)
    view must containErrorElementWithTagAndHref("a", s"#details_address_$fieldName")
    view must containErrorElementWithMessageKey(s"declaration.address.$fieldName.$errorKey")
  }

  def createViewWithAddressError(address: Address)(implicit request: JourneyRequest[_]): Document = {
    val view = createView(
      CarrierDetails
        .form(request.declarationType)
        .fillAndValidate(CarrierDetails(EntityDetails(None, Some(address))))
    )
    view must haveGovukGlobalErrorSummary
    view
  }

  private def createView(form: Form[CarrierDetails])(implicit request: JourneyRequest[_]): Document =
    carrierDetailsPage(Mode.Normal, form)(request, messages)

  "Carrier Details View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.carrierAddress.title")
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
      messages must haveTranslationFor("site.details.summary_text_this")
      messages must haveTranslationFor("declaration.carrierAddress.help-item1")
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      val defaultForm = CarrierDetails.form(request.declarationType)
      val view = createView(defaultForm)

      "display page title" in {
        view.getElementsByClass("govuk-fieldset__heading").first().text() mustBe messages("declaration.carrierAddress.title")
      }

      "display section header" in {
        view.getElementById("section-header").text() must include(messages("declaration.section.2"))
      }

      "display empty input with label for Full name" in {
        view.getElementsByAttributeValue("for", "details_address_fullName").first().text() mustBe messages("declaration.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {
        view.getElementsByAttributeValue("for", "details_address_addressLine").first().text() mustBe messages("declaration.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {
        view.getElementsByAttributeValue("for", "details_address_townOrCity").first().text() mustBe messages("declaration.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {
        view.getElementsByAttributeValue("for", "details_address_postCode").first().text() mustBe messages("declaration.address.postCode")
        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {
        view.getElementsByAttributeValue("for", "details_address_country").first().text() mustBe messages("declaration.address.country")
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        val button = view.getElementById("submit_and_return")
        button.text() mustBe messages(saveAndReturnCaption)
        button.attr("name") mustBe SaveAndReturn.toString
      }
    }
  }

  "Carrier Details View with invalid input" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display error for empty Full name" in {

        val emptyFullNameAddress = validAddress.copy(fullName = "")
        assertAddressValidation("empty", "fullName", emptyFullNameAddress)
      }

      "display error for incorrect Full name" in {

        val invalidFullNameAddress = validAddress.copy(fullName = TestHelper.createRandomAlphanumericString(71))
        assertAddressValidation("error", "fullName", invalidFullNameAddress)
      }

      "display error for empty Address Line" in {

        val emptyAddressLineAddress = validAddress.copy(addressLine = "")
        assertAddressValidation("empty", "addressLine", emptyAddressLineAddress)
      }

      "display error for incorrect Address Line" in {

        val invalidAddressLineAddress = validAddress.copy(addressLine = TestHelper.createRandomAlphanumericString(71))
        assertAddressValidation("error", "addressLine", invalidAddressLineAddress)
      }

      "display error for empty Town or city" in {

        val emptyTownOrCityAddress = validAddress.copy(townOrCity = "")
        assertAddressValidation("empty", "townOrCity", emptyTownOrCityAddress)
      }

      "display error for incorrect Town or city" in {

        val invalidTownOrCityAddress = validAddress.copy(townOrCity = TestHelper.createRandomAlphanumericString(71))
        assertAddressValidation("error", "townOrCity", invalidTownOrCityAddress)
      }

      "display error for empty Postcode" in {

        val emptyPostCodeAddress = validAddress.copy(postCode = "")
        assertAddressValidation("empty", "postCode", emptyPostCodeAddress)
      }

      "display error for incorrect Postcode" in {

        val invalidPostCodeAddress = validAddress.copy(postCode = TestHelper.createRandomAlphanumericString(71))
        assertAddressValidation("error", "postCode", invalidPostCodeAddress)
      }

      "display error for empty Country" in {

        val emptyCountryAddress = validAddress.copy(country = "")
        assertAddressValidation("empty", "country", emptyCountryAddress)
      }

      "display error for incorrect Country" in {

        val invalidCountryAddress = validAddress.copy(country = TestHelper.createRandomAlphanumericString(71))
        assertAddressValidation("error", "country", invalidCountryAddress)
      }

      "display errors" when {

        "everything except Full name is empty" in {

          val fullNameOnlyAddress = emptyAddress.copy(fullName = "Marco polo")
          assertAllAddressValidation("empty", "fullName", fullNameOnlyAddress)
        }

        "everything except Country is empty" in {

          val countryOnlyAddress = emptyAddress.copy(country = "Ukraine")
          assertAllAddressValidation("empty", "country", countryOnlyAddress)
        }

        "everything except Full name is incorrect" in {

          val fullNameOnlyAddress = randomAddress.copy(fullName = "Marco polo")
          assertAllAddressValidation("error", "fullName", fullNameOnlyAddress)
        }

        "everything except Country is incorrect" in {

          val countryOnlyAddress = randomAddress.copy(country = "Ukraine")
          assertAllAddressValidation("error", "country", countryOnlyAddress)
        }
      }

    }
  }

  "Carrier Details View when filled" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display data in Business address inputs" in {

        val form = CarrierDetails
          .form(request.declarationType)
          .fill(CarrierDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "Ukraine")))))
        val view = createView(form)

        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "Ukraine"
      }
    }
  }

  "Carrier Details View back links" should {

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
      "display 'Back' button that links to 'Carrier Eori Number' page" in {

        val backButton = createView(CarrierDetails.form(request.declarationType)).getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.CarrierEoriNumberController.displayPage().url
      }
    }
  }
}
