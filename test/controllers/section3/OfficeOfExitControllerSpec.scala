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

package controllers.section3

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.summary.routes.SectionSummaryController
import forms.section3.OfficeOfExit
import forms.section3.OfficeOfExit.fieldId
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section3.office_of_exit

class OfficeOfExitControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockOfficeOfExitPage = mock[office_of_exit]

  val controller =
    new OfficeOfExitController(mockAuthAction, mockJourneyAction, navigator, mcc, mockOfficeOfExitPage, mockExportsCacheService)(ec, auditService)

  def checkViewInteractions(noOfInvocations: Int = 1): Unit =
    verify(mockOfficeOfExitPage, times(noOfInvocations)).apply(any())(any(), any())

  def theResponseForm: Form[OfficeOfExit] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[OfficeOfExit]])
    verify(mockOfficeOfExitPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockOfficeOfExitPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockOfficeOfExitPage)
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  "OfficeOfExitController" should {

    "return a 200 (OK)" when {
      onEveryDeclarationJourney() { request =>
        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          checkViewInteractions()

          theResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains Office Of Exit inside UK" in {
          val officeId = "GB123456"
          withNewCaching(aDeclarationAfter(request.cacheModel, withOfficeOfExit(officeId)))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          checkViewInteractions()

          theResponseForm.value.value.officeId mustBe officeId
        }
      }
    }

    "return a 303 (SEE_OTHER)" when {

      onEveryDeclarationJourney() { request =>
        "a UK Office of Exit is being used" in {
          withNewCaching(request.cacheModel)

          val officeOfExitInput = "GB123456"

          val correctForm = Json.toJson(OfficeOfExit(officeOfExitInput))

          val result = controller.saveOffice(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SectionSummaryController.displayPage(3)
          checkViewInteractions(0)
          theCacheModelUpdated.locations.officeOfExit must be(Some(OfficeOfExit(officeOfExitInput)))
          verifyAudit()
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "no value is entered" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(fieldIdOnError(fieldId) -> "")
        val result = controller.saveOffice(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        theResponseForm.errors.head.messages.head mustBe "declaration.officeOfExit.empty"
      }

      "the entered value is incorrect or not a list's option" in {
        withNewCaching(aStandardDeclaration)

        val incorrectForm = Json.obj(fieldIdOnError(fieldId) -> "!@#$")
        val result = controller.saveOffice(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()

        val errors = theResponseForm.errors
        errors(0).messages.head mustBe "declaration.officeOfExit.length"
        errors(1).messages.head mustBe "declaration.officeOfExit.specialCharacters"
      }
    }
  }
}
