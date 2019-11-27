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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.Address
import forms.declaration.{EntityDetails, ExporterDetails}
import helpers.views.declaration.{CommonMessages, ExporterDetailsMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.exporter_details
import views.tags.ViewTest

@ViewTest
class ExporterDetailsViewSpec extends UnitViewSpec with ExporterDetailsMessages with CommonMessages with Stubs with Injector {

  private val form: Form[ExporterDetails] = ExporterDetails.form()
  private val exporterDetailsPage = new exporter_details(mainTemplate)
  private def createView(form: Form[ExporterDetails] = form, messages: Messages = stubMessages()): Document =
    exporterDetailsPage(Mode.Normal, form)(request, messages)

  "Exporter Details View on empty page" should {

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {

      createView().getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("details_eori-label").text() mustBe messages(consignorEori)
      view.getElementById("details_eori").attr("value") mustBe empty
    }

    "display empty input with label for Full name" in {

      val view = createView()

      view.getElementById("details_address_fullName-label").text() mustBe messages(fullName)
      view.getElementById("details_address_fullName").attr("value") mustBe empty
    }

    "display empty input with label for Address" in {

      val view = createView()

      view.getElementById("details_address_addressLine-label").text() mustBe messages(addressLine)
      view.getElementById("details_address_addressLine").attr("value") mustBe empty
    }

    "display empty input with label for Town or City" in {

      val view = createView()

      view.getElementById("details_address_townOrCity-label").text() mustBe messages(townOrCity)
      view.getElementById("details_address_townOrCity").attr("value") mustBe empty
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      view.getElementById("details_address_postCode-label").text() mustBe messages(postCode)
      view.getElementById("details_address_postCode").attr("value") mustBe empty
    }

    "display empty input with label for Country" in {

      val view = createView()

      view.getElementById("details.address.country-label").text() mustBe messages("supplementary.address.country")
      view.getElementById("details.address.country").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Consignment References' page" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.ConsignmentReferencesController.displayPage().url
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() mustBe messages(saveAndReturnCaption)
      saveButton.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Exporter Details View for invalid input" should {

    "display error when both EORI and business details are empty" in {

      val view = createView(ExporterDetails.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details", "#details")

      view.select("#error-message-details-input").text() mustBe messages(eoriOrAddressEmpty)
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(Some(TestHelper.createRandomAlphanumericString(19)), None)))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_eori", "#details_eori")

      view.select("#error-message-details_eori-input").text() mustBe messages(eoriError)
    }

    "display error for empty Full name" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")

      view.select("#error-message-details_address_fullName-input").text() mustBe messages(fullNameEmpty)
    }

    "display error for incorrect Full name" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(None, Some(Address(TestHelper.createRandomAlphanumericString(71), "Test Street", "Leeds", "LS18BN", "England")))
            )
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")

      view.select("#error-message-details_address_fullName-input").text() mustBe messages(fullNameError)
    }

    "display error for empty Address" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")

      view.select("#error-message-details_address_addressLine-input").text() mustBe messages(addressLineEmpty)
    }

    "display error for incorrect Address" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(None, Some(Address("Marco Polo", TestHelper.createRandomAlphanumericString(71), "Leeds", "LS18BN", "England")))
            )
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")

      view.select("#error-message-details_address_addressLine-input").text() mustBe messages(addressLineError)
    }

    "display error for empty Town or city" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")

      view.select("#error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityEmpty)
    }

    "display error for incorrect Town or city" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", TestHelper.createRandomAlphanumericString(71), "LS18BN", "England")))
            )
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")

      view.select("#error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityError)
    }

    "display error for empty Postcode" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England")))))
      )
      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.select("#error-message-details_address_postCode-input").text() mustBe messages(postCodeEmpty)
    }

    "display error for incorrect Postcode" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", TestHelper.createRandomAlphanumericString(71), "England")))
            )
          )
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.select("#error-message-details_address_postCode-input").text() mustBe messages(postCodeError)
    }

    "display error for empty Country" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.select("span.error-message").text() mustBe messages(countryEmpty)
    }

    "display error for incorrect Country" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.select("span.error-message").text() mustBe messages(countryError)
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.select("#error-message-details_address_addressLine-input").text() mustBe messages(addressLineEmpty)
      view.select("#error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityEmpty)
      view.select("#error-message-details_address_postCode-input").text() mustBe messages(postCodeEmpty)
      view.select("span.error-message").text() mustBe messages(countryEmpty)
    }

    "display errors when everything except Country is empty" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
      )

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.select("#error-message-details_address_fullName-input").text() mustBe messages(fullNameEmpty)
      view.select("#error-message-details_address_addressLine-input").text() mustBe messages(addressLineEmpty)
      view.select("#error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityEmpty)
      view.select("#error-message-details_address_postCode-input").text() mustBe messages(postCodeEmpty)
    }

    "display errors when everything except Full name is incorrect" in {

      val view = createView(
        ExporterDetails
          .form()
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

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.select("#error-message-details_address_addressLine-input").text() mustBe messages(addressLineError)
      view.select("#error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityError)
      view.select("#error-message-details_address_postCode-input").text() mustBe messages(postCodeError)
      view.select("span.error-message").text() mustBe messages(countryError)
    }

    "display errors when everything except Country is incorrect" in {

      val view = createView(
        ExporterDetails
          .form()
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

      checkErrorsSummary(view)
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.select("#error-message-details_address_fullName-input").text() mustBe messages(fullNameError)
      view.select("#error-message-details_address_addressLine-input").text() mustBe messages(addressLineError)
      view.select("#error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityError)
      view.select("#error-message-details_address_postCode-input").text() mustBe messages(postCodeError)
    }
  }

  "Exporter Details View when filled" should {

    "display data in EORI input" in {

      val form = ExporterDetails.form().fill(ExporterDetails(EntityDetails(Some("1234"), None)))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") mustBe "1234"
      view.getElementById("details_address_fullName").attr("value") mustBe empty
      view.getElementById("details_address_addressLine").attr("value") mustBe empty
      view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      view.getElementById("details_address_postCode").attr("value") mustBe empty
      view.getElementById("details.address.country").attr("value") mustBe empty
    }

    "display data in Business address inputs" in {

      val form = ExporterDetails
        .form()
        .fill(ExporterDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") mustBe empty
      view.getElementById("details_address_fullName").attr("value") mustBe "test"
      view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
      view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
      view.getElementById("details_address_postCode").attr("value") mustBe "test3"
      view.getElementById("details.address.country").attr("value") mustBe "test4"
    }

    "display data in both EORI and Business address inputs" in {

      val form = ExporterDetails
        .form()
        .fill(ExporterDetails(EntityDetails(Some("1234"), Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") mustBe "1234"
      view.getElementById("details_address_fullName").attr("value") mustBe "test"
      view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
      view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
      view.getElementById("details_address_postCode").attr("value") mustBe "test3"
      view.getElementById("details.address.country").attr("value") mustBe "test4"
    }
  }
}
