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
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import play.twirl.api.HtmlFormat.Appendable
import views.html.declaration.additionalDocuments.additional_documents_required

class AdditionalDocumentsRequiredControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val page = mock[additional_documents_required]

  val controller = new AdditionalDocumentsRequiredController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mcc,
    page
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(page.apply(any[String], any[Form[YesNoAnswer]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  val itemId = "itemId"

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(page).apply(any[String], captor.capture())(any(), any())
    captor.getValue
  }

  "AdditionalDocumentsRequiredController" should {

    onEveryDeclarationJourney() { implicit request =>
      "return 200 (OK)" when {
        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))
          val result = controller.displayPage(itemId)(getRequest())
          status(result) must be(OK)
          verifyPageInvoked
        }
      }

      "return 303 (SEE_OTHER) and redirect to the 'Additional Documents' page" when {
        "answer is 'yes'" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

          val result = controller.submitForm(itemId)(postRequest(Json.obj("yesNo" -> YesNoAnswers.yes)))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.AdditionalDocumentAddController.displayPage(itemId)
          verifyAudit()
        }
      }

      "return 303 (SEE_OTHER) and redirect to the 'Items Summary' page" when {
        "answer is 'no'" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

          val result = controller.submitForm(itemId)(postRequest(Json.obj("yesNo" -> YesNoAnswers.no)))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe routes.ItemsSummaryController.displayItemsSummaryPage
          verifyAudit()
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "neither Yes or No have been selected on the page" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))
          val incorrectForm = Json.obj("yesNo" -> "")

          val result = controller.submitForm(itemId)(postRequest(incorrectForm))
          status(result) must be(BAD_REQUEST)
          verifyPageInvoked
          verifyNoAudit()
        }
      }
    }
  }

  private def verifyPageInvoked: Appendable = verify(page).apply(any[String], any[Form[YesNoAnswer]])(any(), any())
}
