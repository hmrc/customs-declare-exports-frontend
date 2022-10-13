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
import controllers.declaration.routes.{BorderTransportController, DepartureTransportController}
import controllers.helpers.TransportSectionHelper.nonPostalOrFTIModeOfTransportCodes
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.InlandOrBorder.Border
import forms.declaration.ModeOfTransportCode.{Maritime, RoRo}
import forms.declaration.TransportCountry
import forms.declaration.TransportCountry._
import models.DeclarationType.{STANDARD, SUPPLEMENTARY}
import models.Mode
import models.Mode.Normal
import models.codes.Country
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.data.Form
import play.api.libs.json.Json
import views.declaration.spec.PageWithButtonsSpec
import views.helpers.ModeOfTransportCodeHelper
import views.html.declaration.transport_country
import views.tags.ViewTest

import scala.collection.immutable.ListMap

@ViewTest
class TransportCountryViewSpec extends PageWithButtonsSpec with Injector {

  implicit val codeListConnector = mock[CodeListConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(codeListConnector.getCountryCodes(any())).thenReturn(ListMap("GB" -> Country("United Kingdom", "GB")))
  }

  override protected def afterEach(): Unit = {
    reset(codeListConnector)
    super.afterEach()
  }

  def form(transportMode: String): Form[TransportCountry] = TransportCountry.form(transportMode)

  val page = instanceOf[transport_country]

  override val typeAndViewInstance = {
    val maritime = ModeOfTransportCodeHelper.transportMode(Some(Maritime))
    (STANDARD, page(Normal, maritime, form(maritime))(_, _))
  }

  def createView(form: Form[TransportCountry], transportMode: String, mode: Mode = Normal)(implicit request: JourneyRequest[_]): Document =
    page(mode, transportMode, form)(request, messages)

  "TransportCountry View" when {

    List(STANDARD, SUPPLEMENTARY).foreach { declarationType =>
      s"the declaration's type is $declarationType and" when {

        // When TransportLeavingTheBorder is 'Postal' or 'FTI' the user does not land on the /transport-country page
        nonPostalOrFTIModeOfTransportCodes.foreach { code =>
          val transportMode = ModeOfTransportCodeHelper.transportMode(Some(code))
          s"the transport mode is $transportMode" should {

            "contain the expected content" which {
              val view = createView(form(transportMode), transportMode)(journeyRequest(declarationType))

              "display 'Back' button that links to the 'Border Transport' page" in {
                val backButton = view.getElementById("back-link")
                backButton must containMessage("site.backToPreviousQuestion")
                backButton.getElementById("back-link") must haveHref(BorderTransportController.displayPage(Normal))
              }

              "display 'Back' button that links to the 'Departure Transport' page" when {
                "the user selects 'Border' on the /inland-or-border page" in {
                  implicit val request = withRequestOfType(declarationType, withInlandOrBorder(Some(Border)))
                  val view = createView(form(transportMode), transportMode)
                  val backButton = view.getElementById("back-link")
                  backButton must containMessage("site.backToPreviousQuestion")
                  backButton.getElementById("back-link") must haveHref(DepartureTransportController.displayPage(Normal))
                }
              }

              "display section header" in {
                view.getElementById("section-header") must containMessage("declaration.section.6")
              }

              "display page title" in {
                view.getElementsByTag("h1").text mustBe messages(s"$prefix.title", transportMode)
              }

              "display body text" when {
                "the transport mode is RoRo" in {
                  val body = view.getElementsByClass("govuk-body")

                  body.size mustBe (if (code == RoRo) 2 else 1)

                  val expectedText = messages(if (code == RoRo) s"$prefix.roro.body" else exitAndReturnCaption)
                  body.get(0).text mustBe expectedText
                }
              }

              "display radio button with Yes option" in {
                view.getElementById("code_yes").attr("value") mustBe YesNoAnswers.yes
                view.getElementsByAttributeValue("for", "code_yes") must containMessageForElements("site.yes")
              }

              "display radio button with No option" in {
                view.getElementById("code_no").attr("value") mustBe YesNoAnswers.no
                view.getElementsByAttributeValue("for", "code_no") must containMessageForElements("site.no")
              }

              "display the country's label when the user selects the 'Yes' radio" in {
                val label = view.getElementsByAttributeValue("for", transportCountry).get(0)
                label.text mustBe messages(s"$prefix.country.label", transportMode)
                label.tag.getName mustBe "label"
                label.id mustBe s"${transportCountry}-label"
              }

              val createViewWithMode: Mode => Document =
                mode => createView(form(transportMode), transportMode, mode = mode)(journeyRequest(declarationType))
              checkAllSaveButtonsAreDisplayed(createViewWithMode)
            }

            "display an error" when {

              "the user does not select any radio button" in {
                val formData = Json.obj(hasTransportCountry -> "", transportCountry -> "")
                val formWithError = form(transportMode).bind(formData, JsonBindMaxChars)
                val view = createView(formWithError, transportMode)(journeyRequest(declarationType))

                view must haveGovukGlobalErrorSummary
                view must containErrorElementWithTagAndHref("a", "#code_yes")
                view must containErrorElementWithMessage(messages(s"$prefix.error.empty", transportMode))
              }

              "the user selects the 'Yes' radio but does not enter a country" in {
                val formData = Json.obj(hasTransportCountry -> YesNoAnswers.yes, transportCountry -> "")
                val formWithError = form(transportMode).bind(formData, JsonBindMaxChars)
                val view = createView(formWithError, transportMode)(journeyRequest(declarationType))

                view must haveGovukGlobalErrorSummary
                view must containErrorElementWithTagAndHref("a", s"#$transportCountry")
                view must containErrorElementWithMessage(messages(s"$prefix.country.error.empty", transportMode))
              }

              "the user selects the 'Yes' radio but enters an invalid country" in {
                val formData = Json.obj(hasTransportCountry -> YesNoAnswers.yes, transportCountry -> "12345")
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
  }
}
