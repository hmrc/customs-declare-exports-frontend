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

package controllers.section2

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section2.routes.{RepresentativeEntityController, RepresentativeStatusController}
import forms.common.Eori
import forms.section2.RepresentativeAgent
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.representative_details_agent

class RepresentativeAgentControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockPage = mock[representative_details_agent]

  val controller =
    new RepresentativeAgentController(mockAuthAction, mockJourneyAction, navigator, mockExportsCacheService, mcc, mockPage)(ec, auditService)

  def theResponseForm: Form[RepresentativeAgent] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[RepresentativeAgent]])
    verify(mockPage).apply(formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage, auditService)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def verifyPage(numberOfTimes: Int): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any())(any(), any())

  "Representative Agent controller" must {
    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "display page method is invoked with empty cache" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyPage(1)

          theResponseForm.value mustBe empty
        }

        "display page method is invoked with data in cache" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withRepresentativeDetails(None, None, Some("Yes"))))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyPage(1)

          theResponseForm.value.map(_.representingAgent) mustBe Some("Yes")
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(RepresentativeAgent("invalid"))
          val result = controller.submitForm()(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verifyPage(1)
          verifyNoAudit()
        }
      }
    }

    onEveryDeclarationJourney() { request =>
      "representing other agent" should {
        val formAnswer = "Yes"

        "redirect (303 SEE_OTHER) to representative entity" in {
          withNewCaching(request.cacheModel)

          val correctForm = Json.toJson(RepresentativeAgent(formAnswer))
          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RepresentativeStatusController.displayPage

          verifyPage(0)
          verifyAudit()
        }

        "clear the representative eori if already present " in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withRepresentativeDetails(Some(Eori("GB1234567890")), None, None)))

          val correctForm = Json.toJson(RepresentativeAgent(formAnswer))
          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage

          val representativeDetails = theCacheModelUpdated.parties.representativeDetails
          representativeDetails.flatMap(_.details).isDefined mustBe false
          verifyAudit()
        }
      }

      "not representing other agent" should {
        val formAnswer = "No"

        "redirect (303 SEE_OTHER) to representative status" in {
          withNewCaching(request.cacheModel)

          val correctForm = Json.toJson(RepresentativeAgent(formAnswer))
          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RepresentativeEntityController.displayPage

          verifyPage(0)
          verifyAudit()
        }

        "not clear the representative eori if already present" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withRepresentativeDetails(Some(Eori("GB1234567890")), None, None)))

          val correctForm = Json.toJson(RepresentativeAgent(formAnswer))
          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage

          val representativeDetails = theCacheModelUpdated.parties.representativeDetails
          representativeDetails.flatMap(_.details).isDefined mustBe true
          verifyAudit()
        }
      }
    }
  }
}
