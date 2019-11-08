/*
 * Copyright 2019 HM Revenue & Customs
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

import controllers.declaration.OriginationCountryController
import models.Mode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.destinationCountries.origination_country

import scala.concurrent.ExecutionContext.global

class OriginationCountryControllerSpec extends ControllerSpec {

  val originationCountryPage = mock[origination_country]

  val controller = new OriginationCountryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    originationCountryPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(originationCountryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(originationCountryPage)

    super.afterEach()
  }

  "Origination Country Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        withNewCaching(aDeclaration())

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(originationCountryPage).apply(any(), any())(any(), any())
      }

      "display page method is invoked and cache contains data" in {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(originationCountryPage).apply(any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect country" in {

        withNewCaching(aDeclaration())

        val incorrectForm = JsObject(Map("country" -> JsString("incorrect")))

        val result = controller.submit(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to Destination Countries page" when {

      "form is correct" in {

        withNewCaching(aDeclaration(withDestinationCountries()))

        val correctForm = JsObject(Map("country" -> JsString("PL")))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DestinationCountriesController.displayPage()
      }
    }
  }
}
