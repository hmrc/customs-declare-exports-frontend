/*
 * Copyright 2024 HM Revenue & Customs
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

package views.section5

import base.{Injector, TestHelper}
import controllers.section5.routes.{AdditionalInformationController, AdditionalInformationRequiredController}
import forms.section5.AdditionalInformation
import forms.section5.AdditionalInformation.form
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import services.cache.ExportsTestHelper
import views.common.PageWithButtonsSpec
import views.html.section5.additionalInformation.additional_information_add
import views.tags.ViewTest

@ViewTest
class AdditionalInformationAddViewSpec extends PageWithButtonsSpec with ExportsTestHelper with Injector {

  val page = instanceOf[additional_information_add]

  override val typeAndViewInstance = (STANDARD, page(itemId, form)(_, _))

  def createView(frm: Form[AdditionalInformation] = form)(implicit request: JourneyRequest[_]): Document =
    page(itemId, frm)(request, messages)

  "Additional Information Add View" should {
    "have a proper messages" in {
      messages must haveTranslationFor("declaration.additionalInformation.title")
      messages must haveTranslationFor("declaration.additionalInformation.code")
      messages must haveTranslationFor("declaration.additionalInformation.item.code")
      messages must haveTranslationFor("declaration.additionalInformation.code.error")
      messages must haveTranslationFor("declaration.additionalInformation.code.empty")
      messages must haveTranslationFor("declaration.additionalInformation.description")
      messages must haveTranslationFor("declaration.additionalInformation.item.description")
      messages must haveTranslationFor("declaration.additionalInformation.description.error")
      messages must haveTranslationFor("declaration.additionalInformation.description.empty")
    }
  }

  "Additional Information Add on empty page" should {
    onEveryDeclarationJourney() { implicit request =>
      "display page title" in {
        createView().getElementsByTag("h1") must containMessageForElements("declaration.additionalInformation.title")
      }

      "display section header" in {
        createView().getElementById("section-header") must containMessage("declaration.section.5")
      }

      "display empty input with label for Union code" in {
        val view = createView()
        view.getElementsByAttributeValue("for", "code") must containMessageForElements("declaration.additionalInformation.code")
        view.getElementById("code").attr("value") mustBe empty
      }

      "display empty input with label for Description" in {
        val view = createView()
        view.getElementsByAttributeValue("for", "description").text() mustBe messages("declaration.additionalInformation.description")
        view.getElementById("description").attr("value") mustBe empty
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }

  "Additional Information Add when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      "display data in both inputs" in {
        val view = createView(form.fill(AdditionalInformation("12345", "12345")))
        view.getElementById("code").attr("value") mustBe "12345"
        view.getElementById("description").text() mustBe "12345"

      }

      "display data in code input" in {
        val view = createView(form.fill(AdditionalInformation("12345", "")))
        view.getElementById("code").attr("value") mustBe "12345"
        view.getElementById("description").text() mustBe empty
      }

      "display data in description input" in {
        val view = createView(form.fill(AdditionalInformation("", "12345")))

        view.getElementById("code").attr("value") mustBe empty
        view.getElementById("description").text() mustBe "12345"
      }
    }
  }

  "Additional Information Add back links" should {

    onEveryDeclarationJourney() { implicit request =>
      "have link to is additional information required when cache is empty" in {
        val backButton = createView().getElementById("back-link")
        backButton must containText(messages(backToPreviousQuestionCaption))
        backButton must haveHref(AdditionalInformationRequiredController.displayPage(itemId))
      }
    }

    val item = withItem(anItem(withItemId(itemId), withAdditionalInformation("12345", "Description")))
    onEveryDeclarationJourney(item) { implicit request =>
      "have link to additional information list when cache contains data" in {
        val backButton = createView().getElementById("back-link")
        backButton must containText(messages(backToPreviousQuestionCaption))
        backButton must haveHref(AdditionalInformationController.displayPage(itemId))
      }
    }
  }

  "Additional Information Add for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      "display error for missing code" in {
        val view = createView(form.fillAndValidate(AdditionalInformation("", "description")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code")
        view must containErrorElementWithMessageKey("declaration.additionalInformation.code.empty")
      }

      "display error for invalid code" in {
        val view = createView(form.fillAndValidate(AdditionalInformation("1234", "description")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#code")
        view must containErrorElementWithMessageKey("declaration.additionalInformation.code.error")
      }

      "display error for missing description" in {
        val view = createView(form.fillAndValidate(AdditionalInformation("12345", "")))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#description")
        view must containErrorElementWithMessageKey("declaration.additionalInformation.description.empty")
      }

      "display error for invalid description" in {
        val view = createView(form.fillAndValidate(AdditionalInformation("12345", TestHelper.createRandomAlphanumericString(101))))
        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#description")
        view must containErrorElementWithMessageKey("declaration.additionalInformation.description.error")
      }
    }
  }
}
