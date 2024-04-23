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

import base.ExportsTestData.itemWithPC
import base.{Injector, MockTransportCodeService}
import controllers.declaration.routes.{InlandOrBorderController, InlandTransportDetailsController, SupervisingCustomsOfficeController}
import controllers.helpers.TransportSectionHelper._
import forms.declaration.DepartureTransport.form
import forms.declaration.InlandOrBorder.{Border, Inland}
import forms.declaration.ModeOfTransportCode.{RoRo, Road}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.{ModeOfTransportCode, TransportCodes}
import models.DeclarationType.{DeclarationType, STANDARD}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat.Appendable
import services.TransportCodeService
import views.declaration.spec.UnitViewSpec
import views.helpers.ModeOfTransportCodeHelper
import views.html.declaration.departure_transport
import views.tags.ViewTest

import scala.jdk.CollectionConverters.IteratorHasAsScala

@ViewTest
class DepartureTransportViewSpec extends UnitViewSpec with Injector {

  private val prefix = "declaration.transportInformation.meansOfTransport"

  val page = instanceOf[departure_transport]

  implicit val transportCodeService: TransportCodeService = MockTransportCodeService.transportCodeService

  val typeAndViewInstance: (DeclarationType, (JourneyRequest[_], Messages) => Appendable) =
    (STANDARD, page(form(transportCodeService.transportCodesForV1))(_, _))

  def createView(transportCodes: TransportCodes = transportCodeService.transportCodesForV1)(implicit request: JourneyRequest[_]): Document =
    page(form(transportCodes))(request, messages)

  override def checkAllSaveButtonsAreDisplayed(view: Document): Unit = checkSaveAndContinueButtonIsDisplayed(view)

  "Departure Transport View" should {

    onEveryDeclarationJourney() { implicit request =>
      val view = createView()

      "display the expected section header" in {
        view.getElementById("section-header") must containMessage("declaration.section.6")
      }

      "display the expected tariff details" in {
        val expectedKey = "common"

        val tariffTitle = view.getElementsByClass("govuk-details__summary-text")
        tariffTitle.text mustBe messages(s"tariff.expander.title.$expectedKey")

        val tariffDetails = view.getElementsByClass("govuk-details__text").first.text

        val expectedText =
          removeLineBreakIfAny(messages(s"tariff.declaration.text", messages(s"tariff.declaration.departureTransport.$expectedKey.linkText.0")))
        removeBlanksIfAnyBeforeDot(tariffDetails) mustBe expectedText
      }

      checkAllSaveButtonsAreDisplayed(createView())
    }
  }

  "Departure Transport View" should {

    "display the expected page title" in {
      List(dataForVersion1, dataForVersion2, dataForVersion3).zipWithIndex.foreach { dataForVersion =>
        dataForVersion._1.foreach { dataOnTest =>
          val view = createView(dataOnTest.transportCodes)(dataOnTest.request)
          view.getElementById("title").text mustBe messages(
            s"$prefix.departure.title.v${dataForVersion._2 + 1}",
            ModeOfTransportCodeHelper.transportMode(Some(dataOnTest.transportCode))
          )
        }
      }
    }

    "display the body expected for versions 1 and 3" in {
      List(dataForVersion1, dataForVersion3).foreach { dataForVersion =>
        dataForVersion.foreach { dataOnTest =>
          val view = createView(dataOnTest.transportCodes)(dataOnTest.request)
          val elements = view.getElementsByClass("govuk-body")
          elements.size mustBe 2
          elements.get(0).text mustBe messages(s"$prefix.departure.body")
        }
      }
    }

    "display the expected body for version 2" in {
      dataForVersion2.foreach { dataOnTest =>
        val view = createView(dataOnTest.transportCodes)(dataOnTest.request)
        val elements = view.getElementsByClass("govuk-body")
        if (dataOnTest.transportCode == Road) {
          elements.size mustBe 3
          elements.get(0).text mustBe messages(s"$prefix.departure.body.v2")
          elements.get(1).text mustBe messages(s"$prefix.departure.body")
        } else {
          elements.size mustBe 2
          elements.get(0).text mustBe messages(s"$prefix.departure.body")
        }
      }
    }

    "display the expected inset text for version 3" in {
      dataForVersion3.foreach { dataOnTest =>
        val view = createView(dataOnTest.transportCodes)(dataOnTest.request)
        val elements = view.getElementsByClass("govuk-inset-text")
        elements.size mustBe 1
        elements.text mustBe messages(s"$prefix.departure.inset.text.v3")
      }
    }

    "display the expected sequence of radio buttons and input fields" in {
      List(dataForVersion1, dataForVersion2, dataForVersion3).foreach { dataForVersion =>
        dataForVersion.foreach { dataOnTest =>
          val view = createView(dataOnTest.transportCodes)(dataOnTest.request)

          val transportCodes = dataOnTest.transportCodes
          val isV2 = transportCodes == transportCodeService.transportCodesForV2
          val notAvailableRadioIsNotIncluded = transportCodes != transportCodeService.transportCodesForV3WhenPC0019

          val radios = view.getElementsByClass("govuk-radios__input")
          radios.size mustBe transportCodes.asList.size
          radios.iterator.asScala.zipWithIndex.foreach { elementAndIndex =>
            val (element, index) = elementAndIndex
            val transportCode = transportCodes.asList(index)
            element.id mustBe s"radio_${transportCode.id}"
            element.attr("value") mustBe transportCode.value
          }

          val radioLabels = view.getElementsByClass("govuk-radios__label")
          radioLabels.size mustBe transportCodes.asList.size
          radioLabels.iterator.asScala.zipWithIndex.foreach { elementAndIndex =>
            val (element, index) = elementAndIndex
            val transportCode = transportCodes.asList(index)
            val suffix = if (isV2 && transportCode.useAltRadioTextForV2) ".v2" else ""
            element.text mustBe messages(s"$prefix.${transportCode.id}$suffix")
            element.attr("for") mustBe s"radio_${transportCode.id}"
          }

          if (notAvailableRadioIsNotIncluded) {
            // Page does not include the radio "Not available", which has no input field
            val inputs = view.getElementsByClass("govuk-input")
            inputs.size mustBe transportCodes.asList.size
            inputs.iterator.asScala.zipWithIndex.foreach { elementAndIndex =>
              val (element, index) = elementAndIndex
              element.id mustBe transportCodes.asList(index).id
            }

            val radios = view.getElementsByClass("govuk-label").iterator.asScala
            val inputLabels = radios.filterNot(_.hasClass("govuk-radios__label")).toList

            inputLabels.size mustBe transportCodes.asList.size
            inputLabels.zipWithIndex.foreach { elementAndIndex =>
              val (element, index) = elementAndIndex
              val transportCode = transportCodes.asList(index)
              element.attr("for") mustBe transportCode.id
              element.text mustBe messages(s"$prefix.${transportCodes.asList(index).id}.label")
            }

            val hints = view.getElementsByClass("govuk-hint")
            hints.size mustBe transportCodes.asList.size
            hints.iterator.asScala.zipWithIndex.foreach { elementAndIndex =>
              val (element, index) = elementAndIndex
              element.id mustBe s"${transportCodes.asList(index).id}-hint"
              element.text mustBe messages(s"$prefix.${transportCodes.asList(index).id}.hint")
            }
          }
        }
      }
    }
  }

