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

package views.declaration.destinationCountries

import controllers.declaration.routes
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.DestinationCountryPage
import models.{DeclarationType, Mode}
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.destination_country

class DestinationCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestData {

  val form: Form[String] = DestinationCountries.form(DestinationCountryPage)

  val destinationCountryPage = new destination_country(mainTemplate)
  val view = destinationCountryPage(Mode.Normal, form)(journeyRequest(), messages)

  "Destination country view spec" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.destinationCountry.title")
      messages must haveTranslationFor("declaration.destinationCountry.heading")
      messages must haveTranslationFor("declaration.destinationCountry.question")
      messages must haveTranslationFor("declaration.destinationCountry.empty")
      messages must haveTranslationFor("declaration.destinationCountry.error")
    }

    "display page question" in {

      view.getElementById("title").text() mustBe messages("declaration.destinationCountry.question")
    }

    "display page heading" in {

      view.getElementById("section-header").text() must include(messages("declaration.destinationCountry.heading"))
    }

    "display back button that links to 'Origination country' page" when {

      "user is during Standard journey" in {

        val standardView = destinationCountryPage(Mode.Normal, form)(journeyRequest(DeclarationType.STANDARD), messages)
        val backButton = standardView.getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.OriginationCountryController.displayPage())
      }

      "user is during Supplementary journey" in {

        val supplementaryView = destinationCountryPage(Mode.Normal, form)(journeyRequest(DeclarationType.SUPPLEMENTARY), messages)
        val backButton = supplementaryView.getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.OriginationCountryController.displayPage())
      }
    }

    "display back button that links to `Declaration holder` page" when {

      "user is during Simplified journey" in {

        val simplifiedView = destinationCountryPage(Mode.Normal, form)(journeyRequest(DeclarationType.SIMPLIFIED), messages)
        val backButton = simplifiedView.getElementById("back-link")

        backButton.text() mustBe messages("site.back")
        backButton must haveHref(routes.DeclarationHolderController.displayPage())
      }
    }

    "display 'Save and continue' button" in {

      view.getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "display 'Save and return' button" in {

      view.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
    }
  }
}
