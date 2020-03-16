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

import base.Injector
import play.api.i18n.MessagesApi
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.not_eligible
import views.tags.ViewTest

@ViewTest
class NotEligibleViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector {
  private val page = instanceOf[not_eligible]

  private val validatedMsg = validatedMessages(request)
  private val view = page()(request, validatedMsg)

  "Not Eligible View on empty page" should {
    "have proper messages for labels" in {
      val messages = instanceOf[MessagesApi].preferred(journeyRequest())
      messages must haveTranslationFor("declaration.natureOfTransaction.title")
      messages must haveTranslationFor("notEligible.title")
      messages must haveTranslationFor("notEligible.titleLineTwo")
      messages must haveTranslationFor("notEligible.description")
      messages must haveTranslationFor("notEligible.descriptionLink")
      messages must haveTranslationFor("notEligible.referenceTitle")
      messages must haveTranslationFor("notEligible.reference.support")
      messages must haveTranslationFor("notEligible.reference.openingHours")
    }

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display header" in {
      view.select("h1").text() mustBe validatedMsg("notEligible.title")
    }

    "display help information" in {
      view.getElementsByClass("govuk-body").get(0).text() mustBe validatedMsg("notEligible.description", validatedMsg("notEligible.descriptionLink"))
    }

    "display read more link" in {
      view
        .getElementsByClass("govuk-body")
        .first()
        .getElementsByTag("a")
        .attr("href") mustBe "https://www.gov.uk/guidance/dispatching-your-goods-within-the-eu"
    }

    "display Help and Support with description" in {
      view.select("h2").first().text() mustBe validatedMsg("notEligible.referenceTitle")
      view.getElementsByClass("govuk-body").get(1).text() must include(validatedMsg("notEligible.reference.support", "0300 200 3700"))
    }

    "display 'Back' button that links to 'Make declaration' page" in {
      val backButton = view.getElementById("back-link")

      backButton.text() mustBe validatedMsg("site.back")
      backButton must haveHref(controllers.routes.StartController.displayStartPage())
    }
  }
}
