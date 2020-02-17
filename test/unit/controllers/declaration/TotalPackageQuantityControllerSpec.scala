/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers.declaration

import controllers.declaration.TotalPackageQuantityController
import forms.declaration.TotalPackageQuantity
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
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
    when(totalPackageQuantity.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    authorizedUser()
  }

  override protected def afterEach(): Unit = {
    Mockito.reset(totalPackageQuantity)
    super.afterEach()
  }

  "Total Package Quantity Controller" must {
    onJourney(STANDARD, SIMPLIFIED)() { declaration =>
      "return 200 (OK)" when {
        "cache is empty" in {
          withNewCaching(declaration)

          val result = controller.displayPage(Mode.Normal).apply(getRequest(declaration))

          status(result) mustBe OK
        }
        "cache is non empty" in {
          withNewCaching(aDeclarationAfter(declaration, withTotalPackageQuantity("1")))

          val result = controller.displayPage(Mode.Normal).apply(getRequest(declaration))

          status(result) mustBe OK
        }
      }
      "return 400 (Bad Request)" when {
        "form is incorrect" in {
          withNewCaching(declaration)

          val result = controller.saveTotalPackageQuantity(Mode.Normal).apply(postRequest(Json.obj("totalPackage" -> "one"), declaration))

          status(result) mustBe BAD_REQUEST
        }
      }
      "return 303 (See Other)" should {
        val correctForm = Json.toJson(TotalPackageQuantity(Some("1")))
        onStandard { declaration =>
          "redirect to next question" in {
            withNewCaching(declaration)
            val result = controller.saveTotalPackageQuantity(Mode.Normal)(postRequest(correctForm))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe controllers.declaration.routes.NatureOfTransactionController.displayPage()
          }
        }
        onSimplified { declaration =>
          "redurect to next question" in {
            withNewCaching(declaration)
            val result = controller.saveTotalPackageQuantity(Mode.Normal)(postRequest(correctForm))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe controllers.declaration.routes.PreviousDocumentsController.displayPage()
          }
        }
      }
    }
  }
}
