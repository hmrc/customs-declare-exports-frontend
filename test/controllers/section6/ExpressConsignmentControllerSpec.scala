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

package controllers.section6

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.general.routes.RootController
import controllers.section6.routes.{ContainerController, TransportPaymentController}
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section6.TransportPayment
import forms.section6.TransportPayment.cash
import models.DeclarationType
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
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
import views.html.section6.express_consignment

class ExpressConsignmentControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val expressConsignmentPage = mock[express_consignment]

  val controller =
    new ExpressConsignmentController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, expressConsignmentPage)(
      ec,
      auditService
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))
    when(expressConsignmentPage.apply(any[Form[YesNoAnswer]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(expressConsignmentPage, auditService)
    super.afterEach()
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(expressConsignmentPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "ExpressConsignmentController" should {

    onJourney(STANDARD, CLEARANCE, SIMPLIFIED, OCCASIONAL) { implicit request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {
          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
          verifyPageInvoked
        }

        "display page method is invoked and cache is not empty" in {
          val payment = Some(TransportPayment(cash))
          withNewCaching(aDeclaration(withTransportPayment(payment)))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
          verifyPageInvoked
        }
      }

      "return 303 (SEE_OTHER) and redirect to the 'Transport payment method' page" when {
        "answer is 'yes'" in {
          verifyRedirect(Some(YesNoAnswers.yes), Some(TransportPaymentController.displayPage))
        }
      }

      "return 303 (SEE_OTHER) and redirect to 'Transport container' page" when {
        "answer is 'no'" in {
          verifyRedirect(Some(YesNoAnswers.no), Some(ContainerController.displayContainerSummary))
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "neither Yes or No have been selected on the page" in {
          val incorrectForm = Json.obj("yesNo" -> "")

          val result = controller.submitForm()(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
          verifyPageInvoked
          verifyNoAudit()
        }
      }
    }

    onJourney(SUPPLEMENTARY) { implicit request =>
      "return 303 (SEE_OTHER)" when {

        "displayOutcomePage method is invoked" in {
          verifyRedirect(None)
        }

        "submitForm method is invoked" in {
          verifyRedirect(Some(YesNoAnswers.yes))
        }
      }
    }
  }

  private def verifyPageInvoked: HtmlFormat.Appendable =
    verify(expressConsignmentPage).apply(any[Form[YesNoAnswer]])(any(), any())

  private def verifyRedirect(yesOrNo: Option[String], call: Option[Call] = None)(implicit request: JourneyRequest[_]): Assertion = {
    withNewCaching(request.cacheModel)

    val result = yesOrNo.fold {
      controller.displayPage(getRequest())
    } { yn =>
      controller.submitForm()(postRequest(Json.obj("yesNo" -> yn)))
    }

    status(result) mustBe SEE_OTHER

    call.fold {
      redirectLocation(result) mustBe Some(RootController.displayPage.url)
    } {
      thePageNavigatedTo mustBe _
    }
  }
}