  "Departure Transport View" when {
    additionalDeclTypesAllowedOnInlandOrBorder.foreach { additionalType =>
      "'Border' was selected on /inland-or-border and" when {
        implicit val request = withRequest(additionalType, withInlandOrBorder(Some(Border)))
        verifyBackButton(InlandOrBorderController.displayPage)
      }

      "'Inland' was selected on /inland-or-border and" when {
        implicit val request = withRequest(additionalType, withInlandOrBorder(Some(Inland)))
        verifyBackButton(InlandTransportDetailsController.displayPage)
      }

      "/inland-or-border was skipped and" when {
        verifyBackButton(InlandTransportDetailsController.displayPage)(withRequest(additionalType))
      }
    }

    "the declarationType is SUPPLEMENTARY and" when {
      // On this condition, InlandOrBorder's value, if any, is NOT taken into account
      implicit val request = withRequest(SUPPLEMENTARY_EIDR, withInlandOrBorder(Some(Border)))
      verifyBackButton(InlandTransportDetailsController.displayPage)
    }

    "the declaration is CLEARANCE and" when {
      List(CLEARANCE_FRONTIER, CLEARANCE_PRE_LODGED).foreach { additionalType =>
        verifyBackButton(SupervisingCustomsOfficeController.displayPage)(withRequest(additionalType))
      }
    }

    def verifyBackButton(call: Call)(implicit request: JourneyRequest[_]): Unit =
      s"AdditionalDeclarationType is ${request.cacheModel.additionalDeclarationType}" should {
        s"display 'Back' button that links to the '${call.url}' page" in {
          val backButton = createView().getElementById("back-link")
          backButton must containMessage(backToPreviousQuestionCaption)
          backButton must haveHref(call)
        }
      }
  }

  case class DataOnTest(transportCodes: TransportCodes, transportCode: ModeOfTransportCode, request: JourneyRequest[_])

  val nonRoRoOrPostalOrFTIModeOfTransportCodes = nonPostalOrFTIModeOfTransportCodes.filterNot(_ == RoRo)

  val dataForVersion1 =
    additionalDeclTypesAllowedOnInlandOrBorder.flatMap { additionalType =>
      nonRoRoOrPostalOrFTIModeOfTransportCodes.map { transportCode: ModeOfTransportCode =>
        DataOnTest(
          transportCodeService.transportCodesForV1,
          transportCode,
          withRequest(additionalType, withTransportLeavingTheBorder(Some(transportCode)), withInlandOrBorder(Some(Border)))
        )
      }
    }

  val dataForVersion2 =
    additionalDeclTypesAllowedOnInlandOrBorder.flatMap { additionalType =>
      nonPostalOrFTIModeOfTransportCodes.flatMap { transportCode =>
        val tcsForV2 = transportCodeService.transportCodesForV2
        List(
          DataOnTest(tcsForV2, transportCode, withRequest(additionalType, withInlandModeOfTransportCode(transportCode))),
          DataOnTest(tcsForV2, transportCode, withRequest(SUPPLEMENTARY_EIDR, withInlandModeOfTransportCode(transportCode)))
        )
      }
    }

  val dataForVersion3 =
    List(CLEARANCE_FRONTIER, CLEARANCE_PRE_LODGED).flatMap { additionalType =>
      nonRoRoOrPostalOrFTIModeOfTransportCodes.flatMap { transportCode =>
        List(
          DataOnTest(
            transportCodeService.transportCodesForV3,
            transportCode,
            withRequest(additionalType, withTransportLeavingTheBorder(Some(transportCode)))
          ),
          DataOnTest(
            transportCodeService.transportCodesForV3WhenPC0019,
            transportCode,
            withRequest(additionalType, withTransportLeavingTheBorder(Some(transportCode)), withItem(itemWithPC("0019")))
          )
        )
      }
    }
}
