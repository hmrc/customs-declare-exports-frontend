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

package controllers.section1

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.actions.AmendmentDraftFilterSpec
import controllers.section1.routes.{DucrEntryController, TraderReferenceController}
import controllers.routes.RootController
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section1.ducr_choice

class DucrChoiceControllerSpec extends ControllerSpec with AuditedControllerSpec with AmendmentDraftFilterSpec {

  private val ducrChoicePage = mock[ducr_choice]

  val controller =
    new DucrChoiceController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, ducrChoicePage)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aStandardDeclaration)
    when(ducrChoicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(ducrChoicePage)
    super.afterEach()
  }

  def nextPageOnTypes: Seq[NextPageOnType] =
    allDeclarationTypesExcluding(SUPPLEMENTARY).map(NextPageOnType(_, DucrEntryController.displayPage))

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(ducrChoicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  "DucrChoiceController.displayOutcomePage" should {

    "return 200 (OK)" when {
      List(STANDARD, CLEARANCE, SIMPLIFIED, OCCASIONAL).foreach { declarationType =>
        s"journey is $declarationType" in {
          withNewCaching(aDeclaration(withType(declarationType)))

          val result = controller.displayPage(getRequest())
          status(result) must be(OK)
        }
      }
    }

    "redirect to /" when {
      "journey is SUPPLEMENTARY" in {
        withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))

        val result = controller.displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
      }
    }
  }

  "DucrChoiceController.submitForm" should {

    "return 400 (BAD_REQUEST)" when {
      List(STANDARD, CLEARANCE, SIMPLIFIED, OCCASIONAL).foreach { declarationType =>
        s"journey is $declarationType and" when {
          "there is no answer from the user" in {
            withNewCaching(aDeclaration(withType(declarationType)))

            val body = Json.obj(YesNoAnswer.formId -> "")
            val result = controller.submitForm(postRequest(body))

            status(result) must be(BAD_REQUEST)
            verifyNoAudit()
          }
        }
      }
    }

    "redirect to /" when {
      "journey is SUPPLEMENTARY" in {
        withNewCaching(aDeclaration(withType(SUPPLEMENTARY)))

        val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
        val result = controller.submitForm(postRequest(body))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(RootController.displayPage.url)
        verifyNoAudit()
      }
    }
  }

  "DucrChoiceController.submitForm" when {

    "answer is no and" when {
      List(STANDARD, CLEARANCE, SIMPLIFIED, OCCASIONAL).foreach { declarationType =>
        s"journey is $declarationType" should {
          "redirect to /trader-reference" in {
            withNewCaching(aDeclaration(withType(declarationType)))

            val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.no)
            val result = controller.submitForm(postRequest(body))

            status(result) mustBe SEE_OTHER
            thePageNavigatedTo mustBe TraderReferenceController.displayPage
            verifyAudit()
          }
        }
      }
    }

    "answer is yes and" when {
      List(STANDARD, CLEARANCE, SIMPLIFIED, OCCASIONAL).foreach { declarationType =>
        s"journey is $declarationType" should {
          "reset ConsignmentReference.ducr and" should {
            "redirect to /ducr-entry" in {
              withNewCaching(aDeclaration(List(withType(declarationType), withConsignmentReferences()): _*))

              val body = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
              val result = controller.submitForm(postRequest(body))

              status(result) mustBe SEE_OTHER

              val consignmentReferences = theCacheModelUpdated.consignmentReferences.get
              consignmentReferences.ducr mustBe None
              consignmentReferences.lrn.get.lrn mustBe LRN.lrn

              thePageNavigatedTo mustBe DucrEntryController.displayPage
              verifyAudit()
            }
          }
        }
      }
    }
  }
}
