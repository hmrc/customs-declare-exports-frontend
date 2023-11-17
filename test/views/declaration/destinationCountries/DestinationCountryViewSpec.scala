/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.declaration.routes.SectionSummaryController
import forms.common.Eori
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{STANDARD_FRONTIER, STANDARD_PRE_LODGED}
import forms.declaration.authorisationHolder.AuthorisationHolder
import forms.declaration.authorisationHolder.AuthorizationTypeCodes.CSE
import forms.declaration.countries.Countries.DestinationCountryPage
import forms.declaration.countries.{Countries, Country}
import models.DeclarationType._
import models.codes.{Country => ModelCountry}
import models.declaration.EoriSource
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.data.Form
import play.api.mvc.Call
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.destinationCountries.destination_country

import scala.collection.immutable.ListMap

class DestinationCountryViewSpec extends PageWithButtonsSpec with Injector {

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> ModelCountry("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  val page = instanceOf[destination_country]

  override val typeAndViewInstance = (STANDARD, page(form(request))(_, _))

  def form(request: JourneyRequest[_]): Form[Country] =
    Countries.form(DestinationCountryPage)(request, messages(request), mockCodeListConnector)

  def createView()(implicit request: JourneyRequest[_]): Document =
    page(form(request))(request, messages)

  "Destination country view spec" should {

    "have defined translation for used labels" in {
      messages must haveTranslationFor("declaration.destinationCountry.empty")
      messages must haveTranslationFor("declaration.destinationCountry.error")
    }

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { implicit request =>
      "display a back button linking to the /summary-section/2 page" in {
        verifyBackLink(SectionSummaryController.displayPage(2))
      }
    }

    "display a back button linking to the /summary-section/2 page" when {
      "AdditionalDeclarationType is 'STANDARD_PRE_LODGED' and" when {
        List(Choice1040, ChoiceOthers).foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '${choice.value}'" in {
            implicit val request = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(choice))
            verifyBackLink(SectionSummaryController.displayPage(2))
          }
        }
      }
    }

    "display a back button linking to the /summary-section/2 page" when {
      "AdditionalDeclarationType is 'STANDARD_FRONTIER' and" when {
        s"AuthorisationProcedureCodeChoice is 'Code1040'" in {
          implicit val request = withRequest(STANDARD_FRONTIER, withAuthorisationProcedureCodeChoice(Choice1040))
          verifyBackLink(SectionSummaryController.displayPage(2))
        }
      }
    }

    "display a back button linking to the /summary-section/2 page" when {
      "AdditionalDeclarationType is 'STANDARD_FRONTIER' and" when {
        s"AuthorisationProcedureCodeChoice is 'CodeOthers'" in {
          // However this should not be possible, since the user should always be forced to enter at least one auth.
          implicit val request = withRequest(STANDARD_FRONTIER, withAuthorisationProcedureCodeChoice(ChoiceOthers))
          verifyBackLink(SectionSummaryController.displayPage(2))
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL) { implicit request =>
      "display a back button linking to the /summary-section/2  page" in {
        verifyBackLink(SectionSummaryController.displayPage(2))
      }
    }

    "display a back button linking to the /summary-section/2 page" when {
      val holder = AuthorisationHolder(Some(CSE), Some(Eori(ExportsTestData.eori)), Some(EoriSource.OtherEori))

      allDeclarationTypes.foreach { declarationType =>
        s"on $declarationType journey and" when {
          "the declaration contains at least one authorisation" in {
            implicit val request = withRequestOfType(declarationType, withAuthorisationHolders(holder))
            verifyBackLink(SectionSummaryController.displayPage(2))
          }
        }
      }
    }

    def verifyBackLink(call: Call)(implicit request: JourneyRequest[_]): Unit = {
      val backButton = createView().getElementById("back-link")
      backButton must containMessage(backCaption)
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

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }
}
