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
import controllers.declaration.routes.DeclarationHolderSummaryController
import forms.common.YesNoAnswer.Yes
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.declarationHolder.DeclarationHolder
import mock.ErrorHandlerMocks
import models.Mode.Normal
import models.declaration.{DeclarationHoldersData, EoriSource}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import play.twirl.api.HtmlFormat.Appendable
import views.html.declaration.declarationHolder.declaration_holder_remove

class DeclarationHolderRemoveControllerSpec extends ControllerSpec with ErrorHandlerMocks with GivenWhenThen with OptionValues {

  val mockRemovePage = mock[declaration_holder_remove]

  val controller = new DeclarationHolderRemoveController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockRemovePage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockRemovePage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withDeclarationHolders(declarationHolder)))
    await(controller.displayPage(Normal, id)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theDeclarationHolder: DeclarationHolder = {
    val captor = ArgumentCaptor.forClass(classOf[DeclarationHolder])
    verify(mockRemovePage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): Appendable =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val declarationHolder: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB123456543443")), Some(EoriSource.OtherEori))
  val declarationHolder_2: DeclarationHolder = DeclarationHolder(Some("ACF"), Some(Eori("GB123456543445")), Some(EoriSource.OtherEori))
  val id = declarationHolder.id

  "DeclarationHolderRemoveController on displayPage" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache is empty" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val result = controller.displayPage(Normal, id)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theDeclarationHolder mustBe declarationHolder
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "display page method is invoked with invalid holderId" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val result = controller.displayPage(Normal, "invalid")(getRequest())

          result.map(_ => ()).recover { case ex => ex.printStackTrace() }

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
        }
      }
    }
  }

  "DeclarationHolderRemoveController on submitForm" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" when {

        "provided with invalid holderId" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val body = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm(Normal, "invalid")(postRequest(body))

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
        }

        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val body = Json.obj("yesNo" -> "invalid")
          val result = controller.submitForm(Normal, id)(postRequest(body))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }
      }
    }
  }

  "DeclarationHolderRemoveController on submitForm" should {

    "redirect to the /authorisations-required page" when {

      onEveryDeclarationJourney() { request =>
        "user submits a 'Yes' answer and" when {
          val body = Json.obj("yesNo" -> "Yes")

          "after removal, cache still contains at least one DeclarationHolder" in {
            And("'isRequired' should be set to None when not STANDARD_PRE_LODGED and not (Code1040 or CodeOther)")
            val holdersData = DeclarationHoldersData(List(declarationHolder, declarationHolder_2), isRequired = Yes)
            withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(holdersData)))

            val result = controller.submitForm(Normal, id)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe DeclarationHolderSummaryController.displayPage(Normal)

            theCacheModelUpdated.parties.declarationHoldersData mustBe Some(DeclarationHoldersData(Seq(declarationHolder_2)))
          }

          "after removal, cache contains NO DeclarationHolders" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

            val result = controller.submitForm(Normal, id)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe DeclarationHolderSummaryController.displayPage(Normal)

            theCacheModelUpdated.parties.declarationHoldersData mustBe None
          }
        }

        "user submits a 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val body = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(Normal, id)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe DeclarationHolderSummaryController.displayPage(Normal)

          verifyTheCacheIsUnchanged()
        }
      }
    }
  }
}
