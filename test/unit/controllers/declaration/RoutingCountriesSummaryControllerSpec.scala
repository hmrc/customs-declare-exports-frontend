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

import controllers.declaration.RoutingCountriesSummaryController
import forms.declaration.RoutingQuestionYesNo
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.model.Country
import unit.base.ControllerSpec
import views.html.declaration.destinationCountries.{remove_routing_country, routing_countries_summary}

class RoutingCountriesSummaryControllerSpec extends ControllerSpec {

  val mockRoutingCountriesSummaryPage = mock[routing_countries_summary]
  val mockRoutingRemovePage = mock[remove_routing_country]

  val controller = new RoutingCountriesSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockRoutingCountriesSummaryPage,
    mockRoutingRemovePage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockRoutingCountriesSummaryPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockRoutingRemovePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRoutingCountriesSummaryPage, mockRoutingRemovePage)

    super.afterEach()
  }

  def theResponseSummaryForm: Form[Boolean] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(mockRoutingCountriesSummaryPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  def theCountryToRemove: Country = {
    val captor = ArgumentCaptor.forClass(classOf[Country])
    verify(mockRoutingRemovePage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  "Routing Country summary controller" should {

    "return 200 (OK) for display page method" when {

      "declaration type is different than Supplementary and cache contains countries" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) mustBe OK
        theResponseSummaryForm.value mustBe empty
      }
    }

    "return 200 (OK) for display remove country page" when {

      "user try to remove country that exists in cache" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq("PL", "GB"))))

        val result = controller.displayRemoveCountryPage(Mode.Normal, "PL")(getRequest())

        status(result) mustBe OK
        theCountryToRemove.countryCode mustBe "PL"
      }
    }

    "return 303 (SEE_OTHER) for display page method" when {

      "declaration type is Supplementary" in {

        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DestinationCountryController.displayPage()

        verify(mockRoutingCountriesSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
      }

      "declaration type is different than Supplementary but cache doesn't contain countries" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq.empty)))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesController.displayRoutingQuestion()

        verify(mockRoutingCountriesSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
      }
    }

    "deturn 303 (SEE_OTHER) for removing country" when {

      "user try to remove country that didn't added" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq("PL", "GB"))))

        val result = controller.displayRemoveCountryPage(Mode.Normal, "FR")(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      }

      "user remove country that exists in cache" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq("PL", "GB"))))

        val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submitRemoveCountry(Mode.Normal, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      }

      "user decided to not remove country" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq("PL", "GB"))))

        val correctForm = JsObject(Seq("answer" -> JsString("No")))

        val result = controller.submitRemoveCountry(Mode.Normal, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesSummaryController.displayPage()
      }
    }

    "return 400 (BAD_REQUEST) for submit method" when {

      "form is incorrect" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val incorrectAnswer = JsObject(Seq("answer" -> JsString("incorrect")))

        val result = controller.submit(Mode.Normal)(postRequest(incorrectAnswer))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 400 (BAD_REQUEST) for removing country" when {

      "form is incorrect" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val incorrectAnswer = JsObject(Seq("answer" -> JsString("incorrect")))

        val result = controller.submit(Mode.Normal)(postRequest(incorrectAnswer))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to Routing Countries page" when {

      "form is correct and answer is Yes" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val correctAnswer = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submit(Mode.Normal)(postRequest(correctAnswer))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.RoutingCountriesController.displayRoutingCountry()
      }
    }

    "return 303 (SEE_OTHER) and redirect to Location page" when {

      "form is correct and answer is No" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val correctAnswer = JsObject(Seq("answer" -> JsString("No")))

        val result = controller.submit(Mode.Normal)(postRequest(correctAnswer))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.LocationController.displayPage()
      }
    }
  }
}
