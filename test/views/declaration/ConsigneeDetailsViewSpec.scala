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
import forms.common.Address
import forms.declaration.{ConsigneeDetails, EntityDetails}
import helpers.views.declaration.{CommonMessages, ConsigneeDetailsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.consignee_details
import views.tags.ViewTest

@ViewTest
class ConsigneeDetailsViewSpec extends ViewSpec with ConsigneeDetailsMessages with CommonMessages {

  val form: Form[ConsigneeDetails] = ConsigneeDetails.form()
  private def createView(form: Form[ConsigneeDetails] = form): Html = consignee_details(appConfig, form)

  "Consignee Details View" should {

    "have proper messages for labels" in {

      assertMessage(title, messages("supplementary.consignee.title"))
      assertMessage(hint, messages("supplementary.consignee.title.hint"))
    }
  }

  "Consignee Details View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display section header" in {

      val view = createView()

      getElementById(view, "section-header").text() must be(messages("Parties"))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      getElementById(view, "details_eori-label").text() must be(messages(eoriInfo))
      getElementById(view, "details_eori-hint").text() must be(messages(consigneeEoriHint))
      getElementById(view, "details_eori").attr("value") must be("")
    }

    "display empty input with label for Full name" in {

      val view = createView()

      getElementById(view, "details_address_fullName-label").text() must be(messages(fullName))
      getElementById(view, "details_address_fullName").attr("value") must be("")
    }

    "display address label" in {

      val view = createView()

      getElementById(view, "address-header").text() must be(messages(addressInfo))
      getElementById(view, "details_address_fullName").attr("value") must be("")
    }

    "display empty input with label for Address" in {

      val view = createView()

      getElementById(view, "details_address_addressLine-label").text() must be(messages(addressLine))
      getElementById(view, "details_address_addressLine").attr("value") must be("")
    }

    "display empty input with label for Town or City" in {

      val view = createView()

      getElementById(view, "details_address_townOrCity-label").text() must be(messages(townOrCity))
      getElementById(view, "details_address_townOrCity").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      getElementById(view, "details_address_postCode-label").text() must be(messages(postCode))
      getElementById(view, "details_address_postCode").attr("value") must be("")
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementById(view, "details_address_country-label").text() must be(messages(country))
      getElementById(view, "details.address.country").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Exporter Details\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/exporter-details")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Consignee Details View with invalid input" should {

    "display error when both EORI and business details are empty" in {

      val view = createView(ConsigneeDetails.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, "details-error", eoriOrAddressEmpty, "#details")

      getElementById(view, "error-message-details-input").text() must be(messages(eoriOrAddressEmpty))
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(Some(TestHelper.createRandomAlphanumericString(18)), None)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.eori-error", eoriError, "#details_eori")

      getElementById(view, "error-message-details_eori-input").text() must be(messages(eoriError))
    }

    "display error for empty Full name" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.fullName-error", fullNameEmpty, "#details_address_fullName")

      getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
    }

    "display error for incorrect Full name" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
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
      checkErrorLink(view, "details.address.fullName-error", fullNameError, "#details_address_fullName")

      getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameError))
    }

    "display error for empty Address" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")

      getElementById(view, "error-message-details_address_addressLine-input").text() must be(
        messages(addressLineEmpty)
      )
    }

    "display error for incorrect Address" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
              EntityDetails(
                None,
                Some(Address("Marco Polo", TestHelper.createRandomAlphanumericString(71), "Leeds", "LS18BN", "England"))
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")

      getElementById(view, "error-message-details_address_addressLine-input").text() must be(
        messages(addressLineError)
      )
    }

    "display error for empty Town or city" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")

      getElementById(view, "error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
    }

    "display error for incorrect Town or city" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
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
      checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")

      getElementById(view, "error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
    }

    "display error for empty Postcode" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England"))))
          )
      )
      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")

      getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
    }

    "display error for incorrect Postcode" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
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
      checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")

      getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
    }

    "display error for empty Country" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", ""))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.country-error", countryEmpty, "#details_address_country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
    }

    "display error for incorrect Country" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.country-error", countryError, "#details_address_country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")
      checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")
      checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")
      checkErrorLink(view, "details.address.country-error", countryEmpty, "#details_address_country")

      getElementById(view, "error-message-details_address_addressLine-input").text() must be(
        messages(addressLineEmpty)
      )
      getElementById(view, "error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
      getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))

    }

    "display errors when everything except Country is empty" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, "details.address.fullName-error", fullNameEmpty, "#details_address_fullName")
      checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")
      checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")
      checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")

      getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
      getElementById(view, "error-message-details_address_addressLine-input").text() must be(
        messages(addressLineEmpty)
      )
      getElementById(view, "error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
      getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
    }

    "display errors when everything except Full name is incorrect" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
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
      checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")
      checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")
      checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")
      checkErrorLink(view, "details.address.country-error", countryError, "#details_address_country")

      getElementById(view, "error-message-details_address_addressLine-input").text() must be(
        messages(addressLineError)
      )
      getElementById(view, "error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
      getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
    }

    "display errors when everything except Country is incorrect" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
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
      checkErrorLink(view, "details.address.fullName-error", fullNameError, "#details_address_fullName")
      checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")
      checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")
      checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")

      getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameError))
      getElementById(view, "error-message-details_address_addressLine-input").text() must be(
        messages(addressLineError)
      )
      getElementById(view, "error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
      getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
    }
  }

  "Consignee Details View when filled" should {

    "display data in EORI input" in {

      val form = ConsigneeDetails.form().fill(ConsigneeDetails(EntityDetails(Some("1234"), None)))
      val view = createView(form)

      getElementById(view, "details_eori").attr("value") must be("1234")
      getElementById(view, "details_address_fullName").attr("value") must be("")
      getElementById(view, "details_address_addressLine").attr("value") must be("")
      getElementById(view, "details_address_townOrCity").attr("value") must be("")
      getElementById(view, "details_address_postCode").attr("value") must be("")
      getElementById(view, "details.address.country").attr("value") must be("")
    }

    "display data in Business address inputs" in {

      val form = ConsigneeDetails
        .form()
        .fill(ConsigneeDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      getElementById(view, "details_eori").attr("value") must be("")
      getElementById(view, "details_address_fullName").attr("value") must be("test")
      getElementById(view, "details_address_addressLine").attr("value") must be("test1")
      getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
      getElementById(view, "details_address_postCode").attr("value") must be("test3")
      getElementById(view, "details.address.country").attr("value") must be("test4")
    }

    "display data in both EORI and Business address inputs" in {

      val form = ConsigneeDetails
        .form()
        .fill(ConsigneeDetails(EntityDetails(Some("1234"), Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      getElementById(view, "details_eori").attr("value") must be("1234")
      getElementById(view, "details_address_fullName").attr("value") must be("test")
      getElementById(view, "details_address_addressLine").attr("value") must be("test1")
      getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
      getElementById(view, "details_address_postCode").attr("value") must be("test3")
      getElementById(view, "details.address.country").attr("value") must be("test4")
    }
  }
}
