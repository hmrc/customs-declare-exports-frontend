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

import controllers.declaration.OfficeOfExitOutsideUkController
import forms.declaration.Document
import forms.declaration.officeOfExit.OfficeOfExitOutsideUK
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.office_of_exit_outside_uk

class OfficeOfExitOutsideUkControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[office_of_exit_outside_uk]

  val controller = new OfficeOfExitOutsideUkController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockPage,
    mockExportsCacheService
  )(ec)

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockPage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def theResponseForm: Form[OfficeOfExitOutsideUK] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[OfficeOfExitOutsideUK]])
    verify(mockPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockPage)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
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

      "display page method is invoked and cache contains data" in {

        val officeId = "officeId"
        withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExitOutsideUK(officeId)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        checkViewInteractions()

        theResponseForm.value.value.officeId mustBe officeId
      }

    }
  }

  "should return a 400 (BAD_REQUEST)" when {
    onEveryDeclarationJourney() { request =>
      "invalid characters are submitted" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(OfficeOfExitOutsideUK("!@#$"))

        val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "form is empty" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(OfficeOfExitOutsideUK(""))

        val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "office code is bigger than 8 characters" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(OfficeOfExitOutsideUK("GB34567890"))

        val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        checkViewInteractions()
      }

      "office code is in the wrong format" in {

        withNewCaching(request.cacheModel)

        val incorrectForm = Json.toJson(OfficeOfExitOutsideUK("34567890"))

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

        val correctForm = Json.toJson(OfficeOfExitOutsideUK("GB123456"))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.TotalNumberOfItemsController.displayPage()
        checkViewInteractions(0)
      }
    }

    onJourney(DeclarationType.CLEARANCE, DeclarationType.OCCASIONAL, DeclarationType.SIMPLIFIED) { request =>
      "a UK Office of Exit is being used" in {

        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(OfficeOfExitOutsideUK("GB123456"))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
        checkViewInteractions(0)
      }

      "a UK Office of Exit is being used and user has documents in cache" in {

        withNewCaching(request.cacheModel.updatePreviousDocuments(Seq(Document("Y", "355", "reference", None))))

        val correctForm = Json.toJson(OfficeOfExitOutsideUK("GB123456"))

        val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsSummaryController.displayPage()
        checkViewInteractions(0)
      }
    }
  }
}
