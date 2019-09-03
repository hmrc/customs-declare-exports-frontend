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

import base.Injector
import play.api.i18n.MessagesApi
import play.api.test.Helpers.stubMessages
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.not_eligible
import views.tags.ViewTest

@ViewTest
class NotEligibleViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {
  private val view = new not_eligible(mainTemplate)()(journeyRequest(), stubMessages())

  "Not Eligible View on empty page" should {
    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.natureOfTransaction.title")
      messages must haveTranslationFor("notEligible.pageTitle")
      messages must haveTranslationFor("notEligible.title")
      messages must haveTranslationFor("notEligible.titleLineTwo")
      messages must haveTranslationFor("notEligible.descriptionPreUrl")
      messages must haveTranslationFor("notEligible.descriptionUrl")
      messages must haveTranslationFor("notEligible.descriptionPostUrl")
      messages must haveTranslationFor("notEligible.referenceTitle")
      messages must haveTranslationFor("notEligible.reference.text")
    }

    "display page title" in {
      view.select("title").text() mustBe "notEligible.pageTitle"
    }

    "display header with hint" in {
      view.select("h1").text() mustBe "notEligible.title notEligible.titleLineTwo"
    }

    "display CHIEF information" in {
      view.select("p:nth-child(3)").text() mustBe
        "notEligible.descriptionPreUrl " +
          "notEligible.descriptionUrl " +
          "notEligible.descriptionPostUrl"
    }

    "display CHIEF link" in {
      view.select("p:nth-child(3)>a").attr("href") mustBe "https://secure.hmce.gov.uk/ecom/login/index.html"
    }

    "display Help and Support with description" in {
      view.select("h3").text() mustBe "notEligible.referenceTitle"
      view.select("p:nth-child(5)").text() mustBe "notEligible.reference.text"
    }

    "display 'Back' button that links to 'Make declaration' page" in {
      val backButton = view.getElementById("link-back")

      backButton.text() mustBe "site.back"
      backButton must haveHref(controllers.routes.StartController.displayStartPage())
    }
  }
}
