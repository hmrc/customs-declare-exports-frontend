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
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType._
import models.Mode
import models.declaration.EoriSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.declarationHolder.declaration_holder_summary

class DeclarationHolderSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[declaration_holder_summary]

  val controller = new DeclarationHolderSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockPage
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withDeclarationHolders(declarationHolder)))
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  def theHoldersList: Seq[DeclarationHolder] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[DeclarationHolder]])
    verify(mockPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1) =
    verify(mockPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val declarationHolder: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB56523343784324")), Some(EoriSource.OtherEori))
  val id = "ACE-GB56523343784324"

  "DeclarationHolderSummaryController on displayPage" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "the cache contains one or more holders" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theHoldersList must be(Seq(declarationHolder))
        }

        "there are no holders in the cache" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarationHolderAddController.displayPage(Mode.Normal)
        }
      }
    }
  }

  "DeclarationHolderSummaryController on submitForm" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" when {
        "the user submits the page but does not answer with yes or no" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val requestBody = Seq("yesNo" -> "")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }

      }

      "return 303 (SEE_OTHER)" when {
        "the user submits the page answering Yes" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarationHolderAddController.displayPage(Mode.Normal)
        }
      }
    }

    "re-direct to next page" when {
      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "the user submits the page answering No" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DestinationCountryController.displayPage(Mode.Normal)
        }
      }

      onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "the user submits the page answering No" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DestinationCountryController.displayPage(Mode.Normal)
        }
      }
    }
  }
}
