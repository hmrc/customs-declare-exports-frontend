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

package views.declaration

import base.{Injector, TestHelper}
import forms.declaration.RepresentativeDetails
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.representative_details
import views.tags.ViewTest

@ViewTest
class RepresentativeDetailsViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new representative_details(mainTemplate)
  private val form: Form[RepresentativeDetails] = RepresentativeDetails.form()
  private def createView(mode: Mode = Mode.Normal, form: Form[RepresentativeDetails] = form): Document =
    page(mode, form)(journeyRequest(), stubMessages())

  "Representative Details View on empty page" should {
    val view = createView()

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("supplementary.representative.header")
      messages must haveTranslationFor("supplementary.representative.eori.info")
      messages must haveTranslationFor("supplementary.address.fullName")
      messages must haveTranslationFor("supplementary.address.addressLine")
      messages must haveTranslationFor("supplementary.address.postCode")
      messages must haveTranslationFor("supplementary.address.townOrCity")
      messages must haveTranslationFor("supplementary.representative.representationType.error.empty")
      messages must haveTranslationFor("supplementary.eori.error")
      messages must haveTranslationFor("supplementary.address.fullName.empty")
      messages must haveTranslationFor("supplementary.address.fullName.error")
      messages must haveTranslationFor("supplementary.address.addressLine.empty")
      messages must haveTranslationFor("supplementary.address.addressLine.error")
      messages must haveTranslationFor("supplementary.address.townOrCity.empty")
      messages must haveTranslationFor("supplementary.address.townOrCity.error")
      messages must haveTranslationFor("supplementary.address.postCode.empty")
      messages must haveTranslationFor("supplementary.address.postCode.error")
      messages must haveTranslationFor("supplementary.address.country.empty")
      messages must haveTranslationFor("supplementary.address.country.error")
      messages must haveTranslationFor("supplementary.address.townOrCity.empty")
    }

    "display page title" in {
      view.getElementById("title").text() must be("supplementary.representative.header")
    }

    "display empty input with label for EORI" in {
      view.getElementById("details_eori-label").text() must be("supplementary.representative.eori.info")
      view.getElementById("details_eori").attr("value") must be("")
    }

    "display empty input with label for Full name" in {
      view.getElementById("details_address_fullName-label").text() must be("supplementary.address.fullName")
      view.getElementById("details_address_fullName").attr("value") must be("")
    }

    "display empty input with label for Address" in {
      view.getElementById("details_address_addressLine-label").text() must be("supplementary.address.addressLine")
      view.getElementById("details_address_addressLine").attr("value") must be("")
    }

    "display empty input with label for Town or City" in {
      view.getElementById("details_address_townOrCity-label").text() must be("supplementary.address.townOrCity")
      view.getElementById("details_address_townOrCity").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {
      view.getElementById("details_address_postCode-label").text() must be("supplementary.address.postCode")
      view.getElementById("details_address_postCode").attr("value") must be("")
    }

    "display empty input with label for Country" in {
      view.getElementById("details.address.country-label").text() mustBe "supplementary.address.country"
      view.getElementById("details.address.country").attr("value") mustBe ""
    }

    "display three radio buttons with description (not selected)" in {

      val view = createView(form = RepresentativeDetails.form().fill(RepresentativeDetails(None, None)))

      val optionDirect = view.getElementById("statusCode_direct")
      optionDirect.attr("checked") must be("")

      val optionDirectLabel = view.select("#statusCode>div:nth-child(2)>label>span")
      optionDirectLabel.text() must include("")

      val optionIndirect = view.getElementById("statusCode_indirect")
      optionIndirect.attr("checked") must be("")

      val optionIndirectLabel = view.select("#statusCode>div:nth-child(3)>label>span")
      optionIndirectLabel.text() must include("")
    }

    "display 'Back' button that links to 'Declarant Details' page" in {

      val backButton = view.getElementById("link-back")

      backButton.text() must be("site.back")
      backButton.getElementById("link-back") must haveHref(
        controllers.declaration.routes.DeclarantDetailsController.displayPage(Mode.Normal)
      )
    }

    "display 'Save and continue' button on page" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() must be("site.save_and_continue")
    }

    "display 'Save and return' button on page" in {
      val saveAndReturnButton = view.getElementById("submit_and_return")
      saveAndReturnButton.text() must be("site.save_and_come_back_later")
    }
  }

  "Representative Details View for invalid input" can {

    "status is not selected" when {

      "display errors when only EORI is entered" in {

        val view = createView(
          form = RepresentativeDetails.adjustErrors(
            RepresentativeDetails
              .form()
              .bind(
                Map(
                  "details.eori" -> "1234",
                  "details.address.fullName" -> "",
                  "details.address.addressLine" -> "",
                  "details.address.townOrCity" -> "",
                  "details.address.postCode" -> "",
                  "details.address.country" -> "",
                  "statusCode" -> ""
                )
              )
          )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("statusCode", "#statusCode")

        view.getElementById("error-message-statusCode-input").text() must be(
          "supplementary.representative.representationType.error.empty"
        )
      }

      "display errors when only address is entered" in {

        val view = createView(
          form = RepresentativeDetails.adjustErrors(
            RepresentativeDetails
              .form()
              .bind(
                Map(
                  "details.eori" -> "",
                  "details.address.fullName" -> "Test",
                  "details.address.addressLine" -> "Test",
                  "details.address.townOrCity" -> "Test",
                  "details.address.postCode" -> "Test",
                  "details.address.country" -> "Poland",
                  "statusCode" -> ""
                )
              )
          )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("statusCode", "#statusCode")

        view.getElementById("error-message-statusCode-input").text() must be(
          "supplementary.representative.representationType.error.empty"
        )
      }

      "display errors when EORI is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> TestHelper.createRandomAlphanumericString(50),
                "details.address.fullName" -> "",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "",
                "details.address.country" -> "",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details", "#details_eori")

        view.getElementById("error-message-details_eori-input").text() must be("supplementary.eori.error")
      }

      "display errors for empty Full name" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "",
                "details.address.addressLine" -> "28 Test Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.empty"
        )
      }

      "display errors for incorrect Full name" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.addressLine" -> "28 Test Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.error"
        )
      }

      "display errors for empty Address" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.empty"
        )
      }

      "display errors for incorrect Address" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.error"
        )
      }

      "display errors for empty Town or city" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")

        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.empty"
        )
      }

      "display errors for incorrect Town or city" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")

        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.error"
        )
      }

      "display errors for empty Postcode" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "",
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.empty"
        )
      }

      "display errors for incorrect Postcode" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> TestHelper.createRandomAlphanumericString(10),
                "details.address.country" -> "Germany",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.error"
        )
      }

      "display errors for empty Country" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B89",
                "details.address.country" -> "",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.select("span.error-message").text() must be("supplementary.address.country.empty")
      }

      "display errors for incorrect Country" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B89",
                "details.address.country" -> "Purrtugal",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.select("span.error-message").text() must be("supplementary.address.country.error")
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "",
                "details.address.country" -> "",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.empty"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.empty"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.empty"
        )
        view.select("span.error-message").text() must be("supplementary.address.country.empty")
      }

      "display errors when everything except Country is empty" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "",
                "details.address.country" -> "Poland",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.empty"
        )
        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.empty"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.empty"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.empty"
        )
      }

      "display errors when everything except Full name is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.townOrCity" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.postCode" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.country" -> TestHelper.createRandomAlphanumericString(71),
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.error"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.error"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.error"
        )
        view.select("span.error-message").text() must be("supplementary.address.country.error")
      }

      "display errors when everything except Country is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.addressLine" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.townOrCity" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.postCode" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.country" -> "Poland",
                "statusCode" -> ""
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.error"
        )
        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.error"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.error"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.error"
        )
      }
    }

    "status is selected" when {

      "display errors when EORI is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> TestHelper.createRandomAlphanumericString(50),
                "details.address.fullName" -> "",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "",
                "details.address.country" -> "",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.eori", "#details_eori")

        view.getElementById("error-message-details_eori-input").text() must be("supplementary.eori.error")
      }

      "display errors for empty Full name" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "",
                "details.address.addressLine" -> "28 Test Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.empty"
        )
      }

      "display errors for incorrect Full name" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.addressLine" -> "28 Test Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.error"
        )
      }

      "display errors for empty Address" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.empty"
        )
      }

      "display errors for incorrect Address" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.error"
        )
      }

      "display errors for empty Town or city" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")

        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.empty"
        )
      }

      "display errors for incorrect Town or city" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.postCode" -> "LS1B82",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")

        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.error"
        )
      }

      "display errors for empty Postcode" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "",
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.empty"
        )
      }

      "display errors for incorrect Postcode" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> TestHelper.createRandomAlphanumericString(10),
                "details.address.country" -> "Germany",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.error"
        )
      }

      "display errors for empty Country" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B89",
                "details.address.country" -> "",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.select("span.error-message").text() must be("supplementary.address.country.empty")
      }

      "display errors for incorrect Country" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "28 Town Street",
                "details.address.townOrCity" -> "Leeds",
                "details.address.postCode" -> "LS1B89",
                "details.address.country" -> "Purrtugal",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.select("span.error-message").text() must be("supplementary.address.country.error")
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "",
                "details.address.country" -> "",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.empty"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.empty"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.empty"
        )
        view.select("span.error-message").text() must be("supplementary.address.country.empty")
      }

      "display errors when everything except Country is empty" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "",
                "details.address.addressLine" -> "",
                "details.address.townOrCity" -> "",
                "details.address.postCode" -> "",
                "details.address.country" -> "Poland",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.empty"
        )
        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.empty"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.empty"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.empty"
        )
      }

      "display errors when everything except Full name is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> "John Smith",
                "details.address.addressLine" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.townOrCity" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.postCode" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.country" -> TestHelper.createRandomAlphanumericString(71),
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")
        haveFieldErrorLink("details.address.country", "#details_address_country")

        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.error"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.error"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.error"
        )
        view.select("span.error-message").text() must be("supplementary.address.country.error")
      }

      "display errors when everything except Country is incorrect" in {

        val view = createView(
          form = RepresentativeDetails
            .form()
            .bind(
              Map(
                "details.eori" -> "",
                "details.address.fullName" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.addressLine" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.townOrCity" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.postCode" -> TestHelper.createRandomAlphanumericString(71),
                "details.address.country" -> "Poland",
                "statusCode" -> "1"
              )
            )
        )

        checkErrorsSummary(view)
        haveFieldErrorLink("details.address.fullName", "#details_address_fullName")
        haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
        haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
        haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_fullName-input").text() must be(
          "supplementary.address.fullName.error"
        )
        view.getElementById("error-message-details_address_addressLine-input").text() must be(
          "supplementary.address.addressLine.error"
        )
        view.getElementById("error-message-details_address_townOrCity-input").text() must be(
          "supplementary.address.townOrCity.error"
        )
        view.getElementById("error-message-details_address_postCode-input").text() must be(
          "supplementary.address.postCode.error"
        )
      }
    }
  }

  "Representative Details View when filled" when {

    "declarant is selected" should {

      "display data in EORI input" in {

        val form = RepresentativeDetails
          .form()
          .bind(
            Map(
              "details.eori" -> "1234",
              "details.address.fullName" -> "",
              "details.address.addressLine" -> "",
              "details.address.townOrCity" -> "",
              "details.address.postCode" -> "",
              "details.address.country" -> "",
              "statusCode" -> "1"
            )
          )
        val view = createView(form = form)

        view.getElementById("details_eori").attr("value") must be("1234")
      }

      "display data in Business address inputs" in {

        val form = RepresentativeDetails
          .form()
          .bind(
            Map(
              "details.eori" -> "",
              "details.address.fullName" -> "test",
              "details.address.addressLine" -> "test1",
              "details.address.townOrCity" -> "test2",
              "details.address.postCode" -> "test3",
              "details.address.country" -> "test4",
              "statusCode" -> "1"
            )
          )
        val view = createView(form = form)

        view.getElementById("details_address_fullName").attr("value") must be("test")
        view.getElementById("details_address_addressLine").attr("value") must be("test1")
        view.getElementById("details_address_townOrCity").attr("value") must be("test2")
        view.getElementById("details_address_postCode").attr("value") must be("test3")
        view.getElementById("details.address.country").attr("value") must be("test4")
      }
    }

    "direct is selected" should {

      "display data in EORI input" in {

        val form = RepresentativeDetails
          .form()
          .bind(
            Map(
              "details.eori" -> "1234",
              "details.address.fullName" -> "",
              "details.address.addressLine" -> "",
              "details.address.townOrCity" -> "",
              "details.address.postCode" -> "",
              "details.address.country" -> "",
              "statusCode" -> "2"
            )
          )
        val view = createView(form = form)

        view.getElementById("details_eori").attr("value") must be("1234")
        view.getElementById("statusCode_direct").attr("checked") must be("checked")
      }

      "display data in Business address inputs" in {

        val form = RepresentativeDetails
          .form()
          .bind(
            Map(
              "details.eori" -> "",
              "details.address.fullName" -> "test",
              "details.address.addressLine" -> "test1",
              "details.address.townOrCity" -> "test2",
              "details.address.postCode" -> "test3",
              "details.address.country" -> "test4",
              "statusCode" -> "2"
            )
          )
        val view = createView(form = form)

        view.getElementById("details_address_fullName").attr("value") must be("test")
        view.getElementById("details_address_addressLine").attr("value") must be("test1")
        view.getElementById("details_address_townOrCity").attr("value") must be("test2")
        view.getElementById("details_address_postCode").attr("value") must be("test3")
        view.getElementById("details.address.country").attr("value") must be("test4")
        view.getElementById("statusCode_direct").attr("checked") must be("checked")
      }
    }

    "indirect is selected" should {

      "display data in EORI input" in {

        val form = RepresentativeDetails
          .form()
          .bind(
            Map(
              "details.eori" -> "1234",
              "details.address.fullName" -> "",
              "details.address.addressLine" -> "",
              "details.address.townOrCity" -> "",
              "details.address.postCode" -> "",
              "details.address.country" -> "",
              "statusCode" -> "3"
            )
          )
        val view = createView(form = form)

        view.getElementById("details_eori").attr("value") must be("1234")
        view.getElementById("statusCode_indirect").attr("checked") must be("checked")
      }

      "display data in Business address inputs" in {

        val form = RepresentativeDetails
          .form()
          .bind(
            Map(
              "details.eori" -> "",
              "details.address.fullName" -> "test",
              "details.address.addressLine" -> "test1",
              "details.address.townOrCity" -> "test2",
              "details.address.postCode" -> "test3",
              "details.address.country" -> "test4",
              "statusCode" -> "3"
            )
          )
        val view = createView(form = form)

        view.getElementById("details_address_fullName").attr("value") must be("test")
        view.getElementById("details_address_addressLine").attr("value") must be("test1")
        view.getElementById("details_address_townOrCity").attr("value") must be("test2")
        view.getElementById("details_address_postCode").attr("value") must be("test3")
        view.getElementById("details.address.country").attr("value") must be("test4")
        view.getElementById("statusCode_indirect").attr("checked") must be("checked")
      }
    }
  }
}
