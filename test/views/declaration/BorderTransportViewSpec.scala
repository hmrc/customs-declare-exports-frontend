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

package views.declaration

import base.{Injector, MockTransportCodeService}
import controllers.declaration.routes.DepartureTransportController
import forms.declaration.BorderTransport.form
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import models.DeclarationType.STANDARD
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import views.declaration.spec.PageWithButtonsSpec
import views.html.declaration.border_transport
import views.tags.ViewTest

@ViewTest
class BorderTransportViewSpec extends PageWithButtonsSpec with Injector {

  val prefix = "declaration.transportInformation.meansOfTransport.crossingTheBorder"

  implicit val transportCodeService = MockTransportCodeService.transportCodeService

  val page = instanceOf[border_transport]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  def createView()(implicit request: JourneyRequest[_]): Document = page(form)

  "Border Transport view" when {

    List(STANDARD_FRONTIER, STANDARD_PRE_LODGED, SUPPLEMENTARY_SIMPLIFIED, SUPPLEMENTARY_EIDR).foreach { additionalType =>
      s"AdditionalDeclarationType is $additionalType and" when {

        implicit val request = withRequest(additionalType)
        val view = createView()

        "display a 'Back' button that links to the /departure-transport page" in {
          val backButton = view.getElementById("back-link")
          backButton must containMessage(backToPreviousQuestionCaption)
          backButton must haveHref(DepartureTransportController.displayPage)
        }

        "display the expected section header" in {
          view.getElementById("section-header") must containMessage("declaration.section.6")
        }

        "display same page title as header" in {
          view.title must include(view.getElementsByTag("h1").text)
        }

        "display the expected page title" in {
          val unknownTransportMode = messages("declaration.transport.leavingTheBorder.transportMode.unknown").toLowerCase
          view.getElementsByTag("h1").text mustBe messages(s"$prefix.title", unknownTransportMode)
        }

        "display the expected body" in {
          view.getElementsByClass("govuk-body").first.text mustBe messages(s"$prefix.body")
        }

        "display the expected 'Means of Transport' section" in {
          transportCodeService.transportCodesOnBorderTransport.foreach { transportCode =>
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

        "display the expected tariff details" in {
          val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
          tariffTitle.text mustBe messages(s"tariff.expander.title.common")

          val tariffDetails = view.getElementsByClass("govuk-details__text").first

          val prefix = "tariff.declaration.borderTransport"
          val expectedText = messages(s"$prefix.common.text", messages(s"$prefix.common.linkText.0"))
          val actualText = removeBlanksIfAnyBeforeDot(tariffDetails.text)
          actualText mustBe removeLineBreakIfAny(expectedText)
        }

        checkAllSaveButtonsAreDisplayed(createView())
      }
    }
  }
}
