/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.controllers.declaration

import controllers.declaration.RepresentativeDetailsController
import forms.Choice
import forms.Choice.AllowedChoiceValues._
import forms.declaration.{EntityDetails, RepresentativeDetails}
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.representative_details

class RepresentativeDetailsControllerSpec extends ControllerSpec with OptionValues {

  val mockRepresentativeDetailsPage = mock[representative_details]

  val controller = new RepresentativeDetailsController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    mockRepresentativeDetailsPage
  )(ec)

  val eori = "GB1000200"

  def theResponseForm: Form[RepresentativeDetails] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[RepresentativeDetails]])
    verify(mockRepresentativeDetailsPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockRepresentativeDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRepresentativeDetailsPage)
    super.afterEach()
  }

  "Representative Details controller" must {

    onEveryDeclarationJourney() { declaration =>
      "return 200 (OK)" when {

        "display page method is invoked with empty cache" in {

          withNewCaching(declaration)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(mockRepresentativeDetailsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm.value mustBe empty
        }

        "display page method is invoked with data in cache" in {

          withNewCaching(
            declaration.copy(
              parties = declaration.parties
                .copy(representativeDetails = Some(RepresentativeDetails(Some(EntityDetails(Some(eori), None)), None)))
            )
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe (OK)
          verify(mockRepresentativeDetailsPage, times(1)).apply(any(), any())(any(), any())

          theResponseForm.value.value.details.value.eori.value mustBe eori
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(declaration)

          val incorrectForm = Json.toJson(RepresentativeDetails(None, Some("incorrect")))

          val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verify(mockRepresentativeDetailsPage, times(1)).apply(any(), any())(any(), any())
        }
      }
    }


    onJourney(DeclarationType.SUPPLEMENTARY, DeclarationType.CLEARANCE)() { declaration =>
      "return 303 (SEE_OTHER) and redirect to additional actors page" in {

        withNewCaching(declaration)

        val correctForm = Json.toJson(RepresentativeDetails(Some(EntityDetails(Some(eori), None)), Some("2")))

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationAdditionalActorsController.displayPage()

        verify(mockRepresentativeDetailsPage, times(0)).apply(any(), any())(any(), any())
      }
    }

    onJourney(DeclarationType.STANDARD, DeclarationType.SIMPLIFIED, DeclarationType.OCCASIONAL)() { declaration =>
      "return 303 (SEE_OTHER) and redirect to carrier details page" in {

        withNewCaching(declaration)

        val correctForm = Json.toJson(RepresentativeDetails(Some(EntityDetails(Some(eori), None)), Some("2")))

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.CarrierDetailsController.displayPage()

        verify(mockRepresentativeDetailsPage, times(0)).apply(any(), any())(any(), any())
      }
    }
  }
}
