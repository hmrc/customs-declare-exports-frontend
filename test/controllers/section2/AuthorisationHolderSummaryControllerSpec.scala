/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.section2

import base.ControllerSpec
import controllers.section2.routes.AuthorisationHolderAddController
import controllers.summary.routes.SectionSummaryController
import forms.common.YesNoAnswer.formId
import forms.common.{Eori, YesNoAnswer}
import forms.section1.AdditionalDeclarationType.arrivedTypes
import forms.section2.authorisationHolder.AuthorisationHolder
import models.DeclarationType._
import models.declaration.EoriSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import play.twirl.api.HtmlFormat.Appendable
import views.html.section2.authorisationHolder.authorisation_holder_summary

class AuthorisationHolderSummaryControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[authorisation_holder_summary]

  val controller = new AuthorisationHolderSummaryController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, mcc, mockPage)

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
    withNewCaching(aDeclaration(withAuthorisationHolders(authorisationHolder)))
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  def theHoldersList: Seq[AuthorisationHolder] = {
    val captor = ArgumentCaptor.forClass(classOf[Seq[AuthorisationHolder]])
    verify(mockPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val authorisationHolder = AuthorisationHolder(Some("ACE"), Some(Eori("GB56523343784324")), Some(EoriSource.OtherEori))
  val id = "ACE-GB56523343784324"

  "AuthorisationHolderSummaryController on displayOutcomePage" should {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {

        "the cache contains one or more holders" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyPageInvoked()

          theHoldersList must be(Seq(authorisationHolder))
        }

        "there are no holders in the cache" in {
          withNewCaching(aDeclarationAfter(request.cacheModel))

          val result = controller.displayPage(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
        }
      }
    }
  }

  "AuthorisationHolderSummaryController on submitForm" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" when {
        "the user submits the page but does not answer with yes or no" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val requestBody = Json.obj(formId -> "")
          val result = controller.submitForm()(postRequest(requestBody))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "the user submits the page answering Yes" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val requestBody = Json.obj(formId -> "Yes")
          val result = controller.submitForm()(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
        }

        "the user submits the page answering Yes in error-fix mode" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val requestBody = Json.obj(formId -> "Yes")
          val result = controller.submitForm()(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
        }
      }
    }

    "re-direct to the next page" when {
      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL, CLEARANCE) { request =>
        "the user submits the page answering No" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val requestBody = Json.obj(formId -> "No")
          val result = controller.submitForm()(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SectionSummaryController.displayPage(2)
        }
      }

      arrivedTypes.foreach { additionalType =>
        s"the user submits the page answering No with a '${additionalType}' declaration" in {
          val request = withRequest(additionalType)
          withNewCaching(aDeclarationAfter(request.cacheModel, withAdditionalDeclarationType(additionalType)))

          val requestBody = Json.obj(formId -> "No")
          val result = controller.submitForm()(postRequest(requestBody))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SectionSummaryController.displayPage(2)
        }
      }
    }
  }
}
