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
import controllers.section2.routes.{ConsigneeDetailsController, ThirdPartyGoodsTransportationController}
import forms.common.YesNoAnswer.YesNoAnswers
import forms.section2.IsExs
import forms.section2.representative.RepresentativeStatus
import models.DeclarationType._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.section2.representative.representative_details_status

class RepresentativeStatusControllerSpec extends ControllerSpec with AuditedControllerSpec with OptionValues {

  val mockPage = mock[representative_details_status]

  val controller =
    new RepresentativeStatusController(mockAuthAction, mockJourneyAction, navigator, mockExportsCacheService, mcc, mockPage)(ec, auditService)

  val statusCode = "2"

  def theResponseForm: Form[RepresentativeStatus] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[RepresentativeStatus]])
    verify(mockPage).apply(any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  def verifyPage(numberOfTimes: Int): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  "Representative Status controller" must {

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
          withNewCaching(aDeclarationAfter(request.cacheModel, withRepresentativeDetails(None, Some(statusCode), None)))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyPage(1)

          theResponseForm.value.flatMap(_.statusCode) mustBe Some(statusCode)
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val incorrectForm = Json.toJson(RepresentativeStatus(Some("invalid")))

          val result = controller.submitForm()(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
          verifyPage(1)
          verifyNoAudit()
        }
      }
    }

    onJourney(SUPPLEMENTARY) { request =>
      "return 303 (SEE_OTHER) and redirect to consignee page" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(RepresentativeStatus(Some(statusCode)))

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage

        verifyPage(0)
        verifyAudit()
      }
    }

    onJourney(STANDARD, SIMPLIFIED, OCCASIONAL) { request =>
      "return 303 (SEE_OTHER) and redirect to third party goods transport page" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(RepresentativeStatus(Some(statusCode)))

        val result = controller.submitForm()(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe ThirdPartyGoodsTransportationController.displayPage

        verifyPage(0)
        verifyAudit()
      }
    }

    onClearance { request =>
      "when user answered 'Yes' to the question of whether this is an EXS" should {
        "return 303 (SEE_OTHER) and redirect to third party goods transport page" in {
          val cachedParties = request.cacheModel.parties.copy(isExs = Some(IsExs(YesNoAnswers.yes)))
          withNewCaching(request.cacheModel.copy(parties = cachedParties))

          val correctForm = Json.toJson(RepresentativeStatus(Some(statusCode)))

          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ThirdPartyGoodsTransportationController.displayPage

          verifyPage(0)
          verifyAudit()
        }
      }

      "when user answered 'No' to the question of whether this is an EXS" should {
        "return 303 (SEE_OTHER) and redirect to consignee page" in {
          val cachedParties = request.cacheModel.parties.copy(isExs = Some(IsExs(YesNoAnswers.no)))
          withNewCaching(request.cacheModel.copy(parties = cachedParties))

          val correctForm = Json.toJson(RepresentativeStatus(Some(statusCode)))

          val result = controller.submitForm()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe ConsigneeDetailsController.displayPage

          verifyPage(0)
          verifyAudit()
        }
      }
    }
  }
}
