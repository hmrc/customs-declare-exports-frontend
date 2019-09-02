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
import models.Mode
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.AppViewSpec
import views.html.declaration.goods_location
import views.tags.ViewTest

@ViewTest
class LocationViewSpec extends AppViewSpec with LocationOfGoodsMessages with CommonMessages {

  private val form: Form[GoodsLocation] = GoodsLocation.form()
  private val goodsLocationPage = app.injector.instanceOf[goods_location]
  private def createView(form: Form[GoodsLocation] = form): Html =
    goodsLocationPage(Mode.Normal, form)(fakeRequest, messages)

  "Location View on empty page" should {

    "display page title" in {

      createView().select("title").text() must be(messages(locationOfGoods))
    }

    "display section header" in {

      createView().getElementById("section-header").text() must be("Locations")
    }

    "display header" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display empty input with label for Country" in {

      val view = createView()

      view.getElementById("country-label").text() must be(messages(country))
      view.getElementById("country").attr("value") must be("")
    }

    "display empty input with label for Type of Location" in {

      val view = createView()

      view.getElementById("typeOfLocation-label").text() must be(messages(typeOfLocation))
      view.getElementById("typeOfLocation").attr("value") must be("")
    }

    "display empty input with label for Qualifier of Identification" in {

      val view = createView()

      view.getElementById("qualifierOfIdentification-label").text() must be(messages(qualifierOfIdent))
      view.getElementById("qualifierOfIdentification").attr("value") must be("")
    }

    "display empty input with label for Identification of Location" in {

      val view = createView()

      view.getElementById("identificationOfLocation-label").text() must be(messages(identOfLocation))
      view.getElementById("identificationOfLocation").attr("value") must be("")
    }

    "display empty input with label for Additional Identifier" in {

      val view = createView()

      view.getElementById("additionalQualifier-label").text() must be(messages(additionalQualifier))
      view.getElementById("additionalQualifier").attr("value") must be("")
    }

    "display empty input with label for Street and Number" in {

      val view = createView()

      view.getElementById("addressLine-label").text() must be(messages(locationAddress))
      view.getElementById("addressLine").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      view.getElementById("postCode-label").text() must be(messages(logPostCode))
      view.getElementById("postCode").attr("value") must be("")
    }

    "display empty input with label for City" in {

      val view = createView()

      view.getElementById("city-label").text() must be(messages(city))
      view.getElementById("city").attr("value") must be("")
    }

    "display 'Back' button that links to 'Destination Countries' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/destination-countries")
    }

    "display 'Save and continue' button" in {
      val view = createView()
      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }

    "display 'Save and return' button" in {
      val view = createView()
      val saveButton = view.getElementById("submit_and_return")
      saveButton.text() must be(messages(saveAndReturnCaption))
    }
  }

  "Location View for invalid input" should {

    "display error for empty Country" in {

      val form =
        GoodsLocation.form.fillAndValidate(GoodsLocation("", "t", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, countryEmpty, "#country")

      view.select("span.error-message").text() must be(messages(countryEmpty))
    }

    "display error for incorrect Country" in {

      val form =
        GoodsLocation.form.fillAndValidate(GoodsLocation("TST", "t", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, countryError, "#country")

      view.select("span.error-message").text() must be(messages(countryError))
    }

    "display error for empty Type of Location" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, typeOfLocationEmpty, "#typeOfLocation")

      view.select("#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationEmpty))
    }

    "display error for incorrect Type of Location" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "TST", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, typeOfLocationError, "#typeOfLocation")

      view.select("#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
    }

    "display error for empty Qualifier of Identification" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "t", "", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, qualifierOfIdentEmpty, "#qualifierOfIdentification")

      view.select("#error-message-qualifierOfIdentification-input").text() must be(messages(qualifierOfIdentEmpty))
    }

    "display error for incorrect Qualifier of Identification" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "t", "@@!", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, qualifierOfIdentError, "#qualifierOfIdentification")

      view.select("#error-message-qualifierOfIdentification-input").text() must be(messages(qualifierOfIdentError))
    }

    "display error for incorrect Identification of Location" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("@@!"), Some("TST"), None, None, None))
      val view = createView(form)

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, identOfLocationError, "#identificationOfLocation")

      view.select("#error-message-identificationOfLocation-input").text() must be(messages(identOfLocationError))
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

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, additionalQualifierError, "#additionalQualifier")

      view.select("#error-message-additionalQualifier-input").text() must be(messages(additionalQualifierError))
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

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, locationAddressError, "#addressLine")

      view.select("#error-message-addressLine-input").text() must be(messages(locationAddressError))
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

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, logPostCodeError, "#postCode")

      view.select("#error-message-postCode-input").text() must be(messages(logPostCodeError))
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

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, cityError, "#city")

      view.select("#error-message-city-input").text() must be(messages(cityError))
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

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, countryError, "#country")
      checkErrorLink(view, 2, typeOfLocationError, "#typeOfLocation")
      checkErrorLink(view, 3, qualifierOfIdentError, "#qualifierOfIdentification")
      checkErrorLink(view, 4, identOfLocationError, "#identificationOfLocation")
      checkErrorLink(view, 5, additionalQualifierError, "#additionalQualifier")
      checkErrorLink(view, 6, locationAddressError, "#addressLine")
      checkErrorLink(view, 7, logPostCodeError, "#postCode")
      checkErrorLink(view, 8, cityError, "#city")

      view.select("span.error-message").text() must be(messages(countryError))
      view.select("#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
      view.select("#error-message-qualifierOfIdentification-input").text() must be(messages(qualifierOfIdentError))
      view.select("#error-message-identificationOfLocation-input").text() must be(messages(identOfLocationError))
      view.select("#error-message-additionalQualifier-input").text() must be(messages(additionalQualifierError))
      view.select("#error-message-postCode-input").text() must be(messages(logPostCodeError))
      view.select("#error-message-city-input").text() must be(messages(cityError))
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

      view.getElementById("error-summary-heading").text() mustNot be(empty)
      checkErrorLink(view, 1, countryError, "#country")
      checkErrorLink(view, 2, typeOfLocationError, "#typeOfLocation")
      checkErrorLink(view, 3, qualifierOfIdentError, "#qualifierOfIdentification")
      checkErrorLink(view, 4, additionalQualifierError, "#additionalQualifier")
      checkErrorLink(view, 5, locationAddressError, "#addressLine")
      checkErrorLink(view, 6, logPostCodeError, "#postCode")
      checkErrorLink(view, 7, cityError, "#city")

      view.select("span.error-message").text() must be(messages(countryError))
      view.select("#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
      view.select("#error-message-qualifierOfIdentification-input").text() must be(messages(qualifierOfIdentError))
      view.select("#error-message-additionalQualifier-input").text() must be(messages(additionalQualifierError))
      view.select("#error-message-postCode-input").text() must be(messages(logPostCodeError))
      view.select("#error-message-city-input").text() must be(messages(cityError))
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
      view.getElementById("country").attr("value") must be("Poland")
      view.getElementById("typeOfLocation").attr("value") must be("AB")
      view.getElementById("qualifierOfIdentification").attr("value") must be("CD")
      view.getElementById("identificationOfLocation").attr("value") must be("TST")
      view.getElementById("additionalQualifier").attr("value") must be(ladditionalInformation)
      view.getElementById("addressLine").attr("value") must be(lstreetAndNumber)
      view.getElementById("postCode").attr("value") must be(lpostCode)
      view.getElementById("city").attr("value") must be(lcity)
    }
  }
}
