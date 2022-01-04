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

package views.declaration.destinationCountries

import base.Injector
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.common.YesNoAnswer.{No, Yes}
import forms.declaration.countries.Countries.DestinationCountryPage
import forms.declaration.countries.{Countries, Country}
import models.codes.{Country => ModelCountry}
import models.DeclarationType._
import models.Mode
import models.declaration.{DeclarationHoldersData, Parties}
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.twirl.api.Html
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.destination_country

import scala.collection.immutable.ListMap

class DestinationCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestData with Injector with BeforeAndAfterEach {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> ModelCountry("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  private val destinationCountryPage = instanceOf[destination_country]

  private def form(request: JourneyRequest[_]): Form[Country] =
    Countries.form(DestinationCountryPage)(request, messages(request), mockCodeListConnector)

  private def view(implicit request: JourneyRequest[_]): Html = destinationCountryPage(Mode.Normal, form(request))(request, messages)

  "Destination country view spec" should {

    "have defined translation for used labels" in {

      messages must haveTranslationFor("declaration.destinationCountry.title")
      messages must haveTranslationFor("declaration.destinationCountry.body")
      messages must haveTranslationFor("declaration.destinationCountry.empty")
      messages must haveTranslationFor("declaration.destinationCountry.error")
      messages must haveTranslationFor("declaration.section.3")
      messages must haveTranslationFor("site.back")
      messages must haveTranslationFor("site.save_and_continue")
      messages must haveTranslationFor("site.save_and_come_back_later")
    }
  }

  onEveryDeclarationJourney() { implicit request =>
    "Destination country view spec" should {

      s"display page question for ${request.declarationType}" in {
        view(request).getElementsByTag("h1").text mustBe messages("declaration.destinationCountry.title")
      }

      s"display page heading for ${request.declarationType}" in {
        view(request).getElementById("section-header").text must include(messages("declaration.section.3"))
      }

      "display the expected body text" in {
        view(request).getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.destinationCountry.body")
      }

      s"display 'Save and continue' button for ${request.declarationType}" in {
        view(request).getElementById("submit").text mustBe messages("site.save_and_continue")
      }

      s"display 'Save and return' button for ${request.declarationType}" in {
        view(request).getElementById("submit_and_return").text mustBe messages("site.save_and_come_back_later")
      }
    }
  }

  onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
    "Destination country view spec" should {

      s"links to 'Declaration Holder Summary' page for ${request.declarationType}" when {

        "cache contains DeclarationHoldersData.isRequired field with 'Yes' answer" in {
          val parties = Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq.empty, Yes)))
          val req = journeyRequest(request.cacheModel.copy(parties = parties))

          val backButton = view(req).getElementById("back-link")

          backButton.text mustBe messages("site.back")
          backButton must haveHref(routes.DeclarationHolderSummaryController.displayPage())
        }

        "cache does NOT contain DeclarationHoldersData.isRequired field" in {
          val backButton = view(request).getElementById("back-link")

          backButton.text mustBe messages("site.back")
          backButton must haveHref(routes.DeclarationHolderSummaryController.displayPage())
        }
      }

      s"links to 'Declaration Holder Required' page for ${request.declarationType}" when {

        "cache contains DeclarationHoldersData.isRequired field with 'No' answer" in {
          val parties = Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq.empty, No)))
          val req = journeyRequest(request.cacheModel.copy(parties = parties))

          val backButton = view(req).getElementById("back-link")

          backButton.text mustBe messages("site.back")
          backButton must haveHref(routes.DeclarationHolderRequiredController.displayPage())
        }
      }
    }
  }

  onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
    "Destination country view spec" should {

      "display back button" that {

        s"links to 'Declaration Holder Summary' page for ${request.declarationType}" when {

          "cache contains DeclarationHoldersData.isRequired field with 'Yes' answer" in {
            val parties = Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq.empty, Yes)))
            val req = journeyRequest(request.cacheModel.copy(parties = parties))

            val backButton = view(req).getElementById("back-link")

            backButton.text mustBe messages("site.back")
            backButton must haveHref(routes.DeclarationHolderSummaryController.displayPage())
          }

          "cache does NOT contain DeclarationHoldersData.isRequired field" in {
            val backButton = view(request).getElementById("back-link")

            backButton.text mustBe messages("site.back")
            backButton must haveHref(routes.DeclarationHolderSummaryController.displayPage())
          }
        }

        s"links to 'Declaration Holder Required' page for ${request.declarationType}" when {

          "cache contains DeclarationHoldersData.isRequired field with 'No' answer" in {
            val parties = Parties(declarationHoldersData = Some(DeclarationHoldersData(Seq.empty, No)))
            val req = journeyRequest(request.cacheModel.copy(parties = parties))

            val backButton = view(req).getElementById("back-link")

            backButton.text mustBe messages("site.back")
            backButton must haveHref(routes.DeclarationHolderRequiredController.displayPage())
          }
        }
      }
    }
  }
}
