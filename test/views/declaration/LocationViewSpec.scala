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
import forms.declaration.GoodsLocation
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Call
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.goods_location
import views.tags.ViewTest

@ViewTest
class LocationViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {

  private val page = new goods_location(mainTemplate)
  private val form: Form[GoodsLocation] = GoodsLocation.form()
  private def createView(
    mode: Mode = Mode.Normal,
    form: Form[GoodsLocation] = form,
    messages: Messages = stubMessages(),
    declarationType: DeclarationType = DeclarationType.STANDARD
  ): Document =
    page(mode, form)(journeyRequest(declarationType), messages)

  "Location View on empty page" should {

    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.summary.locations.header")
      messages must haveTranslationFor("supplementary.goodsLocation.title")
      messages must haveTranslationFor("supplementary.address.country")
      messages must haveTranslationFor("supplementary.address.country.empty")
      messages must haveTranslationFor("supplementary.address.country.error")
      messages must haveTranslationFor("supplementary.goodsLocation.typeOfLocation")
      messages must haveTranslationFor("supplementary.goodsLocation.qualifierOfIdentification")
      messages must haveTranslationFor("supplementary.goodsLocation.identificationOfLocation")
      messages must haveTranslationFor("supplementary.goodsLocation.additionalIdentifier")
      messages must haveTranslationFor("supplementary.goodsLocation.addressLine")
      messages must haveTranslationFor("supplementary.goodsLocation.postCode")
      messages must haveTranslationFor("supplementary.goodsLocation.city")
      messages must haveTranslationFor("declaration.natureOfTransaction.empty")
      messages must haveTranslationFor("declaration.natureOfTransaction.error")
      messages must haveTranslationFor("supplementary.goodsLocation.typeOfLocation.empty")
      messages must haveTranslationFor("supplementary.goodsLocation.typeOfLocation.error")
      messages must haveTranslationFor("supplementary.goodsLocation.qualifierOfIdentification.empty")
      messages must haveTranslationFor("supplementary.goodsLocation.qualifierOfIdentification.error")
      messages must haveTranslationFor("supplementary.goodsLocation.identificationOfLocation.error")
      messages must haveTranslationFor("supplementary.goodsLocation.additionalIdentifier.error")
      messages must haveTranslationFor("supplementary.goodsLocation.addressLine.error")
      messages must haveTranslationFor("supplementary.goodsLocation.postCode.error")
      messages must haveTranslationFor("supplementary.goodsLocation.city.error")
    }

    val view = createView()

    "display same page title as header" in {
      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header").text() must include("declaration.summary.locations.header")
    }

    "display header" in {
      view.getElementById("title").text() mustBe "supplementary.goodsLocation.title"
    }

    "display empty input with label for Country" in {
      view.getElementById("country-label").text() mustBe "supplementary.address.country"
      view.getElementById("country").attr("value") mustBe empty
    }

    "display empty input with label for Type of Location" in {
      view.getElementById("typeOfLocation-label").text() mustBe "supplementary.goodsLocation.typeOfLocation"
      view.getElementById("typeOfLocation").attr("value") mustBe empty
    }

    "display empty input with label for Qualifier of Identification" in {
      view
        .getElementById("qualifierOfIdentification-label")
        .text() mustBe "supplementary.goodsLocation.qualifierOfIdentification"
      view.getElementById("qualifierOfIdentification").attr("value") mustBe empty
    }

    "display empty input with label for Identification of Location" in {
      view
        .getElementById("identificationOfLocation-label")
        .text() mustBe "supplementary.goodsLocation.identificationOfLocation"
      view.getElementById("identificationOfLocation").attr("value") mustBe empty
    }

    "display empty input with label for Additional Identifier" in {
      view.getElementById("additionalIdentifier-label").text() mustBe "supplementary.goodsLocation.additionalIdentifier"
      view.getElementById("additionalIdentifier").attr("value") mustBe empty
    }

    "display empty input with label for Street and Number" in {
      view.getElementById("addressLine-label").text() mustBe "supplementary.goodsLocation.addressLine"
      view.getElementById("addressLine").attr("value") mustBe empty
    }

    "display empty input with label for Postcode" in {
      view.getElementById("postCode-label").text() mustBe "supplementary.goodsLocation.postCode"
      view.getElementById("postCode").attr("value") mustBe empty
    }

    "display empty input with label for City" in {
      view.getElementById("city-label").text() mustBe "supplementary.goodsLocation.city"
      view.getElementById("city").attr("value") mustBe empty
    }

