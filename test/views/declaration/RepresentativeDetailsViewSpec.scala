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
import forms.declaration.{EntityDetails, RepresentativeDetails}
import helpers.views.declaration.{CommonMessages, RepresentativeDetailsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.representative_details
import views.tags.ViewTest

@ViewTest
class RepresentativeDetailsViewSpec extends ViewSpec with RepresentativeDetailsMessages with CommonMessages {

  private val form: Form[RepresentativeDetails] = RepresentativeDetails.form()
  private def createView(form: Form[RepresentativeDetails] = form): Html =
    representative_details(appConfig, form)(fakeRequest, messages, countries)

  "Representative Details View" should {

    "have proper messages for labels" in {

      assertMessage(title, "Add representative")
      assertMessage(header, "Was a representative used?")
      assertMessage(eoriInfo, "3/20 EORI number")
      assertMessage(addressInfo, "3/19 Enter representative’s name and address")
      assertMessage(repTypeHeader, "3/21 What type of representation is being used?")
      assertMessage(repTypeDeclarant, "I am declaring for the company I work for or own")
      assertMessage(repTypeDirect, "Direct representative")
      assertMessage(repTypeIndirect, "Indirect representative")
    }

    "have proper messages for error labels" in {

      assertMessage(repTypeErrorEmpty, "Please, choose your representative status")
      assertMessage(repTypeErrorWrongValue, "Please, choose valid representative status")
    }
  }

  "Representative Details View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(header))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      getElementById(view, "details_eori-label").text() must be(messages(eoriInfo))
      getElementById(view, "details_eori").attr("value") must be("")
    }

    "display empty input with label for Full name" in {

      val view = createView()

      getElementById(view, "details_address_fullName-label").text() must be(messages(fullName))
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

    "display three radio buttons with description (not selected)" in {

      val view = createView(RepresentativeDetails.form().fill(RepresentativeDetails(None, None)))

      val optionDirect = getElementById(view, "statusCode_direct")
      optionDirect.attr("checked") must be("")

      val optionDirectLabel = getElementByCss(view, "#statusCode>div:nth-child(2)>label>span")
      optionDirectLabel.text() must be(messages(repTypeDirect))

      val optionIndirect = getElementById(view, "statusCode_indirect")
      optionIndirect.attr("checked") must be("")

      val optionIndirectLabel = getElementByCss(view, "#statusCode>div:nth-child(3)>label>span")
      optionIndirectLabel.text() must be(messages(repTypeIndirect))
    }

    "display \"Back\" button that links to \"Declarant Details\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/declarant-details")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementByCss(view, "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Representative Details View for invalid input" can {

    "status is not selected" when {

      "display errors when only EORI is entered" in {

        val view = createView(
          RepresentativeDetails.adjustErrors(
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
        checkErrorLink(view, "statusCode-error", repTypeErrorEmpty, "#statusCode")

        getElementById(view, "error-message-statusCode-input").text() must be(messages(repTypeErrorEmpty))
      }

      "display errors when only address is entered" in {

        val view = createView(
          RepresentativeDetails.adjustErrors(
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
        checkErrorLink(view, "statusCode-error", repTypeErrorEmpty, "#statusCode")

        getElementById(view, "error-message-statusCode-input").text() must be(messages(repTypeErrorEmpty))
      }

      "display errors when EORI is incorrect" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.eori-error", eoriError, "#details_eori")

        getElementById(view, "error-message-details_eori-input").text() must be(messages(eoriError))
      }

      "display errors for empty Full name" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameEmpty, "#details_address_fullName")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
      }

      "display errors for incorrect Full name" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameError, "#details_address_fullName")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameError))
      }

      "display errors for empty Address" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineEmpty)
        )
      }

      "display errors for incorrect Address" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineError)
        )
      }

      "display errors for empty Town or city" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")

        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityEmpty)
        )
      }

      "display errors for incorrect Town or city" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")

        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityError)
        )
      }

      "display errors for empty Postcode" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")

        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      }

      "display errors for incorrect Postcode" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")

        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
      }

      "display errors for empty Country" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.country-error", countryEmpty, "#details_address_country")

        getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
      }

      "display errors for incorrect Country" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.country-error", countryError, "#details_address_country")

        getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")
        checkErrorLink(view, "details.address.country-error", countryEmpty, "#details_address_country")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineEmpty)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityEmpty)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
        getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
      }

      "display errors when everything except Country is empty" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameEmpty, "#details_address_fullName")
        checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineEmpty)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityEmpty)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      }

      "display errors when everything except Full name is incorrect" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")
        checkErrorLink(view, "details.address.country-error", countryError, "#details_address_country")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineError)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityError)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
        getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      }

      "display errors when everything except Country is incorrect" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameError, "#details_address_fullName")
        checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameError))
        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineError)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityError)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
      }
    }

    "status is selected" when {

      "display errors when EORI is incorrect" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.eori-error", eoriError, "#details_eori")

        getElementById(view, "error-message-details_eori-input").text() must be(messages(eoriError))
      }

      "display errors for empty Full name" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameEmpty, "#details_address_fullName")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
      }

      "display errors for incorrect Full name" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameError, "#details_address_fullName")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameError))
      }

      "display errors for empty Address" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineEmpty)
        )
      }

      "display errors for incorrect Address" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineError)
        )
      }

      "display errors for empty Town or city" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")

        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityEmpty)
        )
      }

      "display errors for incorrect Town or city" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")

        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityError)
        )
      }

      "display errors for empty Postcode" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")

        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      }

      "display errors for incorrect Postcode" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")

        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
      }

      "display errors for empty Country" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.country-error", countryEmpty, "#details_address_country")

        getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
      }

      "display errors for incorrect Country" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.country-error", countryError, "#details_address_country")

        getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")
        checkErrorLink(view, "details.address.country-error", countryEmpty, "#details_address_country")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineEmpty)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityEmpty)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
        getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
      }

      "display errors when everything except Country is empty" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameEmpty, "#details_address_fullName")
        checkErrorLink(view, "details.address.addressLine-error", addressLineEmpty, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityEmpty, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeEmpty, "#details_address_postCode")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineEmpty)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityEmpty)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      }

      "display errors when everything except Full name is incorrect" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")
        checkErrorLink(view, "details.address.country-error", countryError, "#details_address_country")

        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineError)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityError)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
        getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      }

      "display errors when everything except Country is incorrect" in {

        val view = createView(
          RepresentativeDetails
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
        checkErrorLink(view, "details.address.fullName-error", fullNameError, "#details_address_fullName")
        checkErrorLink(view, "details.address.addressLine-error", addressLineError, "#details_address_addressLine")
        checkErrorLink(view, "details.address.townOrCity-error", townOrCityError, "#details_address_townOrCity")
        checkErrorLink(view, "details.address.postCode-error", postCodeError, "#details_address_postCode")

        getElementById(view, "error-message-details_address_fullName-input").text() must be(messages(fullNameError))
        getElementById(view, "error-message-details_address_addressLine-input").text() must be(
          messages(addressLineError)
        )
        getElementById(view, "error-message-details_address_townOrCity-input").text() must be(
          messages(townOrCityError)
        )
        getElementById(view, "error-message-details_address_postCode-input").text() must be(messages(postCodeError))
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
        val view = createView(form)

        getElementById(view, "details_eori").attr("value") must be("1234")
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
        val view = createView(form)

        getElementById(view, "details_address_fullName").attr("value") must be("test")
        getElementById(view, "details_address_addressLine").attr("value") must be("test1")
        getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
        getElementById(view, "details_address_postCode").attr("value") must be("test3")
        getElementById(view, "details.address.country").attr("value") must be("test4")
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
        val view = createView(form)

        getElementById(view, "details_eori").attr("value") must be("1234")
        getElementById(view, "statusCode_direct").attr("checked") must be("checked")
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
        val view = createView(form)

        getElementById(view, "details_address_fullName").attr("value") must be("test")
        getElementById(view, "details_address_addressLine").attr("value") must be("test1")
        getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
        getElementById(view, "details_address_postCode").attr("value") must be("test3")
        getElementById(view, "details.address.country").attr("value") must be("test4")
        getElementById(view, "statusCode_direct").attr("checked") must be("checked")
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
        val view = createView(form)

        getElementById(view, "details_eori").attr("value") must be("1234")
        getElementById(view, "statusCode_indirect").attr("checked") must be("checked")
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
        val view = createView(form)

        getElementById(view, "details_address_fullName").attr("value") must be("test")
        getElementById(view, "details_address_addressLine").attr("value") must be("test1")
        getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
        getElementById(view, "details_address_postCode").attr("value") must be("test3")
        getElementById(view, "details.address.country").attr("value") must be("test4")
        getElementById(view, "statusCode_indirect").attr("checked") must be("checked")
      }
    }
  }
}
