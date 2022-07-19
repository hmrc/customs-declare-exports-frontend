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

import base.{ExportsTestData, Injector}
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.common.Eori
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{STANDARD_FRONTIER, STANDARD_PRE_LODGED}
import forms.declaration.countries.Countries.DestinationCountryPage
import forms.declaration.countries.{Countries, Country}
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType._
import models.Mode
import models.Mode.Normal
import models.codes.{Country => ModelCountry}
import models.declaration.EoriSource
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.Html
import services.cache.ExportsTestHelper
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.html.declaration.destinationCountries.destination_country

import scala.collection.immutable.ListMap

class DestinationCountryViewSpec extends UnitViewSpec with Stubs with ExportsTestHelper with Injector with BeforeAndAfterEach {

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

  private def createView(mode: Mode = Mode.Normal)(implicit request: JourneyRequest[_]): Html =
    destinationCountryPage(mode, form(request))(request, messages)

  "Destination country view spec" should {

    "have defined translation for used labels" in {
      messages must haveTranslationFor("declaration.destinationCountry.empty")
      messages must haveTranslationFor("declaration.destinationCountry.error")
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { implicit request =>
      "display a back button linking to the /authorisation-choice page" in {
        verifyBackLink(routes.AuthorisationProcedureCodeChoiceController.displayPage(Normal))
      }
    }

    "display a back button linking to the /is-authorisation-required page" when {
      "AdditionalDeclarationType is 'STANDARD_PRE_LODGED' and" when {
        List(Choice1040, ChoiceOthers).foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '${choice.value}'" in {
            implicit val request = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(choice))
            verifyBackLink(routes.DeclarationHolderRequiredController.displayPage())
          }
        }
      }
    }

    "display a back button linking to the /is-authorisation-required page" when {
      "AdditionalDeclarationType is 'STANDARD_FRONTIER' and" when {
        s"AuthorisationProcedureCodeChoice is 'Code1040'" in {
          implicit val request = withRequest(STANDARD_FRONTIER, withAuthorisationProcedureCodeChoice(Choice1040))
          verifyBackLink(routes.DeclarationHolderRequiredController.displayPage())
        }
      }
    }

    "display a back button linking to the /authorisation-choice page" when {
      "AdditionalDeclarationType is 'STANDARD_FRONTIER' and" when {
        s"AuthorisationProcedureCodeChoice is 'CodeOthers'" in {
          // However this should not be possible, since the user should always be forced to enter at least one auth.
          implicit val request = withRequest(STANDARD_FRONTIER, withAuthorisationProcedureCodeChoice(ChoiceOthers))
          verifyBackLink(routes.AuthorisationProcedureCodeChoiceController.displayPage())
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL) { implicit request =>
      "display a back button linking to the /is-authorisation-required  page" in {
        verifyBackLink(routes.DeclarationHolderRequiredController.displayPage(Normal))
      }
    }

    "display a back button linking to the /authorisations-required page" when {
      val holder = DeclarationHolder(Some("CSE"), Some(Eori(ExportsTestData.eori)), Some(EoriSource.OtherEori))

      allDeclarationTypes.foreach { declarationType =>
        s"on $declarationType journey and" when {
          "the declaration contains at least one authorisation" in {
            implicit val request = withRequestOfType(declarationType, withDeclarationHolders(holder))
            verifyBackLink(routes.DeclarationHolderSummaryController.displayPage())
          }
        }
      }
    }

    def verifyBackLink(call: Call)(implicit request: JourneyRequest[_]): Unit = {
      val backButton = createView().getElementById("back-link")
      backButton.text mustBe messages("site.back")
      backButton must haveHref(call)
    }
  }

  onEveryDeclarationJourney() { implicit request =>
    "Destination country view spec" should {

      s"display page question for ${request.declarationType}" in {
        createView()(request).getElementsByTag("h1").text mustBe messages("declaration.destinationCountry.title")
      }

      s"display page heading for ${request.declarationType}" in {
        createView()(request).getElementById("section-header").text must include(messages("declaration.section.3"))
      }

      "display the expected body text" in {
        createView()(request).getElementsByClass("govuk-body").get(0).text mustBe messages("declaration.destinationCountry.body")
      }

      val createViewWithMode: Mode => Document = mode => createView(mode = mode)
      checkAllSaveButtonsAreDisplayed(createViewWithMode)
    }
  }
}
