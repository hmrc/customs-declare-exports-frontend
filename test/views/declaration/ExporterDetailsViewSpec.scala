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

import base.TestHelper
import controllers.util.SaveAndReturn
import forms.common.Address
import forms.declaration.{EntityDetails, ExporterDetails}
import helpers.views.declaration.{CommonMessages, ExporterDetailsMessages}
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.exporter_details
import views.tags.ViewTest

@ViewTest
class ExporterDetailsViewSpec extends ViewSpec with ExporterDetailsMessages with CommonMessages {

  private val form: Form[ExporterDetails] = ExporterDetails.form()
  private val exporterDetailsPage = app.injector.instanceOf[exporter_details]
  private def createView(form: Form[ExporterDetails] = form): Html =
    exporterDetailsPage(Mode.Normal, form)(fakeRequest, messages)

  "Exporter Details View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(title))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be(messages("Parties"))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("details_eori-label").text() must be(messages(consignorEori))
      view.getElementById("details_eori").attr("value") must be("")
    }

    "display empty input with label for Full name" in {

      val view = createView()

      view.getElementById("details_address_fullName-label").text() must be(messages(fullName))
      view.getElementById("details_address_fullName").attr("value") must be("")
    }

    "display empty input with label for Address" in {

      val view = createView()

      view.getElementById("details_address_addressLine-label").text() must be(messages(addressLine))
      view.getElementById("details_address_addressLine").attr("value") must be("")
    }

    "display empty input with label for Town or City" in {

      val view = createView()

      view.getElementById("details_address_townOrCity-label").text() must be(messages(townOrCity))
      view.getElementById("details_address_townOrCity").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      view.getElementById("details_address_postCode-label").text() must be(messages(postCode))
      view.getElementById("details_address_postCode").attr("value") must be("")
    }

    "display empty input with label for Country" in {

      val view = createView()

      view.getElementById("details.address.country-label").text() mustBe "Country"
      view.getElementById("details.address.country").attr("value") mustBe ""
    }

    "display 'Back' button that links to 'Consignment References' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/consignment-references")
    }

    "display 'Save and continue' button" in {
      val saveButton = createView().getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button" in {
      val saveButton = createView().getElementById("submit_and_return")
      saveButton.text() must be(messages(saveAndReturnCaption))
      saveButton.attr("name") must be(SaveAndReturn.toString)
    }
  }

  "Exporter Details View for invalid input" should {

    "display error when both EORI and business details are empty" in {

      val view = createView(ExporterDetails.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eoriOrAddressEmpty, "#details")

      view.select("#error-message-details-input").text() must be(messages(eoriOrAddressEmpty))
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(Some(TestHelper.createRandomAlphanumericString(19)), None)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eoriError, "#details_eori")

      view.select("#error-message-details_eori-input").text() must be(messages(eoriError))
    }

    "display error for empty Full name" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullNameEmpty, "#details_address_fullName")

      view.select("#error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
    }

    "display error for incorrect Full name" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(
                None,
                Some(
                  Address(TestHelper.createRandomAlphanumericString(71), "Test Street", "Leeds", "LS18BN", "England")
                )
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullNameError, "#details_address_fullName")

      view.select("#error-message-details_address_fullName-input").text() must be(messages(fullNameError))
    }

    "display error for empty Address" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLineEmpty, "#details_address_addressLine")

      view.select("#error-message-details_address_addressLine-input").text() must be(messages(addressLineEmpty))
    }

    "display error for incorrect Address" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(
                None,
                Some(Address("Marco Polo", TestHelper.createRandomAlphanumericString(71), "Leeds", "LS18BN", "England"))
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLineError, "#details_address_addressLine")

      view.select("#error-message-details_address_addressLine-input").text() must be(messages(addressLineError))
    }

    "display error for empty Town or city" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, townOrCityEmpty, "#details_address_townOrCity")

      view.select("#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
    }

    "display error for incorrect Town or city" in {

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
                    "Test Street",
                    TestHelper.createRandomAlphanumericString(71),
                    "LS18BN",
                    "England"
                  )
                )
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, townOrCityError, "#details_address_townOrCity")

      view.select("#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
    }

    "display error for empty Postcode" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England"))))
          )
      )
      checkErrorsSummary(view)
      checkErrorLink(view, 1, postCodeEmpty, "#details_address_postCode")

      view.select("#error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
    }

    "display error for incorrect Postcode" in {

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
                    "Test Street",
                    "Leeds",
                    TestHelper.createRandomAlphanumericString(71),
                    "England"
                  )
                )
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, postCodeError, "#details_address_postCode")

      view.select("#error-message-details_address_postCode-input").text() must be(messages(postCodeError))
    }

    "display error for empty Country" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", ""))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryEmpty, "#details_address_country")

      view.select("span.error-message").text() must be(messages(countryEmpty))
    }

    "display error for incorrect Country" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(
            ExporterDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#details_address_country")

      view.select("span.error-message").text() must be(messages(countryError))
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLineEmpty, "#details_address_addressLine")
      checkErrorLink(view, 2, townOrCityEmpty, "#details_address_townOrCity")
      checkErrorLink(view, 3, postCodeEmpty, "#details_address_postCode")
      checkErrorLink(view, 4, countryEmpty, "#details_address_country")

      view.select("#error-message-details_address_addressLine-input").text() must be(messages(addressLineEmpty))
      view.select("#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
      view.select("#error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      view.select("span.error-message").text() must be(messages(countryEmpty))
    }

    "display errors when everything except Country is empty" in {

      val view = createView(
        ExporterDetails
          .form()
          .fillAndValidate(ExporterDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullNameEmpty, "#details_address_fullName")
      checkErrorLink(view, 2, addressLineEmpty, "#details_address_addressLine")
      checkErrorLink(view, 3, townOrCityEmpty, "#details_address_townOrCity")
      checkErrorLink(view, 4, postCodeEmpty, "#details_address_postCode")

      view.select("#error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
      view.select("#error-message-details_address_addressLine-input").text() must be(messages(addressLineEmpty))
      view.select("#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
      view.select("#error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
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
      checkErrorLink(view, 1, addressLineError, "#details_address_addressLine")
      checkErrorLink(view, 2, townOrCityError, "#details_address_townOrCity")
      checkErrorLink(view, 3, postCodeError, "#details_address_postCode")
      checkErrorLink(view, 4, countryError, "#details_address_country")

      view.select("#error-message-details_address_addressLine-input").text() must be(messages(addressLineError))
      view.select("#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
      view.select("#error-message-details_address_postCode-input").text() must be(messages(postCodeError))
      view.select("span.error-message").text() must be(messages(countryError))
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
      checkErrorLink(view, 1, fullNameError, "#details_address_fullName")
      checkErrorLink(view, 2, addressLineError, "#details_address_addressLine")
      checkErrorLink(view, 3, townOrCityError, "#details_address_townOrCity")
      checkErrorLink(view, 4, postCodeError, "#details_address_postCode")

      view.select("#error-message-details_address_fullName-input").text() must be(messages(fullNameError))
      view.select("#error-message-details_address_addressLine-input").text() must be(messages(addressLineError))
      view.select("#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
      view.select("#error-message-details_address_postCode-input").text() must be(messages(postCodeError))
    }
  }

  "Exporter Details View when filled" should {

    "display data in EORI input" in {

      val form = ExporterDetails.form().fill(ExporterDetails(EntityDetails(Some("1234"), None)))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") must be("1234")
      view.getElementById("details_address_fullName").attr("value") must be("")
      view.getElementById("details_address_addressLine").attr("value") must be("")
      view.getElementById("details_address_townOrCity").attr("value") must be("")
      view.getElementById("details_address_postCode").attr("value") must be("")
      view.getElementById("details.address.country").attr("value") must be("")
    }

    "display data in Business address inputs" in {

      val form = ExporterDetails
        .form()
        .fill(ExporterDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") must be("")
      view.getElementById("details_address_fullName").attr("value") must be("test")
      view.getElementById("details_address_addressLine").attr("value") must be("test1")
      view.getElementById("details_address_townOrCity").attr("value") must be("test2")
      view.getElementById("details_address_postCode").attr("value") must be("test3")
      view.getElementById("details.address.country").attr("value") must be("test4")
    }

    "display data in both EORI and Business address inputs" in {

      val form = ExporterDetails
        .form()
        .fill(ExporterDetails(EntityDetails(Some("1234"), Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") must be("1234")
      view.getElementById("details_address_fullName").attr("value") must be("test")
      view.getElementById("details_address_addressLine").attr("value") must be("test1")
      view.getElementById("details_address_townOrCity").attr("value") must be("test2")
      view.getElementById("details_address_postCode").attr("value") must be("test3")
      view.getElementById("details.address.country").attr("value") must be("test4")
    }
  }
}
