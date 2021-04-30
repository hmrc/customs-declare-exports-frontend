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
import controllers.declaration.RoutingCountriesController
import forms.declaration.countries.Country
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.destinationCountries.{country_of_routing, routing_country_question}

class RoutingCountriesControllerSpec extends ControllerSpec {

  val mockRoutingQuestionPage = mock[routing_country_question]
  val mockCountryOfRoutingPage = mock[country_of_routing]

  val controller = new RoutingCountriesController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockRoutingQuestionPage,
    mockCountryOfRoutingPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockRoutingQuestionPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCountryOfRoutingPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRoutingQuestionPage, mockCountryOfRoutingPage)

    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayRoutingQuestion(Mode.Normal, false)(request))
    theRoutingQuestionForm
  }

  def theRoutingQuestionForm: Form[Boolean] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(mockRoutingQuestionPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Routing Countries Controller" should {

    "return 200 (OK) and display page" when {

      "user is coming to the page without fastForward flag" in {

        withNewCaching(aDeclaration(withRoutingQuestion(true), withRoutingCountries()))

        val result = controller.displayRoutingQuestion(Mode.Normal, false)(getRequest())

        status(result) mustBe OK
        theRoutingQuestionForm.value mustBe Some(true)
      }

      "user doesn't have any countries in cache" in {

        withNewCaching(aDeclaration(withRoutingQuestion(false), withoutRoutingCountries()))

        val result = controller.displayRoutingQuestion(Mode.Normal, true)(getRequest())

        status(result) mustBe OK
        theRoutingQuestionForm.value mustBe Some(false)
      }

      "user answer Yes on the Routing Question and try to get routing country page" in {

        withNewCaching(aDeclaration(withRoutingQuestion(), withoutRoutingCountries()))

        val result = controller.displayRoutingCountry(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockCountryOfRoutingPage).apply(any(), any(), any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user put incorrect answer on Routing Question page" in {

        withNewCaching(aDeclaration(withDestinationCountry()))

        val incorrectForm = JsObject(Seq("answer" -> JsString("incorrect")))

        val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }

      "user submitted incorrect country on Routing Countries page" in {

        withNewCaching(aDeclaration(withDestinationCountry()))

        val incorrectForm = JsObject(Seq("countryCode" -> JsString("incorrect")))

        val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }

      "user submitted duplicated country in Routing Countries page" in {

        withNewCaching(aDeclaration(withRoutingCountries(Seq(Country(Some("PL"))))))

        val duplicatedForm = JsObject(Seq("countryCode" -> JsString("PL")))

        val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "user is coming to the page with fastForward flag and countries in the cache" in {

        withNewCaching(aDeclaration(withRoutingCountries()))

        val result = controller.displayRoutingQuestion(Mode.Normal, true)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      }

      "user answered Yes for Routing Question" in {

        withNewCaching(aDeclaration(withDestinationCountry()))

        val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry()
      }

      "user answered No for Routing Question" in {

        withNewCaching(aDeclaration(withDestinationCountry()))

        val correctForm = JsObject(Seq("answer" -> JsString("No")))

        val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.LocationController.displayPage()
      }

      "user try to get Routing Countries page without Routing Question" in {

        withNewCaching(aDeclaration(withoutRoutingQuestion()))

        val result = controller.displayRoutingCountry(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(fastForward = false)
      }

      "user try to get Routing Countries when answered No for Routing Question" in {

        withNewCaching(aDeclaration(withRoutingQuestion(false)))

        val result = controller.displayRoutingCountry(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion(fastForward = false)
      }

      "use submitted correct routing country" in {

        withNewCaching(aDeclaration(withRoutingQuestion()))

        val correctForm = JsObject(Seq("countryCode" -> JsString("GB")))

        val result = controller.submitRoutingCountry(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      }

      "user is during error fixing and the naswer is yes" in {

        withNewCaching(aDeclaration(withDestinationCountry()))

        val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submitRoutingAnswer(Mode.ErrorFix)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry(Mode.ErrorFix)
      }

      "user submitted correct routing country during error fixing" in {

        withNewCaching(aDeclaration(withRoutingQuestion()))

        val correctForm = JsObject(Seq("countryCode" -> JsString("GB")))

        val result = controller.submitRoutingCountry(Mode.ErrorFix)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesSummaryController.displayPage(Mode.ErrorFix)
      }
    }
  }
}
