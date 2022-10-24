/*
 * Copyright 2022 HM Revenue & Customs
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

import base.ExportsTestData.eori
import base.Injector
import base.TestHelper.createRandomAlphanumericString
import controllers.declaration.routes
import forms.common.Eori
import forms.declaration.declarationHolder.DeclarationHolder
import models.declaration.EoriSource.{OtherEori, UserEori}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.declarationHolder.declaration_holder_change
import views.tags.ViewTest

@ViewTest
class DeclarationHolderChangeViewSpec extends UnitViewSpec with CommonMessages with Stubs with Injector {

  val declarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB42354735346235")), Some(OtherEori))
  val id = "ACE-GB42354735346235"

  private val declarationHolderPage = instanceOf[declaration_holder_change]

  private def createForm(implicit request: JourneyRequest[_]): Form[DeclarationHolder] =
    DeclarationHolder.form(eori, request.cacheModel.additionalDeclarationType)

  private def createView(form: Form[DeclarationHolder])(implicit request: JourneyRequest[_]): Document =
    declarationHolderPage(id, form, eori)(request, messages)

  "Declaration Holder View when filled" should {
    onEveryDeclarationJourney() { implicit request =>
      val view = createView(createForm.fill(declarationHolder))

      "display page title" in {
        view.getElementsByTag("h1").text mustBe messages("declaration.declarationHolder.title")
      }

      "display section header" in {
        view.getElementById("section-header").text must include(messages("declaration.section.2"))
      }

      "display 'Back' button that links to 'Summary' page" in {
        val backButton = view.getElementById("back-link")

        backButton.text mustBe messages(backToPreviousQuestionCaption)
        backButton.attr("href") mustBe routes.DeclarationHolderSummaryController.displayPage().url
      }

      "display data in authorisationTypeCode" in {
        view.getElementById("authorisationTypeCode").attr("value") mustBe declarationHolder.authorisationTypeCode.get
      }

      "display data UserEori checked" in {
        val declarationHolder = DeclarationHolder(Some("test"), Some(Eori("test1")), Some(UserEori))
        val view = createView(createForm.fill(declarationHolder))

        view.getElementById("conditional-OtherEori").attr("class").contains("hidden") mustBe true
        view.getElementById("UserEori").getElementsByAttribute("checked").size mustBe 1
      }

      "display data OtherEori checked and data is present in EORI input" in {
        val declarationHolder = DeclarationHolder(None, Some(Eori("test")), Some(OtherEori))
        val view = createView(createForm.fill(declarationHolder))

        view.getElementById("authorisationTypeCode").attr("value") mustBe empty
        view.getElementById("conditional-OtherEori").attr("class").contains("hidden") mustBe false
        view.getElementById("OtherEori").getElementsByAttribute("checked").size mustBe 1
        view.getElementById("eori").attr("value") mustBe "test"
      }

    }
  }

  "Declaration Holder View for invalid input" should {
    onEveryDeclarationJourney() { implicit request =>
      /*
       * Both add and save button returns the same errors, so
       * no point to distinguish them and move to controller test
       */
      "display error for empty Authorisation code" in {
        val eori = Eori(createRandomAlphanumericString(17))
        val declarationHolder = DeclarationHolder(None, Some(eori), Some(OtherEori))
        val view = createView(createForm.fillAndValidate(declarationHolder))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#authorisationTypeCode")

        view must containErrorElementWithMessageKey("declaration.declarationHolder.authorisationCode.empty")
      }

      "display error for incorrect EORI" in {
        val eori = Eori(createRandomAlphanumericString(18))
        val declarationHolder = DeclarationHolder(Some("ACE"), Some(eori), Some(OtherEori))
        val view = createView(createForm.fillAndValidate(declarationHolder))

        view must haveGovukGlobalErrorSummary
        view must containErrorElementWithTagAndHref("a", "#eori")

        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }

      "display error for both incorrect fields" in {
        val eori = Eori(createRandomAlphanumericString(18))
        val declarationHolder = DeclarationHolder(None, Some(eori), Some(OtherEori))
        val view = createView(createForm.fillAndValidate(declarationHolder))

        view must haveGovukGlobalErrorSummary

        view must containErrorElementWithTagAndHref("a", "#authorisationTypeCode")
        view must containErrorElementWithTagAndHref("a", "#eori")

        view must containErrorElementWithMessageKey("declaration.declarationHolder.authorisationCode.empty")
        view must containErrorElementWithMessageKey("declaration.eori.error.format")
      }
    }
  }
}
