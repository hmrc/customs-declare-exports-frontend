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
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.Address
import forms.declaration.{ConsigneeDetails, EntityDetails}
import helpers.views.declaration.{CommonMessages, ConsigneeDetailsMessages}
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.consignee_details
import views.tags.ViewTest

@ViewTest
class ConsigneeDetailsViewSpec extends UnitViewSpec with ConsigneeDetailsMessages with CommonMessages with Stubs {

  val form: Form[ConsigneeDetails] = ConsigneeDetails.form()
  val consigneeDetailsPage = new consignee_details(mainTemplate)
  private def createView(form: Form[ConsigneeDetails] = form): Document = consigneeDetailsPage(Mode.Normal, form)

  "Consignee Details View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages(title)
    }

    "display section header" in {

      val view = createView()

      view.getElementById("section-header").text() mustBe messages("supplementary.summary.parties.header")
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("details_eori-label").text() mustBe messages(eoriInfo)
      view.getElementById("details_eori").attr("value") mustBe empty
    }

    "display empty input with label for Full name" in {

      val view = createView()

      view.getElementById("details_address_fullName-label").text() mustBe messages(fullName)
      view.getElementById("details_address_fullName").attr("value") mustBe empty
    }

    "display address label" in {

      val view = createView()

      view.getElementById("address-header").text() mustBe messages(addressInfo)
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

    "display 'Back' button that links to 'Exporter Details' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.ExporterDetailsController.displayPage().url
    }

    "display 'Save and continue' button on page" in {
      createView().getElementById("submit").text() mustBe messages(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      val button = createView().getElementById("submit_and_return")
      button.text() mustBe messages(saveAndReturnCaption)
      button.attr("name") mustBe SaveAndReturn.toString
    }
  }

  "Consignee Details View with invalid input" should {

    "display error when both EORI and business details are empty" in {

      val view = createView(ConsigneeDetails.form().bind(Map[String, String]()))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details", "#details")

      view.getElementById("error-message-details-input").text() mustBe messages(eoriOrAddressEmpty)
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(Some(TestHelper.createRandomAlphanumericString(18)), None)))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.eori", "#details_eori")

      view.getElementById("error-message-details_eori-input").text() mustBe messages(eoriError)
    }

    "display error for empty Full name" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England"))))
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.fullName", "#details_address_fullName")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages(fullNameEmpty)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.fullName", "#details_address_fullName")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages(fullNameError)
    }

    "display error for empty Address" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England"))))
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages(addressLineEmpty)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages(addressLineError)
    }

    "display error for empty Town or city" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England"))))
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")

      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityEmpty)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")

      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityError)
    }

    "display error for empty Postcode" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England"))))
          )
      )
      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages(postCodeEmpty)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages(postCodeError)
    }

    "display error for empty Country" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", ""))))
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.country", "#details_address_country")

      view.select("span.error-message").text() mustBe messages(countryEmpty)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.country", "#details_address_country")

      view.select("span.error-message").text() mustBe messages(countryError)
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details.address.postCode", "#details_address_postCode")
      view must haveFieldErrorLink("details.address.country", "#details_address_country")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages(addressLineEmpty)
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityEmpty)
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages(postCodeEmpty)
      view.select("span.error-message").text() mustBe messages(countryEmpty)
    }

    "display errors when everything except Country is empty" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.fullName", "#details_address_fullName")
      view must haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages(fullNameEmpty)
      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages(addressLineEmpty)
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityEmpty)
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages(postCodeEmpty)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details.address.postCode", "#details_address_postCode")
      view must haveFieldErrorLink("details.address.country", "#details_address_country")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages(addressLineError)
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityError)
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages(postCodeError)
      view.select("span.error-message").text() mustBe messages(countryError)
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

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details.address.fullName", "#details_address_fullName")
      view must haveFieldErrorLink("details.address.addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details.address.townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details.address.postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages(fullNameError)
      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages(addressLineError)
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages(townOrCityError)
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages(postCodeError)
    }
  }

  "Consignee Details View when filled" should {

    "display data in EORI input" in {

      val form = ConsigneeDetails.form().fill(ConsigneeDetails(EntityDetails(Some("1234"), None)))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") mustBe "1234"
      view.getElementById("details_address_fullName").attr("value") mustBe empty
      view.getElementById("details_address_addressLine").attr("value") mustBe empty
      view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      view.getElementById("details_address_postCode").attr("value") mustBe empty
      view.getElementById("details.address.country").attr("value") mustBe empty
    }

    "display data in Business address inputs" in {

      val form = ConsigneeDetails
        .form()
        .fill(ConsigneeDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "Ukraine")))))
      val view = createView(form)

      view.getElementById("details_eori").attr("value") mustBe empty
      view.getElementById("details_address_fullName").attr("value") mustBe "test"
      view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
      view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
      view.getElementById("details_address_postCode").attr("value") mustBe "test3"
      view.getElementById("details.address.country").attr("value") mustBe "Ukraine"
    }

    "display data in both EORI and Business address inputs" in {

      val form = ConsigneeDetails
        .form()
        .fill(ConsigneeDetails(EntityDetails(Some("1234"), Some(Address("test", "test1", "test2", "test3", "test4")))))
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
