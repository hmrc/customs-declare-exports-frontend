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
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.DeclarationAdditionalActors
import models.DeclarationType._
import models.declaration.DeclarationAdditionalActorsData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalActors.additional_actors_summary

class AdditionalActorsSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[additional_actors_summary]

  val controller = new AdditionalActorsSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockPage
  )
  val additionalActorsData = DeclarationAdditionalActorsData(Seq(DeclarationAdditionalActors(Some(Eori("GB56523343784324")), Some("CS"))))
  val id = "ACE-GB56523343784324"

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withDeclarationAdditionalActors(additionalActorsData)))
    await(controller.displayPage()(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  def additionalActorsList: Seq[DeclarationAdditionalActors] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[DeclarationAdditionalActors]])
    verify(mockPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) = verify(mockPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  "AdditionalActors Summary Controller" should {

    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache contains data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActorsData)))

          val result = controller.displayPage()(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          additionalActorsList must be(additionalActorsData.actors)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActorsData)))

          val requestBody = Json.obj("yesNo" -> "invalid")
          val result = controller.submitForm()(postRequest(requestBody))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

      }

      "return 303 (SEE_OTHER)" when {

        "there are no additional actors in the cache" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage()(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsAddController.displayPage()
        }

        "user submits valid Yes answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActorsData)))

          val requestBody = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm()(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsAddController.displayPage()
        }

        "user submits valid Yes answer with error-fix flag" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActorsData)))

          val requestBody = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.ErrorFix)(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsAddController.displayPage(Mode.ErrorFix)
        }
      }
    }

    "re-direct to next question" when {
      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
        "user submits valid No answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActorsData)))

          val requestBody = Json.obj("yesNo" -> "No")
          val result = controller.submitForm()(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AuthorisationProcedureCodeChoiceController.displayPage()
        }
      }
    }
  }
}
