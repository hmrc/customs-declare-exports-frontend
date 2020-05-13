/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.declaration.OfficeOfExitController
import forms.declaration.officeOfExit.{AllowedUKOfficeOfExitAnswers, OfficeOfExit, OfficeOfExitInsideUK}
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
import views.html.declaration.office_of_exit

class OfficeOfExitControllerSpec extends ControllerSpec with OptionValues {

  val mockOfficeOfExitPage = mock[office_of_exit]

  val controller = new OfficeOfExitController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockOfficeOfExitPage,
    mockExportsCacheService
  )(ec)

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockOfficeOfExitPage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def theResponseForm: Form[OfficeOfExitInsideUK] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[OfficeOfExitInsideUK]])
    verify(mockOfficeOfExitPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockOfficeOfExitPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockOfficeOfExitPage)
  }

  "should return a 200 (OK)" when {
    onEveryDeclarationJourney() { request =>
      "display page method is invoked and cache is empty" in {

        withNewCaching(request.cacheModel)

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains Office Of Exit inside UK" in {

        val officeId = "GB123456"
        val answer = "Yes"
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId, answer)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.officeId mustBe Some(officeId)
        theResponseForm.value.value.isUkOfficeOfExit mustBe answer
      }

      "display page method is invoked and cache contains Office Of Exit outside UK" in {

        val officeId = "NL123456"
        val answer = "No"
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId, answer)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.officeId mustBe None
        theResponseForm.value.value.isUkOfficeOfExit mustBe answer
      }
    }
  }

  "should return a 400 (BAD_REQUEST)" when {
    onEveryDeclarationJourney() { request =>
      "form is incorrect" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(OfficeOfExitInsideUK(Some("!@#$"), "wrong"))

        val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }
    }
  }

  "should return a 303 (SEE_OTHER)" when {
    onJourney(DeclarationType.STANDARD, DeclarationType.SUPPLEMENTARY) { request =>
      "a UK Office of Exit is being used" in {

        withNewCaching(request.cacheModel)

        val officeOfExitInput = Some("GB123456");
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.yes

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TotalNumberOfItemsController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(officeOfExitInput, Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected" in {

        withNewCaching(request.cacheModel)

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(officeOfExitInput, Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected, but already has Office Of Exit Outside UK in the cache" in {

        val officeOfExitFromCache = "NL000123";
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeOfExitFromCache, AllowedUKOfficeOfExitAnswers.no)))

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(Some(officeOfExitFromCache), Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected, but has Office Of Exit Inside UK in the cache" in {

        val officeOfExitFromCache = "GB000123";
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeOfExitFromCache, AllowedUKOfficeOfExitAnswers.yes)))

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(None, Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected, " +
        "the UK Office of Exit is selected by mistake and already has an existing Office Of Exit Inside UK in the cache" in {

        val officeOfExitFromCache = "GB000123";
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeOfExitFromCache, AllowedUKOfficeOfExitAnswers.yes)))

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(None, Some(isUKOfficeOfExitInput))))
      }
    }

    onJourney(DeclarationType.CLEARANCE, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { request =>
      "a UK Office of Exit is being used" in {

        withNewCaching(request.cacheModel)

        val officeOfExitInput = Some("GB123456");
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.yes

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(officeOfExitInput, Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected" in {

        withNewCaching(request.cacheModel)

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(officeOfExitInput, Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected, but already has Office Of Exit Outside UK in the cache" in {

        val officeOfExitFromCache = "NL000123";
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeOfExitFromCache, AllowedUKOfficeOfExitAnswers.no)))

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(Some(officeOfExitFromCache), Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected, but has Office Of Exit Inside UK in the cache" in {

        val officeOfExitFromCache = "GB000123";
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeOfExitFromCache, AllowedUKOfficeOfExitAnswers.yes)))

        val officeOfExitInput = None;
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(officeOfExitInput, isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(None, Some(isUKOfficeOfExitInput))))
      }

      "the non UK Office of Exit option is selected, " +
        "the UK Office of Exit is selected by mistake and already has an existing Office Of Exit Inside UK in the cache" in {

        val officeOfExitFromCache = "GB000123";
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeOfExitFromCache, AllowedUKOfficeOfExitAnswers.yes)))

        val officeOfExitInput = "GB000124";
        val isUKOfficeOfExitInput = AllowedUKOfficeOfExitAnswers.no

        val correctForm = Json.toJson(OfficeOfExitInsideUK(Some(officeOfExitInput), isUKOfficeOfExitInput))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.OfficeOfExitOutsideUkController.displayPage()
        checkViewInteractions(0)
        theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(None, Some(isUKOfficeOfExitInput))))
      }
    }
  }
}
