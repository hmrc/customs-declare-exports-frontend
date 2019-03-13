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

package views

import forms.supplementary.{Address, ConsigneeDetails, EntityDetails}
import play.api.data.Form
import play.twirl.api.Html
import views.helpers.{Item, ViewSpec}
import views.html.supplementary.consignee_details
import views.tags.ViewTest

@ViewTest
class ConsigneeDetailsViewSpec extends ViewSpec {

  val form:Form[ConsigneeDetails] = ConsigneeDetails.form()

  private val prefix = s"${basePrefix}consignee."

  private val title = Item(prefix, "title")
  private val eori = Item(basePrefix, "eori")
  private val fullName = Item(addressPrefix, "fullName")
  private val addressLine = Item(addressPrefix, "addressLine")
  private val townOrCity = Item(addressPrefix, "townOrCity")
  private val postCode = Item(addressPrefix, "postCode")
  private val country = Item(addressPrefix, "country")
  private val nothingEntered = Item(basePrefix, "namedEntityDetails")
  private def createView(form :Form[ConsigneeDetails] = form) : Html = consignee_details(appConfig, form)

  "Consignee Details View" should {

    "have proper messages for labels" in {

      assertMessage(title.withPrefix, "supplementary.consignee.title")
      assertMessage(title.withHint, "supplementary.consignee.title.hint")
      assertMessage(eori.withPrefix, "EORI number")
      assertMessage(eori.withHint, "Enter the EORI number or business details")
      assertMessage(fullName.withPrefix, "Full name")
      assertMessage(addressLine.withPrefix, "Address line 1")
      assertMessage(townOrCity.withPrefix, "Town or city")
      assertMessage(postCode.withPrefix, "Postcode")
      assertMessage(country.withPrefix, "Country")
    }

    "have proper messages for error labels" in {

      assertMessage(nothingEntered.withError, "Please, provide either EORI number or full Address details")
      assertMessage(fullName.withEmpty, "Full name cannot be empty")
      assertMessage(fullName.withError, "Full name is incorrect")
      assertMessage(addressLine.withEmpty, "Address line 1 cannot be empty")
      assertMessage(addressLine.withError, "Address line 1 is incorrect")
      assertMessage(townOrCity.withEmpty, "Town or city cannot be empty")
      assertMessage(townOrCity.withError, "Town or city is incorrect")
      assertMessage(postCode.withEmpty, "Postcode cannot be empty")
      assertMessage(postCode.withError, "Postcode is incorrect")
      assertMessage(country.withEmpty, "Country cannot be empty")
      assertMessage(country.withError, "Country is incorrect")
    }
  }

