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
import views.html.declaration.goods_location
import views.declaration.spec.ViewSpec

class LocationViewSpec extends ViewSpec with LocationOfGoodsMessages with CommonMessages {

  private val form: Form[GoodsLocation] = GoodsLocation.form()
  private def createView(form: Form[GoodsLocation] = form): Html =
    goods_location(appConfig, form)(fakeRequest, messages, countries)

  "Location View" should {

    "have proper messages for labels" in {

      assertMessage(locationOfGoods, "Location of goods")
      assertMessage(title, "5/23 What is the location of the goods?")
      assertMessage(typeOfLocation, "Type of location")
      assertMessage(qualifierOfIdent, "Qualifier of the identification")
      assertMessage(identOfLocation, "Identification of location")
      assertMessage(additionalIdentifier, "Additional identifier")
      assertMessage(streetAndNumber, "Street and number")
      assertMessage(logPostCode, "Postcode")
      assertMessage(city, "City")
    }

    "have proper messages for error labels" in {

      assertMessage(typeOfLocationError, "Type of location is incorrect")
      assertMessage(qualifierOfIdentError, "Qualifier of the identification is incorrect")
      assertMessage(identOfLocationEmpty, "Identification of location cannot be empty")
      assertMessage(identOfLocationError, "Identification of location is incorrect")
      assertMessage(additionalIdentifierError, "Additional identifier is incorrect")
      assertMessage(streetAndNumberError, "Street and number is incorrect")
      assertMessage(logPostCodeError, "Postcode is incorrect")
      assertMessage(cityError, "City is incorrect")
    }
  }

