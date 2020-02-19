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

import base.Injector
import controllers.declaration.routes
import forms.declaration.destinationCountries.DestinationCountries
import forms.declaration.destinationCountries.DestinationCountries.OriginationCountryPage
import models.Mode
import play.api.data.Form
import services.cache.ExportsTestData
import unit.tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.origination_country
import config.AppConfig

class OriginationCountryViewSpec extends UnitViewSpec with Injector with Stubs with ExportsTestData {
  private val appConfig = instanceOf[AppConfig]

  val form: Form[String] = DestinationCountries.form(OriginationCountryPage)

  val originationCountryPage = new origination_country(mainTemplate, appConfig)
  val view = originationCountryPage(Mode.Normal, form)(journeyRequest(), messages)

  "Origination country view spec" should {

    "have defined translation for used labels" in {

      val messages = realMessagesApi.preferred(request)
      messages must haveTranslationFor("declaration.originationCountry.title")
      messages must haveTranslationFor("declaration.originationCountry.heading")
      messages must haveTranslationFor("declaration.originationCountry.question")
      messages must haveTranslationFor("declaration.originationCountry.empty")
      messages must haveTranslationFor("declaration.originationCountry.error")
    }

    "display page question" in {

      view.getElementById("title").text() mustBe messages("declaration.originationCountry.question")
    }

    "display page heading" in {

      view.getElementById("section-header").text() must include(messages("declaration.originationCountry.heading"))
    }

    "display back button that links to 'Declaration Holder' page" in {

      val backButton = view.getElementById("back-link")

      backButton.text() mustBe messages("site.back")
      backButton must haveHref(routes.DeclarationHolderController.displayPage())
    }

    "display 'Save and continue' button" in {

      view.getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "display 'Save and return' button" in {

      view.getElementById("submit_and_return").text() mustBe messages("site.save_and_come_back_later")
    }
  }
}
