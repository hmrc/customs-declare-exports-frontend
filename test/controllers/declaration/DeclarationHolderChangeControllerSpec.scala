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
import forms.common.Eori
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.declarationHolder.AuthorizationTypeCodes.{CSE, EXRR}
import forms.declaration.declarationHolder.DeclarationHolder
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.{DeclarationHoldersData, EoriSource}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import views.html.declaration.declarationHolder.declaration_holder_change

class DeclarationHolderChangeControllerSpec extends ControllerSpec with ErrorHandlerMocks with GivenWhenThen with OptionValues {

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
    await(controller.displayPage(declarationHolder1.id)(request))
    theDeclarationHolder
  }

  def theDeclarationHolder: Form[DeclarationHolder] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarationHolder]])
    verify(mockChangePage).apply(any(), any(), captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyChangePageInvoked(numberOfTimes: Int = 1): Html =
    verify(mockChangePage, times(numberOfTimes)).apply(any(), any(), any(), any())(any(), any())

  val declarationHolder1 = DeclarationHolder(Some("ACE"), Some(Eori("GB42354735346235")), Some(EoriSource.UserEori))
  val declarationHolder2 = DeclarationHolder(Some(CSE), Some(Eori("FR65435642343253")), Some(EoriSource.OtherEori))

  "DeclarationHolder Change Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {
        "display page method is invoked" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder1)))

          val result = controller.displayPage(declarationHolder1.id)(getRequest())

          status(result) mustBe OK
          verifyChangePageInvoked()

          theDeclarationHolder.value mustBe Some(declarationHolder1)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "display page method is invoked with invalid holderId" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage("invalid")(getRequest())

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockChangePage)
        }

        "submit page method is invoked with invalid holderId" in {
          withNewCaching(request.cacheModel)

          val requestBody = List(
            "authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get,
            "eori" -> declarationHolder2.eori.map(_.value).get,
            "eoriSource" -> declarationHolder2.eoriSource.map(_.toString).get
          )

          val result = controller.submitForm("invalid")(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockChangePage)
        }

        "user edits with invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = List("authorisationTypeCode" -> "inva!id", "eori" -> "inva!id", "eoriSource" -> "inva!id")
          val result = controller.submitForm(declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
        }

        "user edit leads to duplicate data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder1, declarationHolder2)))

          val requestBody = List(
            "authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get,
            "eori" -> declarationHolder2.eori.map(_.value).get,
            "eoriSource" -> declarationHolder2.eoriSource.map(_.toString).get
          )
          val result = controller.submitForm(declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "user submits valid data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder1)))

          val requestBody =
            List(
              "authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get,
              "eori" -> declarationHolder2.eori.map(_.value).get,
              "eoriSource" -> declarationHolder2.eoriSource.map(_.toString).get
            )
          val result = controller.submitForm(declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderSummaryController.displayPage()

          val savedHolder = theCacheModelUpdated.parties.declarationHoldersData
          savedHolder mustBe Some(DeclarationHoldersData(List(declarationHolder2)))
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      onJourney(STANDARD, SIMPLIFIED, OCCASIONAL, CLEARANCE) { implicit request =>
        "the user enters 'EXRR' as authorisationTypeCode" in {
          And("the declaration is of type PRE_LODGED")
          val additionalDeclarationType = request.declarationType match {
            case STANDARD   => STANDARD_PRE_LODGED
            case SIMPLIFIED => SIMPLIFIED_PRE_LODGED
            case OCCASIONAL => OCCASIONAL_PRE_LODGED
            case CLEARANCE  => CLEARANCE_PRE_LODGED
          }
          withNewCaching(
            aDeclarationAfter(
              request.cacheModel,
              withAdditionalDeclarationType(additionalDeclarationType),
              withDeclarationHolders(declarationHolder1)
            )
          )

          val requestBody = List("authorisationTypeCode" -> EXRR, "eori" -> "GB42354735346235", "eoriSource" -> "OtherEori")
          val result = controller.submitForm(declarationHolder1.id)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyChangePageInvoked()
        }
      }
    }
  }
}
