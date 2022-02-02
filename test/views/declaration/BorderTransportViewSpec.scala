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

import base.Injector
import connectors.CodeListConnector
import controllers.declaration.routes.DepartureTransportController
import forms.declaration.BorderTransport
import forms.declaration.TransportCodes.transportCodesOnBorderTransport
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.Mode.Normal
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import services.cache.ExportsTestData
import tools.Stubs
import views.declaration.spec.UnitViewSpec
import views.helpers.CommonMessages
import views.html.declaration.border_transport
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class BorderTransportViewSpec extends UnitViewSpec with ExportsTestData with Stubs with Injector with CommonMessages with BeforeAndAfterEach {

  private val page = instanceOf[border_transport]

  implicit val mockCodeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(mockCodeListConnector)
    super.afterEach()
  }

  def borderView(view: Document): Unit = {

    "display page title" in {
      view.getElementsByTag("h1").text mustBe messages("declaration.transportInformation.meansOfTransport.crossingTheBorder.title")
    }

    "display section header" in {
      view.getElementById("section-header") must containMessage("declaration.section.6")
    }

    "display 'Save and continue' button on page" in {
      view.getElementById("submit") must containMessage(saveAndContinueCaption)
    }

    "display 'Save and return' button on page" in {
      view.getElementById("submit_and_return") must containMessage(saveAndReturnCaption)
    }
  }

  val havingMeansOfTransport: Document => Unit = (view: Document) => {

    "display 'Means of Transport' section" which {

      "has body" in {
        view
          .getElementById("borderTransportType-fieldSet")
          .getElementsByClass("govuk-fieldset__legend")
          .text mustBe messages("declaration.transportInformation.meansOfTransport.crossingTheBorder.body")
      }

      transportCodesOnBorderTransport.foreach { transportCode =>
        s"has '${transportCode.id}' section" in {
          Option(view.getElementById(s"radio_${transportCode.id}")) must not be None

          val suffix = if (transportCode.useAltRadioTextForBorderTransport) ".vBT" else ""
          val radioLabel = view.getElementsByAttributeValue("for", s"radio_${transportCode.id}").text
          radioLabel mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}$suffix")

          Option(view.getElementById(s"${transportCode.id}")) must not be None

          val inputLabel = view.getElementsByAttributeValue("for", transportCode.id).text
          inputLabel mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}.label")

          val inputHint = view.getElementById(s"${transportCode.id}-hint").text
          inputHint mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}.hint")
        }
      }

      "has nationality picker" in {
        val nationality = view.getElementById("borderTransportNationality-label").text
        nationality mustBe messages("declaration.transportInformation.meansOfTransport.crossingTheBorder.nationality.header")
      }
    }
  }

  private def createView(implicit request: JourneyRequest[_]): Document =
    page(Normal, BorderTransport.form)

  "TransportDetails View" must {

    onJourney(STANDARD, SUPPLEMENTARY) { implicit request =>
      val view = createView
      "display 'Back' button that links to 'Departure' page" in {
        val backButton = view.getElementById("back-link")

        backButton must containMessage(backCaption)
        backButton.getElementById("back-link") must haveHref(DepartureTransportController.displayPage(Normal))
      }

      behave like borderView(view)
      behave like havingMeansOfTransport(view)
    }
  }
}
