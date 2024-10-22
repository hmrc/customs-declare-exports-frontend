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

package views.section3

import base.Injector
import connectors.CodeListConnector
import controllers.section3.routes.DestinationCountryController
import forms.common.Country
import forms.section3.RoutingCountryQuestionYesNo.formAdd
import models.DeclarationType.STANDARD
import models.codes.{Country => ModelCountry}
import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat.Appendable
import views.common.PageWithButtonsSpec
import views.html.section3.routing_country_question
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

    "display back button that links to 'Declaration Holder' page" in {
      val backButton = view.getElementById("back-link")

      backButton.text mustBe messages("site.backToPreviousQuestion")
      backButton must haveHref(DestinationCountryController.displayPage)
    }

    checkAllSaveButtonsAreDisplayed(createView())
  }
}
