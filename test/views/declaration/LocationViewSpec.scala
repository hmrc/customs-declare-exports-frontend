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
import forms.declaration.GoodsLocation
import helpers.views.declaration.{CommonMessages, LocationOfGoodsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.declaration.goods_location
import views.tags.ViewTest

@ViewTest
class LocationViewSpec extends ViewSpec with LocationOfGoodsMessages with CommonMessages {

  private val form: Form[GoodsLocation] = GoodsLocation.form()
  private val goodsLocationPage = app.injector.instanceOf[goods_location]
  private def createView(form: Form[GoodsLocation] = form): Html =
    goodsLocationPage(form)(fakeRequest, messages)

  "Location View" should {

    "have proper messages for labels" in {

      assertMessage(locationOfGoods, "Location of goods")
      assertMessage(title, "5/23 Where was the location of the goods?")
      assertMessage(typeOfLocation, "Location Type")
      assertMessage(qualifierOfIdent, "Qualifier code")
      assertMessage(identOfLocation, "Identification of location")
      assertMessage(additionalQualifier, "Location code and Additional Qualifier")
      assertMessage(locationAddress, "Address Line 1")
      assertMessage(logPostCode, "Postcode")
      assertMessage(city, "City")
    }

    "have proper messages for error labels" in {

      assertMessage(typeOfLocationEmpty, "Type of location cannot be empty")
      assertMessage(typeOfLocationError, "Type of location is incorrect")
      assertMessage(qualifierOfIdentEmpty, "Qualifier of location cannot be empty")
      assertMessage(qualifierOfIdentError, "Qualifier of the identification is incorrect")
      assertMessage(identOfLocationError, "Identification of location is incorrect")
      assertMessage(additionalQualifierError, "Additional identifier is incorrect")
      assertMessage(locationAddressError, "Address Line 1 is incorrect")
      assertMessage(logPostCodeError, "Postcode is incorrect")
      assertMessage(cityError, "City is incorrect")
    }
  }

  "Location View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(locationOfGoods))
    }

    "display section header" in {

      getElementById(createView(), "section-header").text() must be("Locations")
    }

    "display header" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementById(view, "country-label").text() must be(messages(country))
      getElementById(view, "country").attr("value") must be("")
    }

    "display empty input with label for Type of Location" in {

      val view = createView()

      getElementById(view, "typeOfLocation-label").text() must be(messages(typeOfLocation))
      getElementById(view, "typeOfLocation").attr("value") must be("")
    }

    "display empty input with label for Qualifier of Identification" in {

      val view = createView()

      getElementById(view, "qualifierOfIdentification-label").text() must be(messages(qualifierOfIdent))
      getElementById(view, "qualifierOfIdentification").attr("value") must be("")
    }

    "display empty input with label for Identification of Location" in {

      val view = createView()

      getElementById(view, "identificationOfLocation-label").text() must be(messages(identOfLocation))
      getElementById(view, "identificationOfLocation").attr("value") must be("")
    }

    "display empty input with label for Additional Identifier" in {

      val view = createView()

      getElementById(view, "additionalQualifier-label").text() must be(messages(additionalQualifier))
      getElementById(view, "additionalQualifier").attr("value") must be("")
    }

    "display empty input with label for Street and Number" in {

      val view = createView()

      getElementById(view, "addressLine-label").text() must be(messages(locationAddress))
      getElementById(view, "addressLine").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      getElementById(view, "postCode-label").text() must be(messages(logPostCode))
      getElementById(view, "postCode").attr("value") must be("")
    }

    "display empty input with label for City" in {

      val view = createView()

      getElementById(view, "city-label").text() must be(messages(city))
      getElementById(view, "city").attr("value") must be("")
    }

    "display 'Back' button that links to 'Destination Countries' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/destination-countries")
    }

    "display 'Save and continue' button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Location View for invalid input" should {

    "display error for empty Country" in {

      val form =
        GoodsLocation.form.fillAndValidate(GoodsLocation("", "t", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryEmpty, "#country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryEmpty))
    }

    "display error for incorrect Country" in {

      val form =
        GoodsLocation.form.fillAndValidate(GoodsLocation("TST", "t", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
    }

    "display error for empty Type of Location" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, typeOfLocationEmpty, "#typeOfLocation")

      getElementByCss(view, "#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationEmpty))
    }

    "display error for incorrect Type of Location" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "TST", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, typeOfLocationError, "#typeOfLocation")

      getElementByCss(view, "#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
    }

    "display error for empty Qualifier of Identification" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "t", "", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, qualifierOfIdentEmpty, "#qualifierOfIdentification")

      getElementByCss(view, "#error-message-qualifierOfIdentification-input").text() must be(
        messages(qualifierOfIdentEmpty)
      )
    }

    "display error for incorrect Qualifier of Identification" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "t", "@@!", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, qualifierOfIdentError, "#qualifierOfIdentification")

      getElementByCss(view, "#error-message-qualifierOfIdentification-input").text() must be(
        messages(qualifierOfIdentError)
      )
    }

    "display error for incorrect Identification of Location" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("@@!"), Some("TST"), None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, identOfLocationError, "#identificationOfLocation")

      getElementByCss(view, "#error-message-identificationOfLocation-input").text() must be(
        messages(identOfLocationError)
      )
    }

    "display error for incorrect Additional Identifier" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Poland",
            "t",
            "t",
            Some("TST"),
            Some(TestHelper.createRandomAlphanumericString(33)),
            None,
            None,
            None
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, additionalQualifierError, "#additionalQualifier")

      getElementByCss(view, "#error-message-additionalQualifier-input").text() must be(
        messages(additionalQualifierError)
      )
    }

    "display error for incorrect Street and Number" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Poland",
            "t",
            "t",
            Some("TST"),
            Some("TST"),
            Some(TestHelper.createRandomAlphanumericString(71)),
            None,
            None
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, locationAddressError, "#addressLine")

      getElementByCss(view, "#error-message-addressLine-input").text() must be(messages(locationAddressError))
    }

    "display error for incorrect Postcode" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Poland",
            "t",
            "t",
            Some("TST"),
            Some("TST"),
            None,
            Some(TestHelper.createRandomAlphanumericString(10)),
            None
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, logPostCodeError, "#postCode")

      getElementByCss(view, "#error-message-postCode-input").text() must be(messages(logPostCodeError))
    }

    "display error for incorrect City" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Poland",
            "t",
            "t",
            Some("TST"),
            Some("TST"),
            None,
            None,
            Some(TestHelper.createRandomAlphanumericString(36))
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, cityError, "#city")

      getElementByCss(view, "#error-message-city-input").text() must be(messages(cityError))
    }

    "display errors for everything incorrect" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Country",
            "ABC",
            "ABC",
            Some("TEST"),
            Some(TestHelper.createRandomAlphanumericString(33)),
            Some(TestHelper.createRandomAlphanumericString(71)),
            Some(TestHelper.createRandomAlphanumericString(10)),
            Some(TestHelper.createRandomAlphanumericString(36))
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#country")
      checkErrorLink(view, 2, typeOfLocationError, "#typeOfLocation")
      checkErrorLink(view, 3, qualifierOfIdentError, "#qualifierOfIdentification")
      checkErrorLink(view, 4, identOfLocationError, "#identificationOfLocation")
      checkErrorLink(view, 5, additionalQualifierError, "#additionalQualifier")
      checkErrorLink(view, 6, locationAddressError, "#addressLine")
      checkErrorLink(view, 7, logPostCodeError, "#postCode")
      checkErrorLink(view, 8, cityError, "#city")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      getElementByCss(view, "#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
      getElementByCss(view, "#error-message-qualifierOfIdentification-input").text() must be(
        messages(qualifierOfIdentError)
      )
      getElementByCss(view, "#error-message-identificationOfLocation-input").text() must be(
        messages(identOfLocationError)
      )
      getElementByCss(view, "#error-message-additionalQualifier-input").text() must be(
        messages(additionalQualifierError)
      )
      getElementByCss(view, "#error-message-postCode-input").text() must be(messages(logPostCodeError))
      getElementByCss(view, "#error-message-city-input").text() must be(messages(cityError))
    }

    "display errors for everything incorrect (except IoL which is empty)" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Country",
            "ABC",
            "ABC",
            None,
            Some(TestHelper.createRandomAlphanumericString(33)),
            Some(TestHelper.createRandomAlphanumericString(71)),
            Some(TestHelper.createRandomAlphanumericString(10)),
            Some(TestHelper.createRandomAlphanumericString(36))
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#country")
      checkErrorLink(view, 2, typeOfLocationError, "#typeOfLocation")
      checkErrorLink(view, 3, qualifierOfIdentError, "#qualifierOfIdentification")
      checkErrorLink(view, 4, additionalQualifierError, "#additionalQualifier")
      checkErrorLink(view, 5, locationAddressError, "#addressLine")
      checkErrorLink(view, 6, logPostCodeError, "#postCode")
      checkErrorLink(view, 7, cityError, "#city")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      getElementByCss(view, "#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
      getElementByCss(view, "#error-message-qualifierOfIdentification-input").text() must be(
        messages(qualifierOfIdentError)
      )
      getElementByCss(view, "#error-message-additionalQualifier-input").text() must be(
        messages(additionalQualifierError)
      )
      getElementByCss(view, "#error-message-postCode-input").text() must be(messages(logPostCodeError))
      getElementByCss(view, "#error-message-city-input").text() must be(messages(cityError))
    }
  }

  "Location View when filled" should {

    "display data in all inputs" in {

      val ladditionalInformation: String = TestHelper.createRandomAlphanumericString(32)
      val lstreetAndNumber: String = TestHelper.createRandomAlphanumericString(70)
      val lpostCode: String = TestHelper.createRandomAlphanumericString(9)
      val lcity: String = TestHelper.createRandomAlphanumericString(35)

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Poland",
            "AB",
            "CD",
            Some("TST"),
            Some(ladditionalInformation),
            Some(lstreetAndNumber),
            Some(lpostCode),
            Some(lcity)
          )
        )
      val view = createView(form)
      getElementById(view, "country").attr("value") must be("Poland")
      getElementById(view, "typeOfLocation").attr("value") must be("AB")
      getElementById(view, "qualifierOfIdentification").attr("value") must be("CD")
      getElementById(view, "identificationOfLocation").attr("value") must be("TST")
      getElementById(view, "additionalQualifier").attr("value") must be(ladditionalInformation)
      getElementById(view, "addressLine").attr("value") must be(lstreetAndNumber)
      getElementById(view, "postCode").attr("value") must be(lpostCode)
      getElementById(view, "city").attr("value") must be(lcity)
    }
  }
}
