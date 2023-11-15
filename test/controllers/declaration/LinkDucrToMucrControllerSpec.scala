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
import controllers.actions.AmendmentDraftFilterSpec
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import models.DeclarationType._
import models.requests.JourneyRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Call, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.link_ducr_to_mucr

class LinkDucrToMucrControllerSpec extends ControllerSpec with AmendmentDraftFilterSpec {

  private val linkDucrToMucrPage = mock[link_ducr_to_mucr]

  val controller = new LinkDucrToMucrController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    linkDucrToMucrPage
  )(ec)

  def nextPageOnTypes: Seq[NextPageOnType] =
    allDeclarationTypes.map(NextPageOnType(_, routes.SectionSummaryController.displayPage(1)))

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(linkDucrToMucrPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(STANDARD)))
    when(linkDucrToMucrPage.apply(any[Form[YesNoAnswer]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(linkDucrToMucrPage)
    super.afterEach()
  }

  "LinkDucrToMucrController on displayOutcomePage" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }

      "display page method is invoked and cache is not empty" in {
        withNewCaching(aDeclaration(withLinkDucrToMucr()))

        val result = controller.displayPage(getRequest())
        status(result) must be(OK)
        verifyPageInvoked
      }
    }
  }

  "LinkDucrToMucrController on submitForm" should {

    "return 303 (SEE_OTHER) and redirect to 'Enter a MUCR' page" when {
      onEveryDeclarationJourney() { implicit request =>
        "answer is 'yes'" in {
          verifyRedirect(YesNoAnswers.yes, routes.MucrController.displayPage)
        }
      }
    }

    "return 303 (SEE_OTHER) and redirect to 'Section Summary' page" when {
      onEveryDeclarationJourney() { implicit request =>
        "answer is 'no'" in {
          verifyRedirect(YesNoAnswers.no, routes.SectionSummaryController.displayPage(1))
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in {
        val incorrectForm = Json.obj("yesNo" -> "wrong")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
      }

      "neither Yes or No have been selected on the page" in {
        val incorrectForm = Json.obj("yesNo" -> "")

        val result = controller.submitForm()(postRequest(incorrectForm))
        status(result) must be(BAD_REQUEST)
        verifyPageInvoked
      }
    }
  }

  private def verifyPageInvoked: HtmlFormat.Appendable =
    verify(linkDucrToMucrPage).apply(any[Form[YesNoAnswer]])(any(), any())

  private def verifyRedirect(yesOrNo: String, call: Call)(implicit request: JourneyRequest[_]): Assertion = {
    withNewCaching(request.cacheModel)
    val correctForm = Json.obj("yesNo" -> yesOrNo)

    val result = controller.submitForm()(postRequest(correctForm))

    status(result) mustBe SEE_OTHER
    thePageNavigatedTo mustBe call
  }
}
