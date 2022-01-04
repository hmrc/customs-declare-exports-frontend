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

package controllers.declaration

import base.ControllerSpec
import forms.common.Eori
import forms.declaration.{EntityDetails, RepresentativeEntity}
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.representative_details_entity

class RepresentativeEntityControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[representative_details_entity]

  val controller = new RepresentativeEntityController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    mockExportsCacheService,
    stubMessagesControllerComponents(),
    mockPage
  )(ec)

  val eori = "GB12345678912345"

  def theResponseForm: Form[RepresentativeEntity] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[RepresentativeEntity]])
    verify(mockPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  def verifyPage(numberOfTimes: Int) = verify(mockPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  "Representative Entity controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked with empty cache" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verifyPage(1)

          theResponseForm.value mustBe empty
        }

        "display page method is invoked with data in cache" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withRepresentativeDetails(Some(Eori(eori)), None, None)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe (OK)
          verifyPage(1)

          theResponseForm.value.flatMap(_.details.eori) mustBe Some(Eori(eori))
        }

      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(RepresentativeEntity(EntityDetails(None, None)))

          val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verifyPage(1)
        }
      }
    }

    onEveryDeclarationJourney() { request =>
      "return 303 (SEE_OTHER) and redirect to carrier details page" in {

        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(RepresentativeEntity(EntityDetails(Some(Eori(eori)), None)))

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RepresentativeStatusController.displayPage()

        verifyPage(0)
      }
    }
  }
}
