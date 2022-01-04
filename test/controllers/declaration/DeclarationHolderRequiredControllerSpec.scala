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
import controllers.declaration.routes.{DeclarationHolderAddController, DeclarationHolderSummaryController, DestinationCountryController}
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.declarationHolder.DeclarationHolder
import models.DeclarationType.{CLEARANCE, OCCASIONAL, STANDARD, SUPPLEMENTARY}
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
import views.html.declaration.declarationHolder.declaration_holder_required

class DeclarationHolderRequiredControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[declaration_holder_required]

  val controller = new DeclarationHolderRequiredController(
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
    when(mockPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withDeclarationHolders()))
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  "DeclarationHolder Required Controller" should {

    onJourney(CLEARANCE, OCCASIONAL, STANDARD, SUPPLEMENTARY) { request =>
      "return 200 (OK)" when {
        "display page method (GET) is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "display page method (GET) is invoked and cache contains already one or more authorisations" in {
          val declarationHolder: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB56523343784324")), Some(EoriSource.OtherEori))
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe DeclarationHolderSummaryController.displayPage(Mode.Normal)
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL, STANDARD, SUPPLEMENTARY) { request =>
      "return 400 (BAD_REQUEST)" when {
        "the user submits the page but does not answer with yes or no" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "the user submits the page answering Yes" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe DeclarationHolderAddController.displayPage(Mode.Normal)
        }
      }
    }

    "re-direct to the next question" when {
      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "the user submits the page answering No" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe DestinationCountryController.displayPage(Mode.Normal)
        }
      }

      onJourney(CLEARANCE, OCCASIONAL) { request =>
        "the user submits the page answering No" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe DestinationCountryController.displayPage(Mode.Normal)
        }
      }
    }
  }
}
