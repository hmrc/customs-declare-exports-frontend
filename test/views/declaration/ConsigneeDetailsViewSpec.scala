/*
 * Copyright 2020 HM Revenue & Customs
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
import helpers.views.declaration.CommonMessages
import models.Mode
import org.jsoup.nodes.Document
import play.api.data.Form
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.consignee_details
import views.tags.ViewTest

@ViewTest
class ConsigneeDetailsViewSpec extends UnitViewSpec with CommonMessages with Stubs {

  val form: Form[ConsigneeDetails] = ConsigneeDetails.form()
  val consigneeDetailsPage = new consignee_details(mainTemplate)
  private def createView(form: Form[ConsigneeDetails] = form): Document = consigneeDetailsPage(Mode.Normal, form)

  "Consignee Details View on empty page" should {

    "display page title" in {

      createView().getElementById("title").text() mustBe messages("supplementary.consignee.title")
    }

    "display section header" in {

      val view = createView()

      view.getElementById("section-header").text() must include(messages("supplementary.summary.parties.header"))
    }

    "display empty input with label for EORI" in {

      val view = createView()

      view.getElementById("details_eori-label").text() mustBe messages("supplementary.consignee.eori.info")
      view.getElementById("details_eori").attr("value") mustBe empty
    }

    "display empty input with label for Full name" in {

      val view = createView()

      view.getElementById("details_address_fullName-label").text() mustBe messages("supplementary.address.fullName")
      view.getElementById("details_address_fullName").attr("value") mustBe empty
    }

    "display address label" in {

      val view = createView()

      view.getElementById("address-header").text() mustBe messages("supplementary.consignee.address.info")
      view.getElementById("details_address_fullName").attr("value") mustBe empty
    }

    "display empty input with label for Address" in {

      val view = createView()

      view.getElementById("details_address_addressLine-label").text() mustBe messages("supplementary.address.addressLine")
      view.getElementById("details_address_addressLine").attr("value") mustBe empty
    }

    "display empty input with label for Town or City" in {

      val view = createView()

      view.getElementById("details_address_townOrCity-label").text() mustBe messages("supplementary.address.townOrCity")
      view.getElementById("details_address_townOrCity").attr("value") mustBe empty
    }

    "display empty input with label for Postcode" in {

      val view = createView()

      view.getElementById("details_address_postCode-label").text() mustBe messages("supplementary.address.postCode")
      view.getElementById("details_address_postCode").attr("value") mustBe empty
    }

    "display empty input with label for Country" in {

      val view = createView()

      view.getElementById("details.address.country-label").text() mustBe messages("supplementary.address.country")
      view.getElementById("details.address.country").attr("value") mustBe empty
    }

    "display 'Back' button that links to 'Exporter Details' page" in {

      val backButton = createView().getElementById("back-link")

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

      view.getElementById("error-message-details-input").text() mustBe messages("supplementary.namedEntityDetails.error")
    }

    "display error when EORI is provided, but is incorrect" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(Some(TestHelper.createRandomAlphanumericString(17)), None)))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_eori", "#details_eori")

      view.getElementById("error-message-details_eori-input").text() mustBe messages("supplementary.eori.nongb.error")
    }

    "display error for empty Full name" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages("supplementary.address.fullName.empty")
    }

    "display error for incorrect Full name" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
              EntityDetails(None, Some(Address(TestHelper.createRandomAlphanumericString(71), "Test Street", "Leeds", "LS18BN", "England")))
            )
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages("supplementary.address.fullName.error")
    }

    "display error for empty Address" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.empty")
    }

    "display error for incorrect Address" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
              EntityDetails(None, Some(Address("Marco Polo", TestHelper.createRandomAlphanumericString(71), "Leeds", "LS18BN", "England")))
            )
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.error")
    }

    "display error for empty Town or city" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")

      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.empty")
    }

    "display error for incorrect Town or city" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", TestHelper.createRandomAlphanumericString(71), "LS18BN", "England")))
            )
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")

      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.error")
    }

    "display error for empty Postcode" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England")))))
      )
      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.empty")
    }

    "display error for incorrect Postcode" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(
            ConsigneeDetails(
              EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", TestHelper.createRandomAlphanumericString(71), "England")))
            )
          )
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.error")
    }

    "display error for empty Country" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.select("span.error-message").text() mustBe messages("supplementary.address.country.empty")
    }

    "display error for incorrect Country" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.select("span.error-message").text() mustBe messages("supplementary.address.country.error")
    }

    "display errors when everything except Full name is empty" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.empty")
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.empty")
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.empty")
      view.select("span.error-message").text() mustBe messages("supplementary.address.country.empty")
    }

    "display errors when everything except Country is empty" in {

      val view = createView(
        ConsigneeDetails
          .form()
          .fillAndValidate(ConsigneeDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
      )

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages("supplementary.address.fullName.empty")
      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.empty")
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.empty")
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.empty")
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
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")
      view must haveFieldErrorLink("details_address_country", "#details_address_country")

      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.error")
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.error")
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.error")
      view.select("span.error-message").text() mustBe messages("supplementary.address.country.error")
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
      view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")
      view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")
      view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")
      view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

      view.getElementById("error-message-details_address_fullName-input").text() mustBe messages("supplementary.address.fullName.error")
      view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.error")
      view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.error")
      view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.error")
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
