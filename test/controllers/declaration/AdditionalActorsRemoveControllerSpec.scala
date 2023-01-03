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

import base.ControllerSpec
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.DeclarationAdditionalActors
import models.declaration.DeclarationAdditionalActorsData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.ListItem
import views.html.declaration.additionalActors.additional_actors_remove

class AdditionalActorsRemoveControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[additional_actors_remove]

  val controller =
    new AdditionalActorsRemoveController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      mockPage
    )(ec)

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
    withNewCaching(aDeclaration(withDeclarationAdditionalActors(DeclarationAdditionalActorsData(Seq(additionalActor)))))
    await(controller.displayPage(id)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  def additionalActorCaptor: DeclarationAdditionalActors = {
    val captor = ArgumentCaptor.forClass(classOf[DeclarationAdditionalActors])
    verify(mockPage).apply(any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val additionalActor: DeclarationAdditionalActors = DeclarationAdditionalActors(Some(Eori("GB123456789000")), Some("MF"))
  val id = ListItem.createId(0, additionalActor)

  "AdditionalActors Remove Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache is empty" in {

          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActor)))

          val result = controller.displayPage(id)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          additionalActorCaptor mustBe additionalActor
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActor)))

          val requestBody = Seq("yesNo" -> "invalid")
          val result = controller.submitForm(id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
        }

      }
      "return 303 (SEE_OTHER)" when {
        "user submits 'Yes' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActor)))

          val requestBody = Seq("yesNo" -> "Yes")
          val result = controller.submitForm(id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsSummaryController.displayPage

          theCacheModelUpdated.parties.declarationHoldersData mustBe None
        }

        "user submits 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationAdditionalActors(additionalActor)))

          val requestBody = Seq("yesNo" -> "No")
          val result = controller.submitForm(id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsSummaryController.displayPage

          verifyTheCacheIsUnchanged()
        }
      }
    }
  }
}
