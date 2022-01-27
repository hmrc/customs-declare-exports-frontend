/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.CodeListConnector
import controllers.declaration.routes.{LocationOfGoodsController, RoutingCountriesController, RoutingCountriesSummaryController}
import forms.declaration.countries.Country
import models.{codes, DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.destinationCountries.{change_routing_country, remove_routing_country, routing_countries_summary}

import scala.collection.immutable.ListMap

class RoutingCountriesSummaryControllerSpec extends ControllerSpec {

  val routingCountriesSummaryPage = mock[routing_countries_summary]
  val routingRemovePage = mock[remove_routing_country]
  val changeRoutingPage = mock[change_routing_country]
  val mockCodeListConnector = mock[CodeListConnector]

  val controller = new RoutingCountriesSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    routingCountriesSummaryPage,
    routingRemovePage,
    changeRoutingPage
  )(ec, mockCodeListConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(routingCountriesSummaryPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(routingRemovePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(changeRoutingPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any())).thenReturn(
      ListMap("GB" -> codes.Country("United Kingdom", "GB"), "FR" -> codes.Country("France", "FR"), "PL" -> codes.Country("Poland", "PL"))
    )
  }

  override protected def afterEach(): Unit = {
    reset(routingCountriesSummaryPage, routingRemovePage, changeRoutingPage, mockCodeListConnector)

    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withRoutingCountries()))
    await(controller.displayPage(Mode.Normal)(request))
    theResponseSummaryForm
  }

  def theResponseSummaryForm: Form[Boolean] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(routingCountriesSummaryPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  def theCountryToRemove: codes.Country = {
    val captor = ArgumentCaptor.forClass(classOf[codes.Country])
    verify(routingRemovePage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theChangeCountry: Form[Country] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Country]])
    verify(changeRoutingPage).apply(any(), captor.capture(), any(), any())(any(), any())
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

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq(Country(Some("PL")), Country(Some("GB"))))))

        val result = controller.displayRemoveCountryPage(Mode.Normal, "PL")(getRequest())

        status(result) mustBe OK
        theCountryToRemove.countryCode mustBe "PL"
      }
    }

    "return 200 (OK) for display change routing country page" when {

      "user change country that exists in cache" in {

        withNewCaching(aDeclaration(withRoutingCountries(Seq(Country(Some("PL"))))))

        val result = controller.displayChangeCountryPage(Mode.Normal, "PL")(getRequest())

        status(result) mustBe OK
        theChangeCountry.value.flatMap(_.code) mustBe Some("PL")
      }
    }

    "return 303 (SEE_OTHER) for display page method" when {

      "cache doesn't contain countries" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withoutRoutingCountries()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingQuestion()

        verify(routingCountriesSummaryPage, times(0)).apply(any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER) for removing country" when {

      "user try to remove country that didn't added" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq(Country(Some("PL")), Country(Some("GB"))))))

        val result = controller.displayRemoveCountryPage(Mode.Normal, "FR")(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage()
      }

      "user remove country that exists in cache" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq(Country(Some("PL")), Country(Some("GB"))))))

        val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submitRemoveCountry(Mode.Normal, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage()
      }

      "user decided to not remove country" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq(Country(Some("PL")), Country(Some("GB"))))))

        val correctForm = JsObject(Seq("answer" -> JsString("No")))

        val result = controller.submitRemoveCountry(Mode.Normal, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage()
      }

      "user removed country during error fixing" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries(Seq(Country(Some("PL")), Country(Some("GB"))))))

        val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submitRemoveCountry(Mode.ErrorFix, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage(Mode.ErrorFix)
      }
    }

    "return 303 (SEE_OTHER) for change country" when {

      "user try to change non existing in cache country" in {

        withNewCaching(aDeclaration(withoutRoutingQuestion()))

        val result = controller.displayChangeCountryPage(Mode.Normal, "PL")(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage()
      }

      "user succesfully update a country" in {

        withNewCaching(aDeclaration(withRoutingCountries(Seq(Country(Some("PL"))))))

        val correctForm = JsObject(Seq("countryCode" -> JsString("GB")))

        val result = controller.submitChangeCountry(Mode.Normal, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage()
      }

      "user submit changed country during error fixing" in {

        withNewCaching(aDeclaration(withRoutingCountries(Seq(Country(Some("PL"))))))

        val correctForm = JsObject(Seq("countryCode" -> JsString("GB")))

        val result = controller.submitChangeCountry(Mode.ErrorFix, "PL")(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesSummaryController.displayPage(Mode.ErrorFix)
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

    "return 400 (BAD_REQUEST) for changing country" when {

      "form is incorrect" in {

        withNewCaching(aDeclaration(withRoutingCountries()))

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
        thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry()
      }
    }

    "return 303 (SEE_OTHER) and redirect to Routing Countries page with ErrorFix mode" when {

      "form is correct, answer is Yes and mode is ErrorFix" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val correctAnswer = JsObject(Seq("answer" -> JsString("Yes")))

        val result = controller.submit(Mode.ErrorFix)(postRequest(correctAnswer))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry(Mode.ErrorFix)
      }
    }

    "return 303 (SEE_OTHER) and redirect to Location page" when {

      "form is correct and answer is No" in {

        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD), withRoutingCountries()))

        val correctAnswer = JsObject(Seq("answer" -> JsString("No")))

        val result = controller.submit(Mode.Normal)(postRequest(correctAnswer))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe LocationOfGoodsController.displayPage()
      }
    }
  }

  "Routing countries controller" should {

    "handle duplication during changing the country" in {

      withNewCaching(aDeclaration(withRoutingCountries(Seq(Country(Some("PL")), Country(Some("DZ")), Country(Some("AD"))))))

      val duplicatedCountry = JsObject(Seq("countryCode" -> JsString("PL")))

      val result = controller.submitChangeCountry(Mode.Normal, "DZ")(postRequest(duplicatedCountry))

      status(result) mustBe BAD_REQUEST
    }
  }
}