    "display 'Save and continue' button" in {
      val saveButton = view.getElementById("submit")
      saveButton.text() mustBe "site.save_and_continue"
    }

    "display 'Save and return' button" in {
      val saveButton = view.getElementById("submit_and_return")
      saveButton.text() mustBe "site.save_and_come_back_later"
    }
  }

  "Location View for invalid input" should {

    "display error for empty Country" in {
      val form =
        GoodsLocation.form.fillAndValidate(GoodsLocation("", "t", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("country", "#country")

      view.select("span.error-message").text() mustBe "supplementary.address.country.empty"
    }

    "display error for incorrect Country" in {

      val form =
        GoodsLocation.form.fillAndValidate(GoodsLocation("TST", "t", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("country", "#country")

      view.select("span.error-message").text() mustBe "supplementary.address.country.error"
    }

    "display error for empty Type of Location" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("typeOfLocation", "#typeOfLocation")

      view
        .select("#error-message-typeOfLocation-input")
        .text() mustBe "supplementary.goodsLocation.typeOfLocation.empty"
    }

    "display error for incorrect Type of Location" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "TST", "t", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("typeOfLocation", "#typeOfLocation")

      view
        .select("#error-message-typeOfLocation-input")
        .text() mustBe "supplementary.goodsLocation.typeOfLocation.error"
    }

    "display error for empty Qualifier of Identification" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "t", "", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("qualifierOfIdentification", "#qualifierOfIdentification")

      view
        .select("#error-message-qualifierOfIdentification-input")
        .text() mustBe "supplementary.goodsLocation.qualifierOfIdentification.empty"
    }

    "display error for incorrect Qualifier of Identification" in {

      val form =
        GoodsLocation.form
          .fillAndValidate(GoodsLocation("Poland", "t", "@@!", Some("TST"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("qualifierOfIdentification", "#qualifierOfIdentification")

      view
        .select("#error-message-qualifierOfIdentification-input")
        .text() mustBe "supplementary.goodsLocation.qualifierOfIdentification.error"
    }

    "display error for incorrect Identification of Location" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("@@!"), Some("TST"), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("identificationOfLocation", "#identificationOfLocation")

      view
        .select("#error-message-identificationOfLocation-input")
        .text() mustBe "supplementary.goodsLocation.identificationOfLocation.error"
    }

    "display error for incorrect Additional Identifier" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("TST"), Some(TestHelper.createRandomAlphanumericString(33)), None, None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("additionalIdentifier", "#additionalIdentifier")

      view
        .select("#error-message-additionalIdentifier-input")
        .text() mustBe "supplementary.goodsLocation.additionalIdentifier.error"
    }

    "display error for incorrect Street and Number" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("TST"), Some("TST"), Some(TestHelper.createRandomAlphanumericString(71)), None, None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("addressLine", "#addressLine")

      view.select("#error-message-addressLine-input").text() mustBe "supplementary.goodsLocation.addressLine.error"
    }

    "display error for incorrect Postcode" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("TST"), Some("TST"), None, Some(TestHelper.createRandomAlphanumericString(10)), None))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("postCode", "#postCode")

      view.select("#error-message-postCode-input").text() mustBe "supplementary.goodsLocation.postCode.error"
    }

    "display error for incorrect City" in {

      val form = GoodsLocation.form
        .fillAndValidate(GoodsLocation("Poland", "t", "t", Some("TST"), Some("TST"), None, None, Some(TestHelper.createRandomAlphanumericString(36))))
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("city", "#city")

      view.select("#error-message-city-input").text() mustBe "supplementary.goodsLocation.city.error"
    }

    "display errors for everything incorrect" in {

      val form = GoodsLocation.form
        .fillAndValidate(
          GoodsLocation(
            "Country",
            "ABC",
            "ABC",
            Some(TestHelper.createRandomAlphanumericString(36)),
            Some(TestHelper.createRandomAlphanumericString(4)),
            Some(TestHelper.createRandomAlphanumericString(71)),
            Some(TestHelper.createRandomAlphanumericString(10)),
            Some(TestHelper.createRandomAlphanumericString(36))
          )
        )
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("country", "#country")
      haveFieldErrorLink("typeOfLocation", "#typeOfLocation")
      haveFieldErrorLink("qualifierOfIdentification", "#qualifierOfIdentification")
      haveFieldErrorLink("identificationOfLocation", "#identificationOfLocation")
      haveFieldErrorLink("additionalIdentifier", "#additionalIdentifier")
      haveFieldErrorLink("addressLine", "#addressLine")
      haveFieldErrorLink("postCode", "#postCode")
      haveFieldErrorLink("city", "#city")

      view.select("span.error-message").text() mustBe "supplementary.address.country.error"
      view
        .select("#error-message-typeOfLocation-input")
        .text() mustBe "supplementary.goodsLocation.typeOfLocation.error"
      view
        .select("#error-message-qualifierOfIdentification-input")
        .text() mustBe "supplementary.goodsLocation.qualifierOfIdentification.error"
      view
        .select("#error-message-identificationOfLocation-input")
        .text() mustBe "supplementary.goodsLocation.identificationOfLocation.error"
      view
        .select("#error-message-additionalIdentifier-input")
        .text() mustBe "supplementary.goodsLocation.additionalIdentifier.error"
      view.select("#error-message-postCode-input").text() mustBe "supplementary.goodsLocation.postCode.error"
      view.select("#error-message-city-input").text() mustBe "supplementary.goodsLocation.city.error"
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
      val view = createView(form = form)

      checkErrorsSummary(view)
      haveFieldErrorLink("country", "#country")
      haveFieldErrorLink("typeOfLocation", "#typeOfLocation")
      haveFieldErrorLink("qualifierOfIdentification", "#qualifierOfIdentification")
      haveFieldErrorLink("additionalIdentifier", "#additionalIdentifier")
      haveFieldErrorLink("addressLine", "#addressLine")
      haveFieldErrorLink("postCode", "#postCode")
      haveFieldErrorLink("city", "#city")

      view.select("span.error-message").text() mustBe "supplementary.address.country.error"
      view
        .select("#error-message-typeOfLocation-input")
        .text() mustBe "supplementary.goodsLocation.typeOfLocation.error"
      view
        .select("#error-message-qualifierOfIdentification-input")
        .text() mustBe "supplementary.goodsLocation.qualifierOfIdentification.error"
      view
        .select("#error-message-additionalIdentifier-input")
        .text() mustBe "supplementary.goodsLocation.additionalIdentifier.error"
      view.select("#error-message-postCode-input").text() mustBe "supplementary.goodsLocation.postCode.error"
      view.select("#error-message-city-input").text() mustBe "supplementary.goodsLocation.city.error"
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
          GoodsLocation("Poland", "AB", "CD", Some("TST"), Some(ladditionalInformation), Some(lstreetAndNumber), Some(lpostCode), Some(lcity))
        )
      val view = createView(form = form)
      view.getElementById("country").attr("value") must be("Poland")
      view.getElementById("typeOfLocation").attr("value") must be("AB")
      view.getElementById("qualifierOfIdentification").attr("value") must be("CD")
      view.getElementById("identificationOfLocation").attr("value") must be("TST")
      view.getElementById("additionalIdentifier").attr("value") must be(ladditionalInformation)
      view.getElementById("addressLine").attr("value") must be(lstreetAndNumber)
      view.getElementById("postCode").attr("value") must be(lpostCode)
      view.getElementById("city").attr("value") must be(lcity)
    }
  }

  "Goods Location view" should {
    def viewWithCorrectBackButton(declarationType: DeclarationType, redirect: Call): Unit =
      "have correct back-link" when {
        val view = createView(declarationType = declarationType)
        "display 'Back' button that links to correct page" in {
          val backButton = view.getElementById("back-link")

          backButton must containText("site.back")
          backButton.getElementById("back-link") must haveHref(redirect)
        }
      }

    "for Standard declaration" should {
      behave like viewWithCorrectBackButton(DeclarationType.STANDARD, controllers.declaration.routes.RoutingCountriesSummaryController.displayPage())
    }
    "for Simplified declaration" should {
      behave like viewWithCorrectBackButton(
        DeclarationType.SIMPLIFIED,
        controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      )
    }
    "for Occasional declaration" should {
      behave like viewWithCorrectBackButton(
        DeclarationType.OCCASIONAL,
        controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      )
    }
    "for Supplementary declaration" should {
      behave like viewWithCorrectBackButton(DeclarationType.SUPPLEMENTARY, controllers.declaration.routes.DestinationCountryController.displayPage())
    }
    "for Clearance request" should {
      behave like viewWithCorrectBackButton(DeclarationType.CLEARANCE, controllers.declaration.routes.DestinationCountryController.displayPage())
    }
  }
}
