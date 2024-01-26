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

import base.Injector
import connectors.CodeListConnector
import controllers.declaration.routes
import forms.declaration.RoutingCountryQuestionYesNo.formAdd
import forms.declaration.countries.Country
import models.DeclarationType.STANDARD
import models.codes.{Country => ModelCountry}
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat.Appendable
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.destinationCountries.routing_country_question
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class RoutingCountryQuestionViewSpec extends PageWithButtonsSpec with Injector {

  implicit val mockCodeListConnector: CodeListConnector = mock[CodeListConnector]

  val expectedCountryName = "Mauritius"

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("MU" -> ModelCountry(expectedCountryName, "MU")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  val page = instanceOf[routing_country_question]

  override val typeAndViewInstance = (STANDARD, page(formAdd())(_, _))

  def createView()(implicit request: JourneyRequest[AnyContent]): Appendable =
    page(formAdd())(request, messages(request))

  "Routing country question page" should {
    val view = createView()

    "have defined translation for used labels" in {
      messages must haveTranslationFor("declaration.routingCountryQuestion.title")
      messages must haveTranslationFor("declaration.routingCountryQuestion.paragraph")
      messages must haveTranslationFor("declaration.routingCountryQuestion.empty")
      messages must haveTranslationFor("tariff.expander.title.clearance")
    }

    "display the section header" in {
      view.getElementById("section-header").text must include(messages("declaration.section.3"))
    }

    "display the expected page title" in {
      val view = createView()(journeyRequest(aDeclaration(withDestinationCountry(Country(Some("MU"))))))
      view.getElementsByTag("h1").text mustBe messages("declaration.routingCountryQuestion.title", expectedCountryName)
    }

    "display Yes/No answers" in {
      view.getElementsByAttributeValue("for", "Yes").text.text() mustBe messages("site.yes")
      view.getElementsByAttributeValue("for", "No").text mustBe messages("site.no")
    }

    "display Tariff section text" in {
      val tariffText = view.getElementsByClass("govuk-details__summary-text").first.text
      tariffText.text() mustBe messages("tariff.expander.title.common")
    }

    "display back button that links to 'Declaration Holder' page" in {
      val backButton = view.getElementById("back-link")

      backButton.text mustBe messages("site.backToPreviousQuestion")
      backButton must haveHref(routes.DestinationCountryController.displayPage)
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }
}
