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

import base.ControllerSpec
import controllers.declaration.routes.NatureOfTransactionController
import controllers.routes.RootController
import forms.declaration.TotalPackageQuantity
import models.DeclarationType._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.total_package_quantity

class TotalPackageQuantityControllerSpec extends ControllerSpec {

  private val totalPackageQuantity = mock[total_package_quantity]

  val controller = new TotalPackageQuantityController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    navigator,
    mockExportsCacheService,
    totalPackageQuantity
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(totalPackageQuantity.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    authorizedUser()
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(totalPackageQuantity)
    super.afterEach()
  }

  def theResponseForm: Form[TotalPackageQuantity] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[TotalPackageQuantity]])
    verify(totalPackageQuantity).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(request))
    theResponseForm
  }

  "Total Package Quantity Controller" must {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 200 (OK)" when {

        "cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage.apply(getRequest(request.cacheModel))
          status(result) mustBe OK
        }

        "cache is non empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withTotalPackageQuantity("1")))

          val result = controller.displayPage.apply(getRequest(request.cacheModel))
          status(result) mustBe OK
        }
      }

      "return 400 (Bad Request)" when {
        "form is incorrect" in {
          withNewCaching(request.cacheModel)

          val body = Json.obj("totalPackage" -> "one")
          val result = controller.saveTotalPackageQuantity().apply(postRequest(body, request.cacheModel))
          status(result) mustBe BAD_REQUEST
        }
      }

      "return 303 (See Other) redirect to next question" in {
        withNewCaching(request.cacheModel)

        val correctForm = Json.toJson(TotalPackageQuantity(Some("1")))
        val result = controller.saveTotalPackageQuantity()(postRequest(correctForm, request.cacheModel))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe NatureOfTransactionController.displayPage
      }
    }

    onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "redirect 303 (See Other) to start" in {
        withNewCaching(request.cacheModel)

        val result = controller.displayPage.apply(getRequest(request.cacheModel))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) must contain(RootController.displayPage.url)
      }
    }
  }
}