  "Consignee Details View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "title").text() must be(messages(title.withPrefix))
    }

    "display header with hint" in {

      val view = createView()

      getElementByCss(view, "legend>h1").text() must be(messages(title.withPrefix))
      getElementByCss(view, "legend>span").text() must be(messages(title.withHint))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      // will grab the first element
      getElementByCss(view, "label.form-label>span").text() must be(messages(eori.withPrefix))
      getElementByCss(view, "label.form-label>span.form-hint").text() must be(messages(eori.withHint))
      getElementById(view, "details_eori").attr("value") must be("")
    }

    "display empty input with label for Full name" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(1)>label").text() must be(messages(fullName.withPrefix))
      getElementById(view, "details_address_fullName").attr("value") must be("")
    }

    "display empty input with label for Address" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(2)>label").text() must be(messages(addressLine.withPrefix))
      getElementById(view, "details_address_addressLine").attr("value") must be("")
    }

    "display empty input with label for Town or City" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(3)>label").text() must be(messages(townOrCity.withPrefix))
      getElementById(view, "details_address_townOrCity").attr("value") must be("")
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(4)>label").text() must be(messages(postCode.withPrefix))
      getElementById(view, "details_address_postCode").attr("value") must be("")
    }

    "display empty input with label for Country" in {

      val view = createView()

      getElementByCss(view, "form>div.form-group>div:nth-child(2)>div:nth-child(5)>label").text() must be(messages(country.withPrefix))
      getElementById(view, "details.address.country").attr("value") must be("")
    }

    "display \"Back\" button that links to \"Representative Details\" page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be("Back")
      backButton.attr("href") must be("/customs-declare-exports/declaration/supplementary/representative-details")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be("Save and continue")
    }
  }

  "Consignee Details View with invalid input" should {

    "display error when both EORI and business details are not provided" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details", messages(nothingEntered.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, nothingEntered.withError, "#details")

      getElementByCss(view, "#error-message-details-input").text() must be(messages(nothingEntered.withError))
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.eori", messages(eori.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, eori.withError, "#details_eori")

      getElementByCss(view, "#error-message-details_eori-input").text() must be(messages(eori.withError))
    }

    "display error for empty Full name" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.fullName", messages(fullName.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullName.withEmpty, "#details_address_fullName")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullName.withEmpty))
    }

    "display error for incorrect Full name" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.fullName", messages(fullName.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullName.withError, "#details_address_fullName")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullName.withError))
    }

    "display error for empty Address" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.addressLine", messages(addressLine.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLine.withEmpty, "#details_address_addressLine")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(messages(addressLine.withEmpty))
    }

    "display error for incorrect Address" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.addressLine", messages(addressLine.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLine.withError, "#details_address_addressLine")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(messages(addressLine.withError))
    }

    "display error for empty Town or city" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.townOrCity", messages(townOrCity.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, townOrCity.withEmpty, "#details_address_townOrCity")

      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCity.withEmpty))
    }

    "display error for incorrect Town or city" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.townOrCity", messages(townOrCity.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, townOrCity.withError, "#details_address_townOrCity")

      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCity.withError))
    }

    "display error for empty Postcode" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.postCode", messages(postCode.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, postCode.withEmpty, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCode.withEmpty))
    }

    "display error for incorrect Postcode" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.postCode", messages(postCode.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, postCode.withError, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCode.withError))
    }

    "display error for empty Country" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.country", messages(country.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, country.withEmpty, "#details_address_country")


      getElementByCss(view, "span.error-message").text() must be(messages(country.withEmpty))
    }

    "display error for incorrect Country" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.country", messages(country.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, country.withError, "#details_address_country")

      getElementByCss(view, "span.error-message").text() must be(messages(country.withError))
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.addressLine", messages(addressLine.withEmpty))
        .withError("details.address.townOrCity", messages(townOrCity.withEmpty))
        .withError("details.address.postCode", messages(postCode.withEmpty))
        .withError("details.address.country", messages(country.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLine.withEmpty, "#details_address_addressLine")
      checkErrorLink(view, 2, townOrCity.withEmpty, "#details_address_townOrCity")
      checkErrorLink(view, 3, postCode.withEmpty, "#details_address_postCode")
      checkErrorLink(view, 4, country.withEmpty, "#details_address_country")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(messages(addressLine.withEmpty))
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCity.withEmpty))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCode.withEmpty))
      getElementByCss(view, "span.error-message").text() must be(messages(country.withEmpty))

    }

    "display errors when everything except Country is empty" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.fullName", messages(fullName.withEmpty))
        .withError("details.address.addressLine", messages(addressLine.withEmpty))
        .withError("details.address.townOrCity", messages(townOrCity.withEmpty))
        .withError("details.address.postCode", messages(postCode.withEmpty)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullName.withEmpty, "#details_address_fullName")
      checkErrorLink(view, 2, addressLine.withEmpty, "#details_address_addressLine")
      checkErrorLink(view, 3, townOrCity.withEmpty, "#details_address_townOrCity")
      checkErrorLink(view, 4, postCode.withEmpty, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullName.withEmpty))
      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(messages(addressLine.withEmpty))
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCity.withEmpty))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCode.withEmpty))
    }

    "display errors when everything except Full name is incorrect" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.addressLine", messages(addressLine.withError))
        .withError("details.address.townOrCity", messages(townOrCity.withError))
        .withError("details.address.postCode", messages(postCode.withError))
        .withError("details.address.country", messages(country.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, addressLine.withError, "#details_address_addressLine")
      checkErrorLink(view, 2, townOrCity.withError, "#details_address_townOrCity")
      checkErrorLink(view, 3, postCode.withError, "#details_address_postCode")
      checkErrorLink(view, 4, country.withError, "#details_address_country")

      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(messages(addressLine.withError))
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCity.withError))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCode.withError))
      getElementByCss(view, "span.error-message").text() must be(messages(country.withError))
    }

    "display errors when everything except Country is incorrect" in {

      val view = createView(ConsigneeDetails.form()
        .withError("details.address.fullName", messages(fullName.withError))
        .withError("details.address.addressLine", messages(addressLine.withError))
        .withError("details.address.townOrCity", messages(townOrCity.withError))
        .withError("details.address.postCode", messages(postCode.withError)))

      checkErrorsSummary(view)
      checkErrorLink(view, 1, fullName.withError, "#details_address_fullName")
      checkErrorLink(view, 2, addressLine.withError, "#details_address_addressLine")
      checkErrorLink(view, 3, townOrCity.withError, "#details_address_townOrCity")
      checkErrorLink(view, 4, postCode.withError, "#details_address_postCode")

      getElementByCss(view, "#error-message-details_address_fullName-input").text() must be(messages(fullName.withError))
      getElementByCss(view, "#error-message-details_address_addressLine-input").text() must be(messages(addressLine.withError))
      getElementByCss(view, "#error-message-details_address_townOrCity-input").text() must be(messages(townOrCity.withError))
      getElementByCss(view, "#error-message-details_address_postCode-input").text() must be(messages(postCode.withError))
    }
  }

  "Consignee Details View when filled" should {

    "display EORI" in {

      val form = ConsigneeDetails.form().fill(ConsigneeDetails(EntityDetails(Some("1234"), None)))
      val view = createView(form)

      getElementById(view, "details_eori").attr("value") must be("1234")
    }

    "display business address" in {

      val form = ConsigneeDetails.form().fill(ConsigneeDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
      val view = createView(form)

      getElementById(view, "details_address_fullName").attr("value") must be("test")
      getElementById(view, "details_address_addressLine").attr("value") must be("test1")
      getElementById(view, "details_address_townOrCity").attr("value") must be("test2")
      getElementById(view, "details_address_postCode").attr("value") must be("test3")
      getElementById(view, "details.address.country").attr("value") must be("test4")
    }
  }
}
