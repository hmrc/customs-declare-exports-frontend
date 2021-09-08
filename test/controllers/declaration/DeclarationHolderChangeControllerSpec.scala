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

package controllers.declaration

import base.ControllerSpec
import forms.common.Eori
import forms.declaration.declarationHolder.DeclarationHolder
import mock.ErrorHandlerMocks
import models.Mode
import models.declaration.{DeclarationHoldersData, EoriSource}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, verifyNoInteractions, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.declarationHolder.declaration_holder_change

class DeclarationHolderChangeControllerSpec extends ControllerSpec with OptionValues with ErrorHandlerMocks {

  val mockChangePage = mock[declaration_holder_change]

  val controller = new DeclarationHolderChangeController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockChangePage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockChangePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockChangePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withDeclarationHolders(declarationHolder1)))
    await(controller.displayPage(Mode.Normal, declarationHolder1.id)(request))
    theDeclarationHolder
  }

  def theDeclarationHolder: Form[DeclarationHolder] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarationHolder]])
    verify(mockChangePage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyChangePageInvoked(numberOfTimes: Int = 1) =
    verify(mockChangePage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  val declarationHolder1: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB42354735346235")), Some(EoriSource.UserEori))
  val declarationHolder2: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("FR65435642343253")), Some(EoriSource.OtherEori))

  "DeclarationHolder Change Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {
        "display page method is invoked" in {

          withNewCaching(
            aDeclarationAfter(request.cacheModel, withDeclarationHolders(Some("ACE"), Some(Eori("GB42354735346235")), Some(EoriSource.UserEori)))
          )

          val result = controller.displayPage(Mode.Normal, declarationHolder1.id)(getRequest())

          status(result) mustBe OK
          verifyChangePageInvoked()

          theDeclarationHolder.value mustBe Some(declarationHolder1)
        }
      }

      "return 400 (BAD_REQUEST)" when {
        "display page method is invoked with invalid holderId" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, "invalid")(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockChangePage)
        }

        "submit page method is invoked with invalid holderId" in {
          withNewCaching(request.cacheModel)

          val requestBody =
            Seq(
              "authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get,
              "eori" -> declarationHolder2.eori.map(_.value).get,
              "eoriSource" -> declarationHolder2.eoriSource.map(_.toString).get
            )
          val result = controller.submitForm(Mode.Normal, "invalid")(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockChangePage)
        }

        "user edits with invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("authorisationTypeCode" -> "inva!id", "eori" -> "inva!id", "eoriSource" -> "inva!id")
          val result = controller.submitForm(Mode.Normal, declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
        }

        "user edit leads to duplicate data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder1, declarationHolder2)))

          val requestBody =
            Seq(
              "authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get,
              "eori" -> declarationHolder2.eori.map(_.value).get,
              "eoriSource" -> declarationHolder2.eoriSource.map(_.toString).get
            )
          val result = controller.submitForm(Mode.Normal, declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "user submits valid data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder1)))

          val requestBody =
            Seq(
              "authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get,
              "eori" -> declarationHolder2.eori.map(_.value).get,
              "eoriSource" -> declarationHolder2.eoriSource.map(_.toString).get
            )
          val result = controller.submitForm(Mode.Normal, declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderSummaryController.displayPage(Mode.Normal)

          val savedHolder = theCacheModelUpdated.parties.declarationHoldersData
          savedHolder mustBe Some(DeclarationHoldersData(Seq(declarationHolder2)))
        }
      }
    }
  }
}
