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
import connectors.CodeListConnector
import controllers.helpers.TransportSectionHelper.nonPostalOrFTIModeOfTransportCodes
import controllers.section6.routes._
import forms.section6.InlandOrBorder.Border
import forms.section6.ModeOfTransportCode.{Maritime, Rail, RoRo}
import forms.section6.TransportCountry
import forms.section6.TransportCountry._
import models.DeclarationType._
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.data.Form
import play.api.libs.json.Json
import views.helpers.ModeOfTransportCodeHelper
import views.html.section6.transport_country
import views.common.PageWithButtonsSpec
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class TransportCountryViewSpec extends PageWithButtonsSpec with Injector {

  implicit val codeListConnector: CodeListConnector = mock[CodeListConnector]

  val page = instanceOf[transport_country]

  val countryCode = "ZA"
  val countryName = "South Africa"

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(codeListConnector.getCountryCodes(any())).thenReturn(ListMap("ZA" -> Country("South Africa", "ZA")))
  }

  override protected def afterEach(): Unit = {
    reset(codeListConnector)
    super.afterEach()
  }

  def form(transportMode: String): Form[TransportCountry] = TransportCountry.form(transportMode)

  override val typeAndViewInstance = {
    val maritime = ModeOfTransportCodeHelper.transportMode(Some(Maritime))
    (STANDARD, page(maritime, form(maritime))(_, _))
  }

  def createView(form: Form[TransportCountry], transportMode: String)(implicit request: JourneyRequest[_]): Document =
    page(transportMode, form)(request, messages)

  // When TransportLeavingTheBorder or InlandModeOfTransportCode are 'Postal' or 'FTI' the page is skipped.
  // When TransportLeavingTheBorder is 'Rail' the page is also skipped, but only for STANDARD and SUPPLEMENTARY.
  // In any case, leaving or removing these 3 ModeOfTransport codes in/from the declaration does not affect the
  // test, as the page's content does not depend on these values.

  "TransportCountry View" when {

    nonClearanceJourneys.foreach { declarationType =>
      s"the declaration's type is $declarationType and" when {

        nonPostalOrFTIModeOfTransportCodes.filterNot(_ == Rail).foreach { code =>
          val transportMode = ModeOfTransportCodeHelper.transportMode(Some(code))
          s"the transport mode is $transportMode" should {

            "contain the expected content" which {
              val view = createView(form(transportMode), transportMode)(journeyRequest(declarationType))

              "display 'Back' button that links to the 'Border Transport' page" in {
                val backButton = view.getElementById("back-link")
                backButton must containMessage("site.backToPreviousQuestion")
                backButton.getElementById("back-link") must haveHref(BorderTransportController.displayPage)
              }

              "display section header" in {
                view.getElementById("section-header") must containMessage("declaration.section.6")
              }

              "display page title" in {
                view.getElementsByTag("h1").text mustBe messages(s"$prefix.title", transportMode)
              }

              "display the expected paragraph" when {
                "'Transport Leaving the Border' is 'RoRo'" in {
                  val body = view.getElementsByClass("govuk-body")

                  body.size mustBe (if (code == RoRo) 2 else 1)

                  val expectedText = messages(if (code == RoRo) s"$prefix.roro.paragraph" else exitAndReturnCaption)
                  body.get(0).text mustBe expectedText
                }
              }

              "display the expected hint" in {
                val hint = view.getElementsByClass("govuk-hint")
                hint.get(0).text mustBe messages("declaration.country.dropdown.hint.noJs")
              }

              "display the expected input field" in {
                val input = view.getElementById(transportCountry)
                input.tagName mustBe "select"
              }

              checkAllSaveButtonsAreDisplayed(view)
            }

            "display an error" when {

              "the user does not enter a country" in {
                val formData = Json.obj(transportCountry -> "")
                val formWithError = form(transportMode).bind(formData, JsonBindMaxChars)
                val view = createView(formWithError, transportMode)(journeyRequest(declarationType))

                view must haveGovukGlobalErrorSummary
                view must containErrorElementWithTagAndHref("a", s"#$transportCountry")
                view must containErrorElementWithMessage(messages(s"$prefix.country.error.empty", transportMode))
              }

              "the user enters an invalid country" in {
                val formData = Json.obj(transportCountry -> "12345")
                val formWithError = form(transportMode).bind(formData, JsonBindMaxChars)
                val view = createView(formWithError, transportMode)(journeyRequest(declarationType))

                view must haveGovukGlobalErrorSummary
                view must containErrorElementWithTagAndHref("a", s"#$transportCountry")
                view must containErrorElementWithMessageKey(s"$prefix.country.error.invalid")
              }
            }
          }
        }
      }
    }

    standardAndSupplementary.foreach { declarationType =>
      s"the declaration's type is $declarationType and" when {

        nonPostalOrFTIModeOfTransportCodes.filterNot(_ == Rail).foreach { code =>
          val transportMode = ModeOfTransportCodeHelper.transportMode(Some(code))
          s"the transport mode is $transportMode" should {

            "display 'Back' button that links to the 'Departure Transport' page" when {
              "the user selects 'Border' on the /inland-or-border page" in {
                implicit val request = withRequestOfType(declarationType, withInlandOrBorder(Some(Border)))
                val view = createView(form(transportMode), transportMode)
                val backButton = view.getElementById("back-link")
                backButton must containMessage("site.backToPreviousQuestion")
                backButton.getElementById("back-link") must haveHref(DepartureTransportController.displayPage)
              }
            }
          }
        }
      }
    }

    occasionalAndSimplified.foreach { declarationType =>
      s"the declaration's type is $declarationType and" when {

        nonPostalOrFTIModeOfTransportCodes.foreach { code =>
          val transportMode = ModeOfTransportCodeHelper.transportMode(Some(code))
          s"the transport mode is $transportMode" should {

            "display 'Back' button that links to the 'Inland or Border' page" when {
              "the user selects 'Border' on the /inland-or-border page" in {
                implicit val request = withRequestOfType(declarationType, withInlandOrBorder(Some(Border)))
                val view = createView(form(transportMode), transportMode)
                val backButton = view.getElementById("back-link")
                backButton must containMessage("site.backToPreviousQuestion")
                backButton.getElementById("back-link") must haveHref(InlandOrBorderController.displayPage)
              }
            }
          }
        }
      }
    }
  }
}
