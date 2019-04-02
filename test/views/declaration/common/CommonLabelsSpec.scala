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

package views.declaration.common
import helpers.views.declaration.CommonMessages
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class CommonLabelsSpec extends ViewSpec with CommonMessages {

  "Button labels" should {

    "have proper value for \"Back\" button" in {
      assertMessage(backCaption, "Back")
    }

    "have proper value for \"Remove\" button" in {
      assertMessage(removeCaption, "Remove")
    }

    "have proper value for \"Add\" button" in {
      assertMessage(addCaption, "Add")
    }

    "have proper value for \"Save and continue\" button" in {
      assertMessage(saveAndContinueCaption, "Save and continue")
    }
  }

  "Global error labels" should {

    "have proper message for global error title" in {
      assertMessage(globalErrorTitle, "There is a problem - Declare customs exports for customs exports - GOV.UK")
    }

    "have proper message for global error heading" in {
      assertMessage(globalErrorHeading, "There is a problem with a service")
    }

    "have proper message for global error message" in {
      assertMessage(globalErrorMessage, "Please try again later.")
    }
  }

  "Error labels" should {

    "have proper message for error summary title" in {
      assertMessage(errorSummaryTitle, "There’s been a problem")
    }

    "have proper message for error summary text" in {
      assertMessage(errorSummaryText, "Check the following")
    }

    "have proper message for limit of items" in {
      assertMessage(limit, "You cannot add more items")
    }

    "have proper message for duplicated item" in {

      assertMessage(duplication, "You cannot add duplicated value")
    }

    "have proper message for adding at least one item" in {

      assertMessage(continueMandatory, "You must add at least one item")
    }
  }

  "DUCR error labels" should {

    "have proper message for incorrect DUCR" in {
      assertMessage(ucrError, "Incorrect DUCR")
    }
  }

  "EORI labels" should {

    "have proper message for EORI number" in {
      assertMessage(eori, "EORI number")
    }

    "have proper message for EORI hint" in {
      assertMessage(eoriHint, "The number starts with a country code, for example FR for France, and is then followed by up to 15 digits")
    }
  }

  "EORI error labels" should {

    "have proper message for empty EORI" in {
      assertMessage(eoriEmpty, "EORI number cannot be empty")
    }

    "have proper message for incorrect EORI" in {
      assertMessage(eoriError, "EORI number is incorrect")
    }

    "have proper message for empty EORI or address" in {
      assertMessage(eoriOrAddressEmpty, "Please, provide either EORI number or full Address details")
    }
  }

  "Address labels" should {

    "have proper message for Full name" in {
      assertMessage(fullName, "Full name")
    }

    "have proper message for Address line" in {
      assertMessage(addressLine, "Address line 1")
    }

    "have proper message for Town or City" in {
      assertMessage(townOrCity, "Town or city")
    }

    "have proper message for Postcode" in {
      assertMessage(postCode, "Postcode")
    }

    "have proper message for Country" in {
      assertMessage(country, "Country code")
    }
  }

  "Address error labels" should {

    "have proper message for empty Full name" in {
      assertMessage(fullNameEmpty, "Full name cannot be empty")
    }

    "have proper message for incorrect Full name" in {
      assertMessage(fullNameError, "Full name is incorrect")
    }

    "have proper message for empty Address line" in {
      assertMessage(addressLineEmpty, "Address line 1 cannot be empty")
    }

    "have proper message for incorrect Address line" in {
      assertMessage(addressLineError, "Address line 1 is incorrect")
    }

    "have proper message for empty Town or city" in {
      assertMessage(townOrCityEmpty, "Town or city cannot be empty")
    }

    "have proper message for incorrect Town or city" in {
      assertMessage(townOrCityError, "Town or city is incorrect")
    }

    "have proper message for empty Postcode" in {
      assertMessage(postCodeEmpty, "Postcode cannot be empty")
    }

    "have proper message for incorrect Postcode" in {
      assertMessage(postCodeError, "Postcode is incorrect")
    }

    "have proper message for Country empty" in {
      assertMessage(countryEmpty, "Country cannot be empty")
    }

    "have proper message for incorrect Country" in {
      assertMessage(countryError, "Country is incorrect")
    }
  }

  "Party labels" should {

    "have proper message for Party type" in {
      assertMessage(partyType, "Party type")
    }

    "have proper message for Consolidator" in {
      assertMessage(consolidator, "Consolidator")
    }

    "have proper message for Manufacturer" in {
      assertMessage(manufacturer, "Manufacturer")
    }

    "have proper message for Freight forwarder" in {
      assertMessage(freightForwarder, "Freight forwarder")
    }

    "have proper message for Warehouse keeper" in {
      assertMessage(warehouseKeeper, "Warehouse keeper")
    }
  }

  "Party error labels" should {

    "have proper message for empty party" in {
      assertMessage(partyTypeEmpty, "Please, choose party type")
    }

    "have proper message for incorrect party" in {
      assertMessage(partyTypeError, "Party type is incorrect")
    }
  }
}
