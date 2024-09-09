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

package controllers.section5

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.section5.routes._
import forms.common.YesNoAnswer
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section5.additionalInformation.additional_information_required

class AdditionalInformationRequiredControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  private val mockPage = mock[additional_information_required]

  val controller = new AdditionalInformationRequiredController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockPage)(
    ec,
    auditService
  )

  val itemId = "itemId"

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(navigator.backLinkForAdditionalInformation(any(), any())(any()))
      .thenReturn(CommodityMeasureController.displayPage(itemId))
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  "AdditionalInformationRequired Controller" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {

        "display page method is invoked and cache is empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

          val result = controller.displayPage(itemId)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "Additional item(s) exist in cache" in {
          withNewCaching(
            aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId), withAdditionalInformation("code", "description"))))
          )

          val result = controller.displayPage(itemId)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalInformationController.displayPage(itemId)
        }

        "user submits valid Yes answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AdditionalInformationController.displayPage(itemId)
          verifyAudit()
        }
      }
    }

    onClearance { request =>
      "user submits valid No answer go to 'Additional Documents'" in {
        withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

        val requestBody = Seq("yesNo" -> "No")
        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalDocumentsController.displayPage(itemId)
        verifyAudit()
      }
    }

    onJourney(STANDARD, OCCASIONAL, SUPPLEMENTARY, SIMPLIFIED) { request =>
      "user submits valid No answer go to 'Is License Required?'" in {
        withNewCaching(aDeclarationAfter(request.cacheModel, withItem(anItem(withItemId(itemId)))))

        val requestBody = Seq("yesNo" -> "No")
        val result = controller.submitForm(itemId)(postRequestAsFormUrlEncoded(requestBody: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe IsLicenceRequiredController.displayPage(itemId)
        verifyAudit()
      }
    }
  }
}
