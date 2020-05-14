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

import controllers.declaration.IsExsController
import forms.declaration.IsExs
import models.{DeclarationType, Mode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.is_exs

class IsExsControllerSpec extends ControllerSpec {

  private val isExsPage = mock[is_exs]

  private val controller =
    new IsExsController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, stubMessagesControllerComponents(), isExsPage)(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(isExsPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(isExsPage)

    super.afterEach()
  }

  "IsExsController" should {

    "return 200 (OK)" when {

      "display page method is invoked without data in cache" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withoutIsExs()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
      }

      "display page method is invoked with data in cache" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withIsExs()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "answer is missing" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withDeclarantIsExporter()))

        val emptyForm = Json.toJson(IsExs(""))

        val result = controller.submit(Mode.Normal)(postRequest(emptyForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to Consignor Eori page" when {

      "answer is Yes" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))

        val correctForm = Json.toJson(IsExs("Yes"))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsignorEoriNumberController
          .displayPage(Mode.Normal)
      }
    }

    "return 303 (SEE_OTHER) and redirect to Representative Agent page" when {

      "answer is No and declarant is not an exporter" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withDeclarantIsExporter("No")))

        val correctForm = Json.toJson(IsExs("No"))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RepresentativeAgentController
          .displayPage(Mode.Normal)
      }
    }

    "return 303 (SEE_OTHER) and redirect to Consignee Details page" when {

      "answer is No and declarant is an exporter" in {

        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE), withDeclarantIsExporter()))

        val correctForm = Json.toJson(IsExs("No"))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsigneeDetailsController
          .displayPage(Mode.Normal)
      }
    }
  }
}