  "Location View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(locationOfGoods))
    }

    "display header" in {

      getElementByCss(createView(), "legend>h1").text() must be(messages(title))
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementByCss(view, "#country-outer>label").text() must be(messages(country))
      getElementById(view, "country").attr("value") must be("")
    }

    "display empty input with label for Type of Location" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(5)>label>span").text() must be(messages(typeOfLocation))
      getElementById(view, "typeOfLocation").attr("value") must be("")
    }

    "display empty input with label for Qualifier of Identification" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(6)>label>span").text() must be(messages(qualifierOfIdent))
      getElementById(view, "qualifierOfIdentification").attr("value") must be("")
    }

    "display empty input with label for Identification of Location" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(7)>label>span").text() must be(messages(identOfLocation))
      getElementById(view, "identificationOfLocation").attr("value") must be("")
    }

    "display empty input with label for Additional Identifier" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(8)>label>span").text() must be(messages(additionalIdentifier))
      getElementById(view, "additionalIdentifier").attr("value") must be("")
    }

    "display empty input with label for Street and Number" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(9)>label>span").text() must be(messages(streetAndNumber))
      getElementById(view, "streetAndNumber").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(10)>label>span").text() must be(messages(logPostCode))
      getElementById(view, "postCode").attr("value") must be("")
    }

    "display empty input with label for City" in {

      val view = createView()

      getElementByCss(view, "form>div:nth-child(11)>label>span").text() must be(messages(city))
      getElementById(view, "city").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Destination Countries\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-declare-exports/declaration/destination-countries")
    }

    "display \"Save and continue\" button" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Location View for invalid input" should {

    "display error for incorrect Country" in {

      val form =
        GoodsLocation.form().fillAndValidate(GoodsLocation(Some("test"), None, None, "TST", None, None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#country")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
    }

    "display error for incorrect Type of Location" in {

      val form =
        GoodsLocation.form().fillAndValidate(GoodsLocation(None, Some("AB"), None, "TST", None, None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, typeOfLocationError, "#typeOfLocation")

      getElementByCss(view, "#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
    }

    "display error for incorrect Qualifier of Identification" in {

      val form =
        GoodsLocation.form().fillAndValidate(GoodsLocation(None, None, Some("AB"), "TST", None, None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, qualifierOfIdentError, "#qualifierOfIdentification")

      getElementByCss(view, "#error-message-qualifierOfIdentification-input").text() must be(
        messages(qualifierOfIdentError)
      )
    }

    "display error for empty Identification of Location" in {

      val form = GoodsLocation.form().fillAndValidate(GoodsLocation(None, None, None, "", None, None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, identOfLocationEmpty, "#identificationOfLocation")

      getElementByCss(view, "#error-message-identificationOfLocation-input").text() must be(
        messages(identOfLocationEmpty)
      )
    }

    "display error for incorrect Identification of Location" in {

      val form = GoodsLocation.form().fillAndValidate(GoodsLocation(None, None, None, "TEST", None, None, None, None))
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, identOfLocationError, "#identificationOfLocation")

      getElementByCss(view, "#error-message-identificationOfLocation-input").text() must be(
        messages(identOfLocationError)
      )
    }

    "display error for incorrect Additional Identifier" in {

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(None, None, None, "TST", Some(TestHelper.createRandomString(33)), None, None, None)
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, additionalIdentifierError, "#additionalIdentifier")

      getElementByCss(view, "#error-message-additionalIdentifier-input").text() must be(
        messages(additionalIdentifierError)
      )
    }

    "display error for incorrect Street and Number" in {

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(None, None, None, "TST", None, Some(TestHelper.createRandomString(71)), None, None)
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, streetAndNumberError, "#streetAndNumber")

      getElementByCss(view, "#error-message-streetAndNumber-input").text() must be(messages(streetAndNumberError))
    }

    "display error for incorrect Postcode" in {

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(None, None, None, "TST", None, None, Some(TestHelper.createRandomString(10)), None)
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, logPostCodeError, "#postCode")

      getElementByCss(view, "#error-message-postCode-input").text() must be(messages(logPostCodeError))
    }

    "display error for incorrect City" in {

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(None, None, None, "TST", None, None, None, Some(TestHelper.createRandomString(36)))
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, cityError, "#city")

      getElementByCss(view, "#error-message-city-input").text() must be(messages(cityError))
    }

    "display errors for everything incorrect" in {

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(
            Some("Country"),
            Some("ABC"),
            Some("ABC"),
            "TEST",
            Some(TestHelper.createRandomString(33)),
            Some(TestHelper.createRandomString(71)),
            Some(TestHelper.createRandomString(10)),
            Some(TestHelper.createRandomString(36))
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#country")
      checkErrorLink(view, 2, typeOfLocationError, "#typeOfLocation")
      checkErrorLink(view, 3, qualifierOfIdentError, "#qualifierOfIdentification")
      checkErrorLink(view, 4, identOfLocationError, "#identificationOfLocation")
      checkErrorLink(view, 5, additionalIdentifierError, "#additionalIdentifier")
      checkErrorLink(view, 6, streetAndNumberError, "#streetAndNumber")
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
      getElementByCss(view, "#error-message-additionalIdentifier-input").text() must be(
        messages(additionalIdentifierError)
      )
      getElementByCss(view, "#error-message-postCode-input").text() must be(messages(logPostCodeError))
      getElementByCss(view, "#error-message-city-input").text() must be(messages(cityError))
    }

    "display errors for everything incorrect (except IoL which is empty)" in {

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(
            Some("Country"),
            Some("ABC"),
            Some("ABC"),
            "",
            Some(TestHelper.createRandomString(33)),
            Some(TestHelper.createRandomString(71)),
            Some(TestHelper.createRandomString(10)),
            Some(TestHelper.createRandomString(36))
          )
        )
      val view = createView(form)

      checkErrorsSummary(view)
      checkErrorLink(view, 1, countryError, "#country")
      checkErrorLink(view, 2, typeOfLocationError, "#typeOfLocation")
      checkErrorLink(view, 3, qualifierOfIdentError, "#qualifierOfIdentification")
      checkErrorLink(view, 4, identOfLocationEmpty, "#identificationOfLocation")
      checkErrorLink(view, 5, additionalIdentifierError, "#additionalIdentifier")
      checkErrorLink(view, 6, streetAndNumberError, "#streetAndNumber")
      checkErrorLink(view, 7, logPostCodeError, "#postCode")
      checkErrorLink(view, 8, cityError, "#city")

      getElementByCss(view, "span.error-message").text() must be(messages(countryError))
      getElementByCss(view, "#error-message-typeOfLocation-input").text() must be(messages(typeOfLocationError))
      getElementByCss(view, "#error-message-qualifierOfIdentification-input").text() must be(
        messages(qualifierOfIdentError)
      )
      getElementByCss(view, "#error-message-identificationOfLocation-input").text() must be(
        messages(identOfLocationEmpty)
      )
      getElementByCss(view, "#error-message-additionalIdentifier-input").text() must be(
        messages(additionalIdentifierError)
      )
      getElementByCss(view, "#error-message-postCode-input").text() must be(messages(logPostCodeError))
      getElementByCss(view, "#error-message-city-input").text() must be(messages(cityError))
    }
  }

  "Location View when filled" should {

    "display all fields entered" in {

      val ladditionalInformation: String = TestHelper.createRandomString(32)
      val lstreetAndNumber: String = TestHelper.createRandomString(70)
      val lpostCode: String = TestHelper.createRandomString(9)
      val lcity: String = TestHelper.createRandomString(35)

      val form = GoodsLocation
        .form()
        .fillAndValidate(
          GoodsLocation(
            Some("Poland"),
            Some("AB"),
            Some("CD"),
            "TST",
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
      getElementById(view, "additionalIdentifier").attr("value") must be(ladditionalInformation)
      getElementById(view, "streetAndNumber").attr("value") must be(lstreetAndNumber)
      getElementById(view, "postCode").attr("value") must be(lpostCode)
      getElementById(view, "city").attr("value") must be(lcity)
    }
  }
}
