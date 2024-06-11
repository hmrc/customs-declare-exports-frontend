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

package controllers.declaration

import base.ExportsTestData.eori
import base.{AuditedControllerSpec, ControllerSpec}
import controllers.declaration.routes.{CarrierEoriNumberController, ConsigneeDetailsController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Eori, YesNoAnswer}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.http.Status.{BAD_REQUEST, SEE_OTHER}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, status, OK}
import play.twirl.api.HtmlFormat
import views.html.declaration.third_party_goods_transportation

class ThirdPartyGoodsTransportationControllerSpec extends ControllerSpec with AuditedControllerSpec {

  private val page = mock[third_party_goods_transportation]

  private val controller =
    new ThirdPartyGoodsTransportationController(mcc, mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, page)(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(auditService, page)
    super.afterEach()
  }

  override protected def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue
  }

  private val declaration = aDeclaration(withCarrierDetails(Some(Eori(eori))))

  "ThirdPartyGoodsTransportation Controller on GET request" should {

    "return 200 OK" in {
      withNewCaching(declaration)

      val response = controller.displayPage.apply(getRequest())

      status(response) mustBe OK
      verify(mockExportsCacheService).get(any())(any())
      verify(page).apply(any())(any(), any())
    }
  }

  "ThirdPartyGoodsTransportation Controller on POST" should {

    "do not change the cache when the user answers yes" in {
      withNewCaching(declaration)

      val yesAnswer = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
      val result = controller.submitPage(postRequest(yesAnswer))

      status(result) mustBe SEE_OTHER

      thePageNavigatedTo mustBe CarrierEoriNumberController.displayPage
      verifyTheCacheIsUnchanged()
      verifyNoAudit()
    }

    "do change the cache when the user answers no" in {
      withNewCaching(declaration)

      val noAnswer = Json.obj(YesNoAnswer.formId -> YesNoAnswers.no)
      val result = controller.submitPage(postRequest(noAnswer))

      status(result) mustBe SEE_OTHER

      thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage
      theCacheModelUpdated.parties.carrierDetails mustBe empty
      verifyAudit()
    }

    "return Bad Request if payload is not compatible with model" in {
      withNewCaching(declaration)

      val wrongAnswer = Json.obj(YesNoAnswer.formId -> "A")
      val result = controller.submitPage(postRequest(wrongAnswer))

      status(result) mustBe BAD_REQUEST
      verifyNoAudit()
    }
  }
}
