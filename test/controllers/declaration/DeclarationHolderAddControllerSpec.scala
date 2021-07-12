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
import forms.declaration.declarationHolder.DeclarationHolderAdd
import models.Mode
import models.declaration.DeclarationHoldersData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.declarationHolder.declaration_holder_add

class DeclarationHolderAddControllerSpec extends ControllerSpec with OptionValues {

  val mockAddPage = mock[declaration_holder_add]

  val controller = new DeclarationHolderAddController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockAddPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockAddPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAddPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theDeclarationHolder
  }

  def theDeclarationHolder: Form[DeclarationHolderAdd] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarationHolderAdd]])
    verify(mockAddPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1) =
    verify(mockAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val declarationHolder: DeclarationHolderAdd = DeclarationHolderAdd(Some("ACE"), Some(Eori("GB123456789012")))

  "DeclarationHolder Add Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {

        "display page method is invoked" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) mustBe OK
          verifyAddPageInvoked()

          theDeclarationHolder.value mustBe None
        }

      }

      "return 400 (BAD_REQUEST)" when {

        "user adds invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("authorisationTypeCode" -> "inva!id", "eori" -> "inva!id")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds duplicate data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val requestBody =
            Seq("authorisationTypeCode" -> declarationHolder.authorisationTypeCode.get, "eori" -> declarationHolder.eori.map(_.value).get)
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds too many codes" in {
          val holders = Seq.fill(99)(declarationHolder)
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(DeclarationHoldersData(holders))))

          val requestBody =
            Seq("authorisationTypeCode" -> declarationHolder.authorisationTypeCode.get, "eori" -> declarationHolder.eori.map(_.value).get)
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user submits no data" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded())

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits valid data" in {
          withNewCaching(request.cacheModel)

          val requestBody =
            Seq("authorisationTypeCode" -> declarationHolder.authorisationTypeCode.get, "eori" -> declarationHolder.eori.map(_.value).get)
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)

          val savedHolder = theCacheModelUpdated.parties.declarationHoldersData
          savedHolder mustBe Some(DeclarationHoldersData(Seq(declarationHolder)))
        }

      }
    }
  }
}
