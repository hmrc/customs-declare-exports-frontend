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

package controllers.declaration

import base.{AuditedControllerSpec, ControllerSpec, MockTaggedCodes}
import connectors.CodeListConnector
import controllers.declaration.routes.{LocationOfGoodsController, OfficeOfExitController, RoutingCountriesController}
import controllers.helpers.TransportSectionHelper.{Guernsey, Jersey}
import forms.declaration.BorderTransport
import forms.declaration.ModeOfTransportCode.Maritime
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.SUPPLEMENTARY_EIDR
import forms.declaration.countries.Country
import models.DeclarationType._
import models.codes.{Country => ModelCountry}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.destinationCountries.destination_country

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.global

class DestinationCountryControllerSpec extends ControllerSpec with AuditedControllerSpec with MockTaggedCodes {

  val destinationCountryPage = mock[destination_country]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new DestinationCountryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    taggedAuthCodes,
    destinationCountryPage
  )(global, mockCodeListConnector, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(destinationCountryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap("PL" -> ModelCountry("Poland", "PL")))
  }

  override protected def afterEach(): Unit = {
    reset(destinationCountryPage, mockCodeListConnector, auditService)
    super.afterEach()
  }

  def theResponseForm: Form[Country] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[Country]])
    verify(destinationCountryPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  "Destination Country Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        withNewCaching(aDeclaration())

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verify(destinationCountryPage).apply(any())(any(), any())
      }

      "display page method is invoked and cache contains data" in {
        withNewCaching(aDeclaration(withDestinationCountry()))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verify(destinationCountryPage).apply(any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form contains incorrect country" in {
        withNewCaching(aDeclaration())

        val incorrectForm = Json.obj("countryCode" -> "incorrect")

        val result = controller.submit(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER) and redirect" when {

      def redirectForDeclarationType(declarationType: DeclarationType, redirect: Call): Unit =
        "redirect" in {
          withNewCaching(aDeclaration(withType(declarationType)))

          val formData = Json.obj("countryCode" -> "PL")

          val result = controller.submit(postRequest(formData))

          status(result) mustBe SEE_OTHER
          verifyAudit()
          thePageNavigatedTo mustBe redirect
        }

      val redirectToOfficeOfExit: Unit =
        "redirect" in {
          val holders = withAuthorisationHolders(Some(taggedAuthCodes.codesSkippingLocationOfGoods.head))
          withNewCaching(aDeclaration(withAdditionalDeclarationType(SUPPLEMENTARY_EIDR), holders))

          val formData = Json.obj("countryCode" -> "PL")

          val result = controller.submit(postRequest(formData))

          status(result) mustBe SEE_OTHER
          verifyAudit()
          thePageNavigatedTo mustBe OfficeOfExitController.displayPage
        }

      "submit for Standard declaration" should {
        behave like redirectForDeclarationType(STANDARD, RoutingCountriesController.displayRoutingQuestion)
      }

      "submit for Simplified declaration" should {
        behave like redirectForDeclarationType(SIMPLIFIED, RoutingCountriesController.displayRoutingQuestion)
      }

      "submit for Occasional declaration" should {
        behave like redirectForDeclarationType(OCCASIONAL, RoutingCountriesController.displayRoutingQuestion)
      }

      "submit for Supplementary declaration" should {
        behave like redirectForDeclarationType(SUPPLEMENTARY, LocationOfGoodsController.displayPage)
      }

      "submit for Customs Clearance request" should {
        behave like redirectForDeclarationType(CLEARANCE, LocationOfGoodsController.displayPage)
      }

      "conditions for skipping location of goods pass" should {
        behave like redirectToOfficeOfExit
      }
    }

    "reset the cache for 'Departure Transport', 'Border transport' and 'Transport Country'" when {
      standardAndSupplementary.zip(List(ModelCountry("Guernsey", Guernsey), ModelCountry("Jersey", Jersey))).foreach { case (journey, modelCountry) =>
        s"the 'submitForm' method is invoked and destination country selected is '${modelCountry.countryName}'" in {
          when(mockCodeListConnector.getCountryCodes(any())).thenReturn(ListMap(modelCountry.countryCode -> modelCountry))

          val departureTransport = withDepartureTransport(Maritime, "10", "identifier")
          val borderTransport = withBorderTransport(BorderTransport("type", "number"))
          val transportCountry = withTransportCountry(Some("IT"))
          withNewCaching(aDeclaration(withType(journey), departureTransport, borderTransport, transportCountry))

          val formData = Json.obj("countryCode" -> modelCountry.countryCode)

          val result = controller.submit(postRequest(formData))

          status(result) mustBe SEE_OTHER

          val transport = theCacheModelUpdated.transport
          transport.meansOfTransportOnDepartureType mustBe None
          transport.meansOfTransportOnDepartureIDNumber mustBe None
          transport.meansOfTransportCrossingTheBorderType mustBe None
          transport.meansOfTransportCrossingTheBorderIDNumber mustBe None
          transport.transportCrossingTheBorderNationality mustBe None

          verifyAudit()
        }
      }
    }
  }
}
