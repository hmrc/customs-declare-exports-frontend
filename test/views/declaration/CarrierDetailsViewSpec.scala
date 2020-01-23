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

import base.{Injector, TestHelper}
import controllers.declaration.routes
import controllers.util.SaveAndReturn
import forms.common.Address
import forms.declaration.{CarrierDetails, EntityDetails, Eori}
import helpers.views.declaration.CommonMessages
import models.Mode
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.MessagesApi
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.carrier_details
import views.tags.ViewTest

@ViewTest
class CarrierDetailsViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val form: Form[CarrierDetails] = CarrierDetails.form()
  private val carrierDetailsPage = new carrier_details(mainTemplate)

  private def createView(form: Form[CarrierDetails] = form)(implicit journeyRequest: JourneyRequest[_]): Document =
    carrierDetailsPage(Mode.Normal, form)

  "Carrier Details" should {

    "have correct messages" in {

      val messages = instanceOf[MessagesApi].preferred(request)
      messages must haveTranslationFor("supplementary.carrier.title")
      messages must haveTranslationFor("supplementary.carrier.title.hint")
      messages must haveTranslationFor("supplementary.carrier.eori.info")
      messages must haveTranslationFor("supplementary.carrier.address.info")
    }
  }

  "Carrier Details View on empty page" should {

    onEveryDeclarationJourney { implicit request =>
      val view = createView()
      "display page title" in {

        view.getElementById("title").text() mustBe messages("supplementary.carrier.title")
      }

      "display empty input with label for EORI" in {

        view.getElementById("details_eori-label").text() mustBe messages("supplementary.carrier.eori.info")
        view.getElementById("details_eori").attr("value") mustBe empty
      }

      "display empty input with label for Full name" in {

        view.getElementById("details_address_fullName-label").text() mustBe messages("supplementary.address.fullName")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display address label" in {

        view.getElementById("address-header").text() mustBe messages("supplementary.carrier.address.info")
        view.getElementById("details_address_fullName").attr("value") mustBe empty
      }

      "display empty input with label for Address" in {

        view.getElementById("details_address_addressLine-label").text() mustBe messages("supplementary.address.addressLine")
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
      }

      "display empty input with label for Town or City" in {

        view.getElementById("details_address_townOrCity-label").text() mustBe messages("supplementary.address.townOrCity")
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
      }

      "display empty input with label for Postcode" in {

        view.getElementById("details_address_postCode-label").text() mustBe messages("supplementary.address.postCode")
        view.getElementById("details_address_postCode").attr("value") mustBe empty
      }

      "display empty input with label for Country" in {

        view.getElementById("details_address_country-label").text() mustBe "supplementary.address.country"
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display 'Back' button that links to 'Representative Details' page" in {

        val backButton = view.getElementById("back-link")

        backButton.text() mustBe messages(backCaption)
        backButton.attr("href") mustBe routes.RepresentativeDetailsController.displayPage().url
      }

      "display 'Save and continue' button on page" in {
        view.getElementById("submit").text() mustBe messages(saveAndContinueCaption)
      }

      "display 'Save and return' button on page" in {
        view.getElementById("submit_and_return").text() mustBe messages(saveAndReturnCaption)
        view.getElementById("submit_and_return").attr("name") mustBe SaveAndReturn.toString
      }
    }
  }

  "Carrier Details View with invalid input" should {

    onEveryDeclarationJourney { implicit request =>
      "display error when both EORI and business details are empty" in {

        val view = createView(CarrierDetails.form().bind(Map[String, String]()))

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details", "#details")

        view.getElementById("error-message-details-input").text() mustBe messages("supplementary.namedEntityDetails.error")
      }

      "display error when EORI is provided, but is incorrect" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(Some(Eori(TestHelper.createRandomAlphanumericString(18))), None)))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_eori", "#details_eori")

        view.getElementById("error-message-details_eori-input").text() mustBe messages("supplementary.eori.error.format")
      }

      "display error for empty Full name" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("", "Test Street", "Leeds", "LS18BN", "England")))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_address_fullName", "#details_address_fullName")

        view.getElementById("error-message-details_address_fullName-input").text() mustBe messages("supplementary.address.fullName.empty")
      }

      "display error for incorrect Full name" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(
              CarrierDetails(
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
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "", "Leeds", "LS18BN", "England")))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_address_addressLine", "#details_address_addressLine")

        view.getElementById("error-message-details_address_addressLine-input").text() mustBe messages("supplementary.address.addressLine.empty")
      }

      "display error for incorrect Address" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(
              CarrierDetails(
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
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "", "LS18BN", "England")))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_address_townOrCity", "#details_address_townOrCity")

        view.getElementById("error-message-details_address_townOrCity-input").text() mustBe messages("supplementary.address.townOrCity.empty")
      }

      "display error for incorrect Town or city" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(
              CarrierDetails(
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
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "", "England")))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_address_postCode", "#details_address_postCode")

        view.getElementById("error-message-details_address_postCode-input").text() mustBe messages("supplementary.address.postCode.empty")
      }

      "display error for incorrect Postcode" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(
              CarrierDetails(
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
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "")))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_address_country", "#details_address_country")

        view.select("span.error-message").text() mustBe messages("supplementary.address.country.empty")
      }

      "display error for incorrect Country" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "Test Street", "Leeds", "LS18BN", "Barcelona")))))
        )

        view must haveGlobalErrorSummary
        view must haveFieldErrorLink("details_address_country", "#details_address_country")

        view.select("span.error-message").text() mustBe messages("supplementary.address.country.error")
      }

      "display errors when everything except Full name is empty" in {

        val view = createView(
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("Marco Polo", "", "", "", "")))))
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
          CarrierDetails
            .form()
            .fillAndValidate(CarrierDetails(EntityDetails(None, Some(Address("", "", "", "", "Ukraine")))))
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
          CarrierDetails
            .form()
            .fillAndValidate(
              CarrierDetails(
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
          CarrierDetails
            .form()
            .fillAndValidate(
              CarrierDetails(
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
  }

  "Carrier Details View when filled" should {

    onEveryDeclarationJourney { implicit request =>
      "display data in EORI input" in {

        val form = CarrierDetails.form().fill(CarrierDetails(EntityDetails(Some(Eori("1234")), None)))
        val view = createView(form)

        view.getElementById("details_eori").attr("value") mustBe "1234"
        view.getElementById("details_address_fullName").attr("value") mustBe empty
        view.getElementById("details_address_addressLine").attr("value") mustBe empty
        view.getElementById("details_address_townOrCity").attr("value") mustBe empty
        view.getElementById("details_address_postCode").attr("value") mustBe empty
        view.getElementById("details_address_country").attr("value") mustBe empty
      }

      "display data in Business address inputs" in {

        val form = CarrierDetails
          .form()
          .fill(CarrierDetails(EntityDetails(None, Some(Address("test", "test1", "test2", "test3", "test4")))))
        val view = createView(form)

        view.getElementById("details_eori").attr("value") mustBe empty
        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "test4"
      }

      "display data in both EORI and Business address inputs" in {

        val form = CarrierDetails
          .form()
          .fill(CarrierDetails(EntityDetails(Some(Eori("1234")), Some(Address("test", "test1", "test2", "test3", "test4")))))
        val view = createView(form)

        view.getElementById("details_eori").attr("value") mustBe "1234"
        view.getElementById("details_address_fullName").attr("value") mustBe "test"
        view.getElementById("details_address_addressLine").attr("value") mustBe "test1"
        view.getElementById("details_address_townOrCity").attr("value") mustBe "test2"
        view.getElementById("details_address_postCode").attr("value") mustBe "test3"
        view.getElementById("details_address_country").attr("value") mustBe "test4"
      }
    }
  }
}
