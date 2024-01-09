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
import controllers.declaration.routes.PackageInformationSummaryController
import controllers.helpers.MultipleItemsHelper
import controllers.routes.RootController
import forms.declaration.StatisticalValue
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.ProcedureCodesData.lowValueDeclaration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.statistical_value

class StatisticalValueControllerSpec extends ControllerSpec with AuditedControllerSpec with ErrorHandlerMocks with OptionValues {

  val mockItemTypePage = mock[statistical_value]

  val controller = new StatisticalValueController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockItemTypePage
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockItemTypePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  val itemId = MultipleItemsHelper.generateItemId()

  override protected def afterEach(): Unit = {
    reset(mockItemTypePage)
    super.afterEach()
  }

  def theResponseForm: Form[StatisticalValue] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[StatisticalValue]])
    verify(mockItemTypePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(itemId)(request))
    theResponseForm
  }

  def validateCache(itemType: StatisticalValue): Assertion = {
    val cacheItemType = theCacheModelUpdated.items.head.statisticalValue.getOrElse(StatisticalValue(""))
    cacheItemType mustBe itemType
  }

  "Item Type Controller" should {

    "return 200 (OK)" when {
      onJourney(SUPPLEMENTARY, STANDARD) { request =>
        "item type exists in the cache and item type is defined" in {
          withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId), withStatisticalValue()))))

          val result = controller.displayPage(itemId)(getRequest(request.cacheModel))

          status(result) mustBe OK
          theResponseForm.value mustNot be(empty)
        }

        "item type exists in the cache and item type is not defined" in {
          withNewCaching(aDeclaration(withItem(anItem(withItemId(itemId)))))

          val result = controller.displayPage(itemId)(getRequest(request.cacheModel))

          status(result) mustBe OK
          theResponseForm.value mustBe empty
        }
      }

      onJourney(OCCASIONAL, SIMPLIFIED) { request =>
        "for a 'low value' declaration" in {
          val procedureCodes = withProcedureCodes(additionalProcedureCodes = Seq(lowValueDeclaration))
          val item = anItem(withItemId(itemId), withStatisticalValue(), procedureCodes)
          withNewCaching(aDeclaration(withItem(item)))

          val result = controller.displayPage(itemId)(getRequest(request.cacheModel))

          status(result) mustBe OK
          theResponseForm.value mustNot be(empty)
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {
      onJourney(SUPPLEMENTARY, STANDARD) { request =>
        "invalid data is posted" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(anItem(withItemId(itemId)))))

          val badData = Json.toJson(StatisticalValue("Seven"))

          val result = controller.submitItemType(itemId)(postRequest(badData))

          status(result) mustBe BAD_REQUEST
          verify(mockItemTypePage, times(1)).apply(any(), any())(any(), any())
          verifyNoAudit()
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "invalid data is posted" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(itemId).apply(getRequest(request.cacheModel))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(RootController.displayPage.url)
          verifyNoAudit()
        }
      }

      onJourney(SUPPLEMENTARY, STANDARD) { request =>
        "valid data is posted" in {
          withNewCaching(aDeclaration(withType(request.declarationType), withItem(anItem(withItemId(itemId)))))

          val badData = Json.toJson(StatisticalValue("7"))

          val result = controller.submitItemType(itemId)(postRequest(badData))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe PackageInformationSummaryController.displayPage(itemId)
          verify(mockItemTypePage, times(0)).apply(any(), any())(any(), any())

          validateCache(StatisticalValue("7"))
          verifyAudit()
        }
      }
    }
  }
}
