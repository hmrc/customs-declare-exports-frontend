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
import forms.declaration.officeOfExit.{OfficeOfExitClearance, OfficeOfExitStandard, OfficeOfExitSupplementary}
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
import views.html.declaration.officeOfExit._

class OfficeOfExitControllerSpec extends ControllerSpec with OptionValues {

  val mockOfficeOfExitSupplementaryPage = mock[office_of_exit_supplementary]
  val mockOfficeOfExitStandardPage = mock[office_of_exit_standard]
  val mockOfficeOfExitClearancePage = mock[office_of_exit_clearance]

  val controller = new OfficeOfExitController(
    mockAuthAction,
    mockJourneyAction,
    navigator,
    stubMessagesControllerComponents(),
    mockOfficeOfExitSupplementaryPage,
    mockOfficeOfExitStandardPage,
    mockOfficeOfExitClearancePage,
    mockExportsCacheService
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockOfficeOfExitSupplementaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockOfficeOfExitStandardPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockOfficeOfExitClearancePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockOfficeOfExitSupplementaryPage, mockOfficeOfExitStandardPage, mockOfficeOfExitClearancePage)
  }

  def checkSupplementaryViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockOfficeOfExitSupplementaryPage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def checkStandardViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockOfficeOfExitStandardPage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def checkClearanceViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockOfficeOfExitClearancePage, times(noOfInvocations)).apply(any(), any())(any(), any())

  def theSupplementaryResponseForm: Form[OfficeOfExitSupplementary] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[OfficeOfExitSupplementary]])
    verify(mockOfficeOfExitSupplementaryPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theStandardResponseForm: Form[OfficeOfExitStandard] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[OfficeOfExitStandard]])
    verify(mockOfficeOfExitStandardPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theClearanceResponseForm: Form[OfficeOfExitClearance] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[OfficeOfExitClearance]])
    verify(mockOfficeOfExitClearancePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Office of Exit controller" should {

    onSupplementary { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkSupplementaryViewInteractions()

          theSupplementaryResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val officeId = "officeId"
          withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkSupplementaryViewInteractions()

          theSupplementaryResponseForm.value.value.officeId mustBe officeId
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

          val incorrectForm = Json.toJson(OfficeOfExitSupplementary("!@#$"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          checkSupplementaryViewInteractions()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {

          withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

          val correctForm = Json.toJson(OfficeOfExitSupplementary("officeId"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TotalNumberOfItemsController.displayPage()
          checkSupplementaryViewInteractions(0)
        }
      }
    }

    onStandard { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkStandardViewInteractions()

          theStandardResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val officeId = "officeId"
          val circumstancesCode = "Yes"
          withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId, Some(circumstancesCode))))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkStandardViewInteractions()

          theStandardResponseForm.value.value.officeId mustBe officeId
          theStandardResponseForm.value.value.circumstancesCode mustBe circumstancesCode
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(OfficeOfExitStandard("!@#$", "wrong"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          checkStandardViewInteractions()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {

          withNewCaching(request.cacheModel)

          val correctForm = Json.toJson(OfficeOfExitStandard("officeId", "Yes"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.TotalNumberOfItemsController.displayPage()
          checkStandardViewInteractions(0)
        }
      }
    }

    onSimplified { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkStandardViewInteractions()

          theStandardResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val officeId = "officeId"
          val circumstancesCode = "Yes"
          withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId, Some(circumstancesCode))))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkStandardViewInteractions()

          theStandardResponseForm.value.value.officeId mustBe officeId
          theStandardResponseForm.value.value.circumstancesCode mustBe circumstancesCode
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(OfficeOfExitStandard("!@#$", "wrong"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          checkStandardViewInteractions()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {

          withNewCaching(request.cacheModel)

          val correctForm = Json.toJson(OfficeOfExitStandard("officeId", "Yes"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
          checkStandardViewInteractions(0)
        }
      }
    }
    onOccasional { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkStandardViewInteractions()

          theStandardResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val officeId = "officeId"
          val circumstancesCode = "Yes"
          withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId, Some(circumstancesCode))))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkStandardViewInteractions()

          theStandardResponseForm.value.value.officeId mustBe officeId
          theStandardResponseForm.value.value.circumstancesCode mustBe circumstancesCode
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(OfficeOfExitStandard("!@#$", "wrong"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          checkStandardViewInteractions()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {

          withNewCaching(request.cacheModel)

          val correctForm = Json.toJson(OfficeOfExitStandard("officeId", "Yes"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
          checkStandardViewInteractions(0)
        }
      }
    }

    onClearance { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkClearanceViewInteractions()

          theClearanceResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val officeId = Some("officeId")
          val circumstancesCode = "Yes"
          withNewCaching(aDeclarationAfter(request.cacheModel, withOptionalOfficeOfExit(officeId, Some(circumstancesCode))))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          checkClearanceViewInteractions()

          theClearanceResponseForm.value.value.officeId mustBe officeId
          theClearanceResponseForm.value.value.circumstancesCode mustBe circumstancesCode
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(OfficeOfExitStandard("!@#$", "wrong"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          checkClearanceViewInteractions()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "information provided by user are correct" in {

          withNewCaching(request.cacheModel)

          val correctForm = Json.toJson(OfficeOfExitStandard("officeId", "Yes"))

          val result = controller.saveOffice(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
          checkClearanceViewInteractions(0)
        }
      }
    }
  }
}
