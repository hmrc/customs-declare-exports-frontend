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

import base.TestHelper._
import base.ControllerSpec
import forms.declaration.Mucr
import models.DeclarationType.{OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.requests.JourneyRequest
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.mucr_code

class MucrControllerSpec extends ControllerSpec {

  private val mucrPage = mock[mucr_code]

  val controller =
    new MucrController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, stubMessagesControllerComponents(), mucrPage)(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(mucrPage.apply(any[Mode], any[Form[Mucr]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mucrPage)
    super.afterEach()
  }

  def theResponseForm: Form[Mucr] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Mucr]])
    verify(mucrPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "MucrController on displayPage" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(Mode.Normal)(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }

      "display page method is invoked and cache is not empty" in {
        withNewCaching(aDeclaration(withMucr()))

        val result = controller.displayPage(Mode.Normal)(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }
    }
  }

  "MucrController on submitForm" should {

    "return 303 (SEE_OTHER) and redirect to 'Are you the exporter?' page" when {
      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { implicit request =>
        "a valid MUCR is entered" in {
          verifyRedirect(routes.DeclarantExporterController.displayPage())
        }
      }
    }

    "return 303 (SEE_OTHER) and redirect to 'Is this an entry into declarant's records?' page" when {
      onClearance { implicit request =>
        "a valid MUCR is entered" in {
          verifyRedirect(routes.EntryIntoDeclarantsRecordsController.displayPage())
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in {
        val incorrectForm = Json.obj(Mucr.MUCR -> "not-allowed-chars !^&")

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
      }

      "no data has been entered on the page" in {
        val incorrectForm = Json.obj(Mucr.MUCR -> "")

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
      }

      "data entered is too long" in {
        val incorrectForm = Json.obj(Mucr.MUCR -> createRandomAlphanumericString(36))

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
      }
    }
  }

  private def verifyPageInvoked: HtmlFormat.Appendable =
    verify(mucrPage).apply(any[Mode], any[Form[Mucr]])(any(), any())

  private def verifyRedirect(call: Call)(implicit request: JourneyRequest[_]): Assertion = {
    withNewCaching(request.cacheModel)
    val correctForm = Json.obj(Mucr.MUCR -> MUCR.mucr)

    val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

    status(result) mustBe SEE_OTHER
    thePageNavigatedTo mustBe call
  }
}
