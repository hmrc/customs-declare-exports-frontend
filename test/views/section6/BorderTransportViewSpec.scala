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

package views.section6

import base.Injector
import base.MockTransportCodeService
import config.AppConfig
import controllers.section6.routes.{DepartureTransportController, InlandTransportDetailsController}
import forms.section6.BorderTransport.form
import models.DeclarationType._
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.mvc.AnyContent
import services.TransportCodeService
import views.common.PageWithButtonsSpec
import views.html.section6.border_transport
import views.tags.ViewTest

@ViewTest
class BorderTransportViewSpec extends PageWithButtonsSpec with Injector {

  val prefix = "declaration.transportInformation.meansOfTransport.crossingTheBorder"

  implicit val transportCodeService: TransportCodeService = MockTransportCodeService.transportCodeService

  val page = instanceOf[border_transport]

  override val typeAndViewInstance = (STANDARD, page(form)(_, _))

  implicit val appConfig: AppConfig = mock[AppConfig]


  def createView()(implicit request: JourneyRequest[_]): Document = page(form)

  "Border Transport view" when {

    nonClearanceJourneys.foreach { declarationType =>
      s"DeclarationType is $declarationType and" when {

        implicit val request = withRequestOfType(declarationType)
        val view = createView()

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
            if(transportCode.value == "option_none"){
              Option(view.getElementById(s"radio_${transportCode.id}")) must be (None)
            } else{

            Option(view.getElementById(s"radio_${transportCode.id}")) must not be None

            val suffix = if (transportCode.useAltRadioTextForBorderTransport) ".vBT" else ""
            val radioLabel = view.getElementsByAttributeValue("for", s"radio_${transportCode.id}").text

            radioLabel mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}$suffix")

            Option(view.getElementById(s"${transportCode.id}")) must not be None

            val inputLabel = view.getElementsByAttributeValue("for", transportCode.id).text
            if(appConfig.isOptionalFieldsEnabled){
              inputLabel mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}.label.flag")
            }else inputLabel mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}.label")

            val inputHint = view.getElementById(s"${transportCode.id}-hint").text
            inputHint mustBe messages(s"declaration.transportInformation.meansOfTransport.${transportCode.id}.hint")
          }
        }

      }
        checkAllSaveButtonsAreDisplayed(createView())
      }
    }

    standardAndSupplementary.foreach { declarationType =>
      implicit val request: JourneyRequest[AnyContent] = withRequestOfType(declarationType)
      val view = createView()

      s"display a 'Back' button that links to the /departure-transport page for ${declarationType}" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(DepartureTransportController.displayPage)
      }
    }

    occasionalAndSimplified.foreach { declarationType =>
      implicit val request: JourneyRequest[AnyContent] = withRequestOfType(declarationType)
      val view = createView()

      s"display a 'Back' button that links to the /inland-transport-details page for ${declarationType}" in {
        val backButton = view.getElementById("back-link")
        backButton must containMessage(backToPreviousQuestionCaption)
        backButton must haveHref(InlandTransportDetailsController.displayPage)
      }
    }
  }
}
