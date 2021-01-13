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

package unit.controllers.declaration

import controllers.declaration.{routes, DeclarationHolderRequiredController}
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.DeclarationHolder
import models.DeclarationType.{CLEARANCE, OCCASIONAL, SIMPLIFIED, STANDARD, SUPPLEMENTARY}
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
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

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {

        "display page method is invoked and cache is empty" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user does not answer with yes or no" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "Additional item(s) exist in cache" in {
          val declarationHolder: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB56523343784324")))
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarationHolderController.displayPage(Mode.Normal)
        }

        "user submits valid Yes answer" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarationHolderAddController.displayPage(Mode.Normal)
        }
      }
    }

    "re-direct to next question" when {
      onJourney(STANDARD, SUPPLEMENTARY) { request =>
        "user submits valid No answer " in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.OriginationCountryController.displayPage(Mode.Normal)
        }
      }

      onJourney(SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "user submits valid No answer (declarationType: STANDARD)" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DestinationCountryController.displayPage(Mode.Normal)
        }
      }
    }
  }
}
