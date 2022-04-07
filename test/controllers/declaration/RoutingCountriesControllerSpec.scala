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
import controllers.declaration.routes.{LocationOfGoodsController, RoutingCountriesController}
import controllers.helpers.{Remove, SaveAndContinue}
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import forms.declaration.countries.{Country => FormCountry}
import models.codes.Country
import views.helpers.CountryHelper
import views.html.declaration.destinationCountries.{country_of_routing, routing_country_question}

import scala.collection.immutable.ListMap

class RoutingCountriesControllerSpec extends ControllerSpec {

  val mockRoutingQuestionPage = mock[routing_country_question]
  val mockCountryOfRoutingPage = mock[country_of_routing]
  val mockCodeListConnector = mock[CodeListConnector]
  val countryHelper = instanceOf[CountryHelper]

  val controller = new RoutingCountriesController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockRoutingQuestionPage,
    mockCountryOfRoutingPage
  )(ec, mockCodeListConnector, countryHelper)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockRoutingQuestionPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCountryOfRoutingPage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any()))
      .thenReturn(ListMap("GB" -> Country("United Kingdom", "GB"), "FR" -> Country("France", "FR"), "IR" -> Country("Ireland", "IE")))
  }

  override protected def afterEach(): Unit = {
    reset(mockRoutingQuestionPage, mockCountryOfRoutingPage, mockCodeListConnector)

    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayRoutingQuestion(Mode.Normal)(request))
    theRoutingQuestionForm
  }

  def theRoutingQuestionForm: Form[Boolean] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(mockRoutingQuestionPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Routing Countries Controller" should {

    "return 200 (OK) and display page" when {

      "user doesn't have any countries in cache" in {

        withNewCaching(aDeclaration(withRoutingQuestion(false), withoutRoutingCountries()))

        val result = controller.displayRoutingQuestion(Mode.Normal)(getRequest())

        status(result) mustBe OK
        theRoutingQuestionForm.value mustBe Some(false)
      }

      "user answer Yes on the Routing Question and try to get routing country page" in {

        withNewCaching(aDeclaration(withRoutingQuestion(), withoutRoutingCountries()))

        val result = controller.displayRoutingCountry(Mode.Normal)(getRequest())

        status(result) mustBe OK
        verify(mockCountryOfRoutingPage).apply(any(), any(), any(), any())(any(), any())
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

        withNewCaching(aDeclaration(withRoutingCountries(Seq(FormCountry(Some("PL"))))))

        val duplicatedForm = JsObject(Seq("countryCode" -> JsString("PL")))

        val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
      }

      "save and continue in Routing Countries" when {

        "there are no countries in list" in {

          withNewCaching(aDeclaration(withRoutingQuestion()))

          val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(Seq(saveAndContinueActionUrlEncoded): _*))

          status(result) mustBe BAD_REQUEST

          verifyTheCacheIsUnchanged()

        }

      }
    }

    "return 303 (SEE_OTHER)" when {

      "Routing Question" when {
        "Yes" in {

          withNewCaching(aDeclaration(withDestinationCountry()))

          val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

          val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry()
        }

        "No" in {

          withNewCaching(aDeclaration(withDestinationCountry()))

          val correctForm = JsObject(Seq("answer" -> JsString("No")))

          val result = controller.submitRoutingAnswer(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe LocationOfGoodsController.displayPage()
        }

        "error fixing and the answer is yes" in {

          withNewCaching(aDeclaration(withDestinationCountry()))

          val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

          val result = controller.submitRoutingAnswer(Mode.ErrorFix)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry(Mode.ErrorFix)
        }
      }

      "unauthorised for Routing Countries" when {

        "without Routing Question" in {

          withNewCaching(aDeclaration(withoutRoutingQuestion()))

          val result = controller.displayRoutingCountry(Mode.Normal)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingQuestion()
        }

        "No for Routing Question" in {

          withNewCaching(aDeclaration(withRoutingQuestion(false)))

          val result = controller.displayRoutingCountry(Mode.Normal)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingQuestion()
        }

      }

      "adding a country" when {
        "use submitted correct routing country" when {
          "there are no existing countries" in {

            withNewCaching(aDeclaration(withRoutingQuestion()))

            val correctForm = Seq("countryCode" -> "GB", addActionUrlEncoded())

            val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry()

            val updatedCountries = theCacheModelUpdated.locations.routingCountries
            theCacheModelUpdated.containRoutingCountries mustBe true
            updatedCountries.contains(FormCountry(Some("GB"))) mustBe true
          }
          "there are existing countries" in {

            withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries()))

            val correctForm = Seq("countryCode" -> "IE", addActionUrlEncoded())

            val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry()

            val updatedCountries = theCacheModelUpdated.locations.routingCountries
            theCacheModelUpdated.containRoutingCountries mustBe true
            updatedCountries.contains(FormCountry(Some("IE"))) mustBe true
            updatedCountries.contains(FormCountry(Some("GB"))) mustBe true
            updatedCountries.contains(FormCountry(Some("FR"))) mustBe true
          }
        }
      }

      "removing a country" when {

        val removeAction = (Remove.toString, "FR")

        "country removed not in list" in {

          withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(FormCountry(Some("PL")), FormCountry(Some("GB"))))))

          val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(Seq(removeAction): _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry()

          val updatedCountries = theCacheModelUpdated.locations.routingCountries
          theCacheModelUpdated.containRoutingCountries mustBe true
          updatedCountries.contains(FormCountry(Some("PL"))) mustBe true
          updatedCountries.contains(FormCountry(Some("GB"))) mustBe true
        }

        "removing country that exists in cache" in {

          withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(FormCountry(Some("GB")), FormCountry(Some("FR"))))))

          val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(removeAction))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry()

          val updatedCountries = theCacheModelUpdated.locations.routingCountries
          theCacheModelUpdated.containRoutingCountries mustBe true
          updatedCountries.contains(FormCountry(Some("FR"))) mustBe false
          updatedCountries.contains(FormCountry(Some("GB"))) mustBe true

        }

      }

      "save and continue" when {

        "country is added to input field" when {
          "there are countries in list" in {

            withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries()))

            val correctForm = Seq("countryCode" -> "IE", saveAndContinueActionUrlEncoded)

            val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe LocationOfGoodsController.displayPage()

            val updatedCountries = theCacheModelUpdated.locations.routingCountries
            theCacheModelUpdated.containRoutingCountries mustBe true
            updatedCountries.contains(FormCountry(Some("IE"))) mustBe true
            updatedCountries.contains(FormCountry(Some("GB"))) mustBe true
          }

          "there are no countries in list" in {

            withNewCaching(aDeclaration(withRoutingQuestion()))

            val correctForm = Seq("countryCode" -> "IE", saveAndContinueActionUrlEncoded)

            val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe LocationOfGoodsController.displayPage()

            val updatedCountries = theCacheModelUpdated.locations.routingCountries
            theCacheModelUpdated.containRoutingCountries mustBe true
            updatedCountries.contains(FormCountry(Some("IE"))) mustBe true
          }
        }

        "there are countries in list" in {

          withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries()))

          val result = controller.submitRoutingCountry(Mode.Normal)(postRequestAsFormUrlEncoded(Seq(saveAndContinueActionUrlEncoded): _*))

          verifyTheCacheIsUnchanged()

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe LocationOfGoodsController.displayPage()

        }

      }

    }
  }
}
