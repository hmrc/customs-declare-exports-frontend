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

package views.supplementary

import base.TestHelper
import forms.supplementary.{Address, DeclarantDetails, EntityDetails}
import helpers.views.supplementary.{CommonMessages, DeclarantDetailsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.supplementary.declarant_details
import views.supplementary.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class DeclarantDetailsViewSpec extends ViewSpec with DeclarantDetailsMessages with CommonMessages {

  private val form: Form[DeclarantDetails] = DeclarantDetails.form()
  private def createView(form: Form[DeclarantDetails] = form): Html =
    declarant_details(appConfig, form)(fakeRequest, messages, countries)

  "Declarant Details View" should {

    "have proper messages for labels" in {

      assertMessage(title, "3/17 - 3/18 Add declarant")
      assertMessage(hint, "TODO")
    }
  }

  "Declarant Details View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title))
      getElementByCss(view, "legend>span").text() must be(messages(hint))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      getElementByCss(view, "label.form-label>span").text() must be(messages(eori))
      getElementByCss(view, "label.form-label>span.form-hint").text() must be(messages(eoriHint))
      getElementById(view, "details_eori").attr("value") must be("")
    }

    "display empty input with label for Full name" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(1)>label").text() must be(
        messages(fullName)
      )
      getElementById(view, "details_address_fullName").attr("value") must be("")
    }

    "display empty input with label for Address" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(2)>label").text() must be(
        messages(addressLine)
      )
      getElementById(view, "details_address_addressLine").attr("value") must be("")
    }

    "display empty input with label for Town or City" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(3)>label").text() must be(
        messages(townOrCity)
      )
      getElementById(view, "details_address_townOrCity").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(4)>label").text() must be(
        messages(postCode)
      )
      getElementById(view, "details_address_postCode").attr("value") must be("")
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(5)>label").text() must be(
        messages(country)
      )
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

  "Declarant Details View with invalid input" should {

    "display error when both EORI and business details are empty" in {

      val view = createView(DeclarantDetails.form().bind(Map[String, String]()))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eoriOrAddressEmpty, "#details")

      getElementByCss(view, "#error-message-details-input").text() must be(messages(eoriOrAddressEmpty))
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(DeclarantDetails(EntityDetails(Some(TestHelper.createRandomString(19)), None)))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eoriError, "#details_eori")

      getElementByCss(view, "#error-message-details_eori-input").text() must be(messages(eoriError))
    }

    "display error for empty Full name" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullNameEmpty, "#details_address_fullName")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
    }

    "display error for incorrect Full name" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(
                None,
                Some(Address(TestHelper.createRandomString(71), "Test Street", "Leeds", "LS18BN", "England"))
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullNameError, "#details_address_fullName")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullNameError))
    }

    "display error for empty Address" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLineEmpty, "#details_address_addressLine")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(
        messages(addressLineEmpty)
      )
    }

    "display error for incorrect Address" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(
                None,
                Some(Address("Marco Polo", TestHelper.createRandomString(71), "Leeds", "LS18BN", "England"))
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLineError, "#details_address_addressLine")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(
        messages(addressLineError)
      )
    }

    "display error for empty Town or city" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England"))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, townOrCityEmpty, "#details_address_townOrCity")

      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
    }

    "display error for incorrect Town or city" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(
                None,
                Some(Address("Marco Polo", "Test Street", TestHelper.createRandomString(71), "LS18BN", "England"))
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, townOrCityError, "#details_address_townOrCity")

      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
    }

    "display error for empty Postcode" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England"))))
          )
      )
      checkErrorsSummary(view)
      checkErrorLink(view, 1, postCodeEmpty, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
    }

    "display error for incorrect Postcode" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(
                None,
                Some(Address("Marco Polo", "Test Street", "Leeds", TestHelper.createRandomString(71), "England"))
              )
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, postCodeError, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCodeError))
    }

    "display error for empty Country" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", ""))))
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryEmpty, "#details_address_country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
    }

    "display error for incorrect Country" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))
            )
          )
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#details_address_country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(DeclarantDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLineEmpty, "#details_address_addressLine")
      checkErrorLink(view, 2, townOrCityEmpty, "#details_address_townOrCity")
      checkErrorLink(view, 3, postCodeEmpty, "#details_address_postCode")
      checkErrorLink(view, 4, countryEmpty, "#details_address_country")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(
        messages(addressLineEmpty)
      )
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
      getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))

    }

    "display errors when everything except Country is empty" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(DeclarantDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
      )

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullNameEmpty, "#details_address_fullName")
      checkErrorLink(view, 2, addressLineEmpty, "#details_address_addressLine")
      checkErrorLink(view, 3, townOrCityEmpty, "#details_address_townOrCity")
      checkErrorLink(view, 4, postCodeEmpty, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullNameEmpty))
      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(
        messages(addressLineEmpty)
      )
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityEmpty))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCodeEmpty))
    }

    "display errors when everything except Full name is incorrect" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(
                None,
                Some(
                  Address(
                    "Marco Polo",
                    TestHelper.createRandomString(71),
                    TestHelper.createRandomString(71),
                    TestHelper.createRandomString(71),
                    TestHelper.createRandomString(71)
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

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(
        messages(addressLineError)
      )
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCodeError))
      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
    }

    "display errors when everything except Country is incorrect" in {

      val view = createView(
        DeclarantDetails
          .form()
          .fillAndValidate(
            DeclarantDetails(
              EntityDetails(
                None,
                Some(
                  Address(
                    TestHelper.createRandomString(71),
                    TestHelper.createRandomString(71),
                    TestHelper.createRandomString(71),
                    TestHelper.createRandomString(71),
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

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullNameError))
      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(
        messages(addressLineError)
      )
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCityError))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCodeError))
    }
  }

  "Declarant Details View when filled" should {

    "display data in EORI input" in {

      val form = DeclarantDetails.form().fill(DeclarantDetails(EntityDetails(Some("1234"), None)))
      val view = createView(form)

      getElementById(view, "details_eori").attr("value") must be("1234")
    }

    "display data in Business address inputs" in {

      val form = DeclarantDetails
        .form()
        .fill(DeclarantDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      getElementById(view, "details_address_fullName").attr("value") must be("test")
      getElementById(view, "details_address_addressLine").attr("value") must be("test1")
      getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
      getElementById(view, "details_address_postCode").attr("value") must be("test3")
      getElementById(view, "details.address.country").attr("value") must be("test4")
    }
  }
}
