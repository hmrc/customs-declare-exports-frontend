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
import controllers.section5.routes.AdditionalInformationController
import forms.common.YesNoAnswer.Yes
import forms.section5.AdditionalInformation
import forms.section5.AdditionalInformation.codeForGVMS
import models.declaration.AdditionalInformationData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.section5.additionalInformation.additional_information_change

class AdditionalInformationChangeControllerSpec extends ControllerSpec with AuditedControllerSpec {

  val mockChangePage = mock[additional_information_change]

  val controller =
    new AdditionalInformationChangeController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockChangePage)(
      ec,
      auditService
    )

  val itemId = "itemId"
  private val additionalInformation1 = AdditionalInformation("00400", "Some description")
  private val additionalInformation2 = AdditionalInformation("00401", "Some description")
  private val additionalInformationId = ListItem.createId(0, additionalInformation1)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withAdditionalInformation(additionalInformation1, additionalInformation2)))))
    when(mockChangePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(mockChangePage)
  }

  def theResponseForm: Form[AdditionalInformation] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[AdditionalInformation]])
    verify(mockChangePage).apply(any(), any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(itemId, additionalInformationId)(request))
    theResponseForm
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockChangePage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "AdditionalInformation controller" should {

    "return 200 (OK)" when {
      "display page method is invoked" in {
        val result = controller.displayPage(itemId, additionalInformationId)(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }
    }

    "return 400 (BAD_REQUEST) during adding" when {

      "user does not enter any data" in {
        val formData = Json.toJson(AdditionalInformation("", ""))
        val result = controller.submitForm(itemId, additionalInformationId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
        verifyNoAudit()
      }

      "user enters 'RRS01' as code" in {
        val formData = Json.toJson(AdditionalInformation(codeForGVMS, "description"))
        val result = controller.submitForm(itemId, additionalInformationId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
        verifyNoAudit()
      }

      "user enters 'LIC99' as code" in {
        val formData = Json.toJson(AdditionalInformation("LIC99", "description"))
        val result = controller.submitForm(itemId, additionalInformationId)(postRequest(formData))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
        verifyNoAudit()
      }

      "user enters duplicated item" in {
        val duplicatedForm = Json.toJson(additionalInformation2)
        val result = controller.submitForm(itemId, additionalInformationId)(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
        verifyPageInvoked()
        verifyNoAudit()
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user correctly changes document" in {
        val correctForm = Json.obj("code" -> "00000", "description" -> "Change")
        val result = controller.submitForm(itemId, additionalInformationId)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalInformationController.displayPage(itemId)
        verifyPageInvoked(0)

        val savedData = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalInformation)
        savedData mustBe Some(AdditionalInformationData(Yes, Seq(AdditionalInformation("00000", "Change"), additionalInformation2)))
        verifyAudit()
      }

      "user does not change document" in {
        val unchangedForm = Json.toJson(additionalInformation1)
        val result = controller.submitForm(itemId, additionalInformationId)(postRequest(unchangedForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe AdditionalInformationController.displayPage(itemId)
        verifyPageInvoked(0)

        val savedDocuments = theCacheModelUpdated.itemBy(itemId).flatMap(_.additionalInformation)
        savedDocuments mustBe Some(AdditionalInformationData(Yes, Seq(additionalInformation1, additionalInformation2)))
        verifyAudit()
      }
    }
  }
}
