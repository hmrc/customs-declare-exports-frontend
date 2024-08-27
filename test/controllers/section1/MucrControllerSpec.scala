/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section1

import base.TestHelper._
import base.{AuditedControllerSpec, ControllerSpec}
import controllers.actions.AmendmentDraftFilterSpec
import controllers.summary.routes.SectionSummaryController
import forms.section1.Mucr
import models.DeclarationType._
import models.requests.JourneyRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section1.mucr_code

class MucrControllerSpec extends ControllerSpec with AuditedControllerSpec with AmendmentDraftFilterSpec {

  private val mucrPage = mock[mucr_code]

  val controller = new MucrController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mucrPage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aStandardDeclaration)
    when(mucrPage.apply(any[Form[Mucr]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mucrPage)
    super.afterEach()
  }

  def nextPageOnTypes: Seq[NextPageOnType] =
    nonClearanceJourneys.map(NextPageOnType(_, SectionSummaryController.displayPage(1)))

  def theResponseForm: Form[Mucr] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Mucr]])
    verify(mucrPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "MucrController on displayOutcomePage" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }

      "display page method is invoked and cache is not empty" in {
        withNewCaching(aDeclaration(withMucr()))

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }
    }
  }

  "MucrController on submitForm" should {

    "return 303 (SEE_OTHER) and redirect to the 'Section Summary' page" when {
      onEveryDeclarationJourney() { implicit request =>
        "a valid MUCR is entered" in {
          verifyRedirect(SectionSummaryController.displayPage(1))
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in {
        val incorrectForm = Json.obj(Mucr.MUCR -> "not-allowed-chars !^&")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
        verifyNoAudit()
      }

      "no data has been entered on the page" in {
        val incorrectForm = Json.obj(Mucr.MUCR -> "")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
        verifyNoAudit()
      }

      "data entered is too long" in {
        val incorrectForm = Json.obj(Mucr.MUCR -> createRandomAlphanumericString(36))

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
        verifyNoAudit()
      }
    }
  }

  private def verifyPageInvoked: HtmlFormat.Appendable =
    verify(mucrPage).apply(any[Form[Mucr]])(any(), any())

  private def verifyRedirect(call: Call)(implicit request: JourneyRequest[_]): Assertion = {
    withNewCaching(request.cacheModel)
    val correctForm = Json.obj(Mucr.MUCR -> MUCR.mucr)

    val result = controller.submitForm()(postRequest(correctForm))

    status(result) mustBe SEE_OTHER
    verifyAudit()
    thePageNavigatedTo mustBe call
  }
}
