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

package controllers.declaration

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.actions.AmendmentDraftFilterSpec
import controllers.declaration.routes.{ConsignmentReferencesController, DeclarantExporterController, DucrChoiceController, NotEligibleController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantEoriConfirmation
import forms.declaration.DeclarantEoriConfirmation.isEoriKey
import models.DeclarationType._
import models.requests.SessionHelper
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.declarant_details

class DeclarantDetailsControllerSpec extends ControllerSpec with AuditedControllerSpec with AmendmentDraftFilterSpec {

  private val declarantDetailsPage = mock[declarant_details]

  val controller =
    new DeclarantDetailsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, declarantDetailsPage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))
    when(declarantDetailsPage.apply(any[Form[DeclarantEoriConfirmation]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(declarantDetailsPage)
  }

  def nextPageOnTypes: Seq[NextPageOnType] =
    List(STANDARD, OCCASIONAL, SIMPLIFIED).map(NextPageOnType(_, DucrChoiceController.displayPage)) :+
      NextPageOnType(CLEARANCE, DeclarantExporterController.displayPage) :+
      NextPageOnType(SUPPLEMENTARY, ConsignmentReferencesController.displayPage)

  def theResponseForm: Form[DeclarantEoriConfirmation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarantEoriConfirmation]])
    verify(declarantDetailsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "DeclarantDetailsController on displayOutcomePage" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }

      "display page method is invoked and cache is not empty" in {
        withNewCaching(aDeclaration(withDeclarantDetails()))

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
      }
    }
  }

  "DeclarantDetailsController on submitForm" when {

    "form contains incorrect values" should {
      "return 400 (BAD_REQUEST)" in {
        val incorrectForm = Json.obj(isEoriKey -> "wrong")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyNoAudit()
      }
    }

    "answer is yes" should {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { request =>
        "return 303 (SEE_OTHER) and redirect to DucrChoiceController details page" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.obj(isEoriKey -> YesNoAnswers.yes)

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe DucrChoiceController.displayPage
          verifyAudit()
        }
      }

      onJourney(SUPPLEMENTARY) { request =>
        "return 303 (SEE_OTHER) and redirect to Consignment References details page" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.obj(isEoriKey -> YesNoAnswers.yes)

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe ConsignmentReferencesController.displayPage
          verifyAudit()
        }
      }

      onClearance { request =>
        "return 303 (SEE_OTHER) and redirect to Declarant Exporter page" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.obj(isEoriKey -> YesNoAnswers.yes)

          val result = controller.submitForm()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe DeclarantExporterController.displayPage
          verifyAudit()
        }
      }
    }

    "answer is no" should {
      onEveryDeclarationJourney() { request =>
        "return 303 (SEE_OTHER) and redirect to Not Eligible page" in {
          withNewCaching(request.cacheModel)

          val correctForm = Json.obj(isEoriKey -> YesNoAnswers.no)

          val result = controller.submitForm()(postRequest(correctForm))
          redirectLocation(result) mustBe Some(NotEligibleController.displayNotDeclarant.url)

          session(result).get(SessionHelper.declarationUuid) must be(None)
          verifyNoAudit()
        }
      }
    }
  }
}
