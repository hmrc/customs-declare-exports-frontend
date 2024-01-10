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
import connectors.CodeListConnector
import controllers.declaration.routes.{LocationOfGoodsController, RoutingCountriesController}
import controllers.helpers.Remove
import controllers.helpers.SequenceIdHelper.valueOfEso
import forms.declaration.countries.{Country => FormCountry}
import models.codes.Country
import models.declaration.RoutingCountry
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.helpers.CountryHelper
import views.html.declaration.destinationCountries.{country_of_routing, routing_country_question}

import scala.collection.immutable.ListMap

class RoutingCountriesControllerSpec extends ControllerSpec with AuditedControllerSpec with GivenWhenThen with OptionValues {

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
  )(ec, mockCodeListConnector, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockRoutingQuestionPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCountryOfRoutingPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockCodeListConnector.getCountryCodes(any()))
      .thenReturn(ListMap("GB" -> Country("United Kingdom", "GB"), "FR" -> Country("France", "FR"), "IR" -> Country("Ireland", "IE")))
  }

  override protected def afterEach(): Unit = {
    reset(mockRoutingQuestionPage, mockCountryOfRoutingPage, mockCodeListConnector)

    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayRoutingQuestion()(request))
    theRoutingQuestionForm
  }

  def theRoutingQuestionForm: Form[Boolean] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Boolean]])
    verify(mockRoutingQuestionPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Routing Countries Controller" should {

    "return 200 (OK) and display page" when {

      "user doesn't have any countries in cache" in {
        withNewCaching(aDeclaration(withRoutingQuestion(false), withoutRoutingCountries))

        val result = controller.displayRoutingQuestion()(getRequest())

        status(result) mustBe OK
        theRoutingQuestionForm.value mustBe Some(false)
      }

      "user answer Yes on the Routing Question and try to get routing country page" in {
        withNewCaching(aDeclaration(withRoutingQuestion(), withoutRoutingCountries))

        val result = controller.displayRoutingCountry()(getRequest())

        status(result) mustBe OK
        verify(mockCountryOfRoutingPage).apply(any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user put incorrect answer on Routing Question page" in {
        withNewCaching(aDeclaration(withDestinationCountry()))

        val incorrectForm = JsObject(Seq("answer" -> JsString("incorrect")))

        val result = controller.submitRoutingAnswer()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()
      }

      "user submitted incorrect country on Routing Countries page" in {
        withNewCaching(aDeclaration(withDestinationCountry()))

        val incorrectForm = JsObject(Seq("countryCode" -> JsString("incorrect")))

        val result = controller.submitRoutingAnswer()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()
      }

      "user submitted duplicated country in Routing Countries page" in {
        withNewCaching(aDeclaration(withRoutingCountries(Seq(FormCountry(Some("PL"))))))

        val duplicatedForm = JsObject(Seq("countryCode" -> JsString("PL")))

        val result = controller.submitRoutingAnswer()(postRequest(duplicatedForm))

        status(result) mustBe BAD_REQUEST
        verifyNoAudit()
      }

      "save and continue in Routing Countries" when {
        "there are no countries in list" in {
          withNewCaching(aDeclaration(withRoutingQuestion()))

          val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(Seq(saveAndContinueActionUrlEncoded): _*))

          status(result) mustBe BAD_REQUEST
          verifyNoAudit()

          verifyTheCacheIsUnchanged()
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "Routing Question" when {
        "Yes" in {
          withNewCaching(aDeclaration(withDestinationCountry()))

          val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

          val result = controller.submitRoutingAnswer()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry
          verifyAudit()
        }

        "No" in {
          withNewCaching(aDeclaration(withDestinationCountry()))

          val correctForm = JsObject(Seq("answer" -> JsString("No")))

          val result = controller.submitRoutingAnswer()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe LocationOfGoodsController.displayPage
          verifyAudit()
        }

        "error fixing and the answer is yes" in {
          withNewCaching(aDeclaration(withDestinationCountry()))

          val correctForm = JsObject(Seq("answer" -> JsString("Yes")))

          val result = controller.submitRoutingAnswer()(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry
          verifyAudit()
        }
      }

      "unauthorised for Routing Countries" when {

        "without Routing Question" in {
          withNewCaching(aDeclaration(withoutRoutingQuestion))

          val result = controller.displayRoutingCountry()(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingQuestion
        }

        "No for Routing Question" in {
          withNewCaching(aDeclaration(withRoutingQuestion(false)))

          val result = controller.displayRoutingCountry()(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingQuestion
        }
      }

      "adding a country" when {
        "use submitted correct routing country" when {

          "there are no existing countries" in {
            withNewCaching(aDeclaration(withRoutingQuestion()))

            val correctForm = Seq("countryCode" -> "GB", addActionUrlEncoded())

            val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry

            verifyCachedRoutingCountries(1, "GB")
          }

          "there are existing countries" in {
            withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries()))

            val correctForm = Seq("countryCode" -> "IE", addActionUrlEncoded())

            val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry

            verifyCachedRoutingCountries(3, "FR", "GB", "IE")
          }
        }
      }

      "removing a country" when {
        val removeAction = (Remove.toString, "FR")

        "country removed not in list" in {
          withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(FormCountry(Some("PL")), FormCountry(Some("GB"))))))

          val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(Seq(removeAction): _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry

          verifyCachedRoutingCountries(2, "PL", "GB")
        }

        "removing country that exists in cache" in {
          withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries(Seq(FormCountry(Some("FR")), FormCountry(Some("IE"))))))

          val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(removeAction))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe RoutingCountriesController.displayRoutingCountry

          verifyCachedRoutingCountries(2, "IE")
        }
      }

      "save and continue" when {
        "country is added to input field" when {

          "there are countries in list" in {
            withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries()))

            val correctForm = Seq("countryCode" -> "IE", saveAndContinueActionUrlEncoded)

            val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe LocationOfGoodsController.displayPage

            verifyCachedRoutingCountries(3, "FR", "GB", "IE")
          }

          "there are no countries in list" in {
            withNewCaching(aDeclaration(withoutRoutingQuestion))

            val correctForm = Seq("countryCode" -> "IE", saveAndContinueActionUrlEncoded)

            val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(correctForm: _*))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe LocationOfGoodsController.displayPage

            verifyCachedRoutingCountries(1, "IE")
          }
        }

        "no country in input field" in {
          val correctForm = Seq("countryCode" -> "", saveAndContinueActionUrlEncoded)

          withNewCaching(aDeclaration(withRoutingQuestion(), withRoutingCountries()))

          val result = controller.submitRoutingCountry()(postRequestAsFormUrlEncoded(correctForm: _*))

          verifyTheCacheIsUnchanged()

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe LocationOfGoodsController.displayPage
          verifyNoAudit()
        }
      }

      def verifyCachedRoutingCountries(expectedSize: Int, expectedCountries: String*): Assertion = {
        val declaration = theCacheModelUpdated
        declaration.containRoutingCountries mustBe true

        val routingCountries = declaration.locations.routingCountries

        routingCountries.zip(expectedCountries).foreach { case (routingCountry, expectedCountry) =>
          routingCountry.country == FormCountry(Some(expectedCountry)) mustBe true
        }
        verifyAudit()
        routingCountries.size mustBe expectedCountries.size
        valueOfEso[RoutingCountry](declaration).value mustBe expectedSize
      }
    }
  }
}
