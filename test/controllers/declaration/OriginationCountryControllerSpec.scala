/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.declaration.OriginationCountryController
import forms.declaration.countries.Country
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.destinationCountries.origination_country

import scala.concurrent.ExecutionContext.global

class OriginationCountryControllerSpec extends ControllerSpec {

  val originationCountryPage = mock[origination_country]

  val controller = new OriginationCountryController(
    mockAuthAction,
    mockVerifiedEmailAction,
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

  def theResponseForm: Form[Country] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Country]])
    verify(originationCountryPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Origination Country Controller" should {

    onJourney(STANDARD, SUPPLEMENTARY) { request =>
      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          withNewCaching(aDeclaration(withType(request.declarationType)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(originationCountryPage).apply(any(), any())(any(), any())
        }

        "display page method is invoked and cache contains data" in {

          withNewCaching(aDeclaration(withType(request.declarationType), withDestinationCountries()))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verify(originationCountryPage).apply(any(), any())(any(), any())
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form contains incorrect country" in {

          withNewCaching(aDeclaration(withType(request.declarationType)))

          val incorrectForm = JsObject(Map("countryCode" -> JsString("incorrect")))

          val result = controller.submit(Mode.Normal)(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
        }
      }

      "redirect to Destination Country page if form is correct" in {

        withNewCaching(aDeclaration(withType(request.declarationType), withDestinationCountries()))

        val correctForm = JsObject(Map("countryCode" -> JsString("PL")))

        val result = controller.submit(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DestinationCountryController.displayPage()
      }

    }

    onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
      "return 303 (SEE_OTHER)" when {

        "redirect to start if journey is invalid" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal).apply(getRequest(request.cacheModel))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) must contain(controllers.routes.RootController.displayPage().url)
        }

      }

    }

  }
}
