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
import controllers.declaration.routes._
import forms.common.YesNoAnswer.YesNoAnswers
import forms.common.{Eori, YesNoAnswer}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.{await, status, OK}
import play.twirl.api.HtmlFormat
import views.html.declaration.third_party_goods_transportation

class ThirdPartyGoodsTransportationControllerSpec extends ControllerSpec with AuditedControllerSpec with BeforeAndAfterEach {

  private val thirdPartyGoodsTransportationTemplate = mock[third_party_goods_transportation]

  private val controller = new ThirdPartyGoodsTransportationController(
    authenticate = mockAuthAction,
    journeyType = mockJourneyAction,
    navigator = navigator,
    exportsCacheService = mockExportsCacheService,
    mcc = stubMessagesControllerComponents(),
    thirdPartyGoodTransportPage = thirdPartyGoodsTransportationTemplate
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(thirdPartyGoodsTransportationTemplate.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(thirdPartyGoodsTransportationTemplate)
    super.afterEach()
  }

  override protected def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(thirdPartyGoodsTransportationTemplate).apply(captor.capture())(any(), any())
    captor.getValue
  }

  private val standardCacheModel = aStandardDeclaration

  "ThirdPartyGoodsTransportation Controller on GET request" should {

    "return 200 OK and check the cache for Carrier details" in {
      withNewCaching(standardCacheModel)

      val response = controller.displayPage.apply(getRequest())

      status(response) must be(OK)
      verify(mockExportsCacheService).get(any())(any())
      verify(thirdPartyGoodsTransportationTemplate).apply(any())(any(), any())
    }
  }

  "ThirdPartyGoodsTransportation Controller on POST" when {

    val yesAnswer = Json.obj(YesNoAnswer.formId -> YesNoAnswers.yes)
    val noAnswer = Json.obj(YesNoAnswer.formId -> YesNoAnswers.no)

    "we are on standard declaration journey" should {

      "redirect when user answers yes" in {
        withNewCaching(standardCacheModel)

        val result = await(controller.submitPage(postRequest(yesAnswer)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CarrierEoriNumberController.displayPage
        verifyTheCacheIsUnchanged()
        verifyNoAudit()
      }

      "redirect when user answers yes having previously answered no" in {
        withNewCaching(aDeclaration(withCarrierDetails(eori = Some(Eori(eori)))))

        val result = await(controller.submitPage(postRequest(yesAnswer)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe CarrierEoriNumberController.displayPage
        theCacheModelUpdated.parties.carrierDetails mustBe empty
        verifyAudit()
      }

      "update cache and redirect when user answers no" in {
        withNewCaching(standardCacheModel)

        val result = await(controller.submitPage(postRequest(noAnswer)))

        result mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage

        val carrierEoriNumber = theCacheModelUpdated.parties.carrierDetails.get.details.eori.get.value
        carrierEoriNumber mustBe eori
        verifyAudit()
      }

      "return Bad Request if payload is not compatible with model" in {
        withNewCaching(standardCacheModel)

        val body = Json.obj(YesNoAnswer.formId -> "A")
        val result = controller.submitPage()(postRequest(body))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()
      }
    }
  }
}
