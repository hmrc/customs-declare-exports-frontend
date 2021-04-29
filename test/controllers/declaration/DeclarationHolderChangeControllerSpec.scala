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
import controllers.declaration.DeclarationHolderChangeController
import forms.common.Eori
import forms.declaration.DeclarationHolder
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
import views.html.declaration.declarationHolder.declaration_holder_change

class DeclarationHolderChangeControllerSpec extends ControllerSpec with OptionValues {

  val mockAddPage = mock[declaration_holder_change]

  val controller = new DeclarationHolderChangeController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockAddPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockAddPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAddPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, id1)(request))
    theDeclarationHolder
  }

  def theDeclarationHolder: Form[DeclarationHolder] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarationHolder]])
    verify(mockAddPage).apply(any(), any(), captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1) = verify(mockAddPage, times(numberOfTimes)).apply(any(), any(), any())(any(), any())

  val declarationHolder1: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("GB42354735346235")))
  val id1 = "ACE-GB42354735346235"
  val declarationHolder2: DeclarationHolder = DeclarationHolder(Some("ACE"), Some(Eori("FR65435642343253")))

  "DeclarationHolder Change Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" when {
        "display page method is invoked" in {

          withNewCaching(request.cacheModel)

          val result = controller.displayPage(Mode.Normal, id1)(getRequest())

          status(result) mustBe OK
          verifyAddPageInvoked()

          theDeclarationHolder.value mustBe Some(declarationHolder1)
        }

      }

      "return 400 (BAD_REQUEST)" when {
        "user adds invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = Seq("authorisationTypeCode" -> "inva!id", "eori" -> "inva!id")
          val result = controller.submitForm(Mode.Normal, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds duplicate data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder2)))

          val requestBody =
            Seq("authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get, "eori" -> declarationHolder2.eori.map(_.value).get)
          val result = controller.submitForm(Mode.Normal, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds too many codes" in {
          val holders = Seq.fill(99)(declarationHolder1)
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(DeclarationHoldersData(holders))))

          val requestBody =
            Seq("authorisationTypeCode" -> declarationHolder1.authorisationTypeCode.get, "eori" -> declarationHolder1.eori.map(_.value).get)
          val result = controller.submitForm(Mode.Normal, "SOME-EORI")(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }
      }

      "return 303 (SEE_OTHER)" when {
        "user submits valid data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder1)))

          val requestBody =
            Seq("authorisationTypeCode" -> declarationHolder2.authorisationTypeCode.get, "eori" -> declarationHolder2.eori.map(_.value).get)
          val result = controller.submitForm(Mode.Normal, id1)(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe controllers.declaration.routes.DeclarationHolderController.displayPage(Mode.Normal)

          val savedHolder = theCacheModelUpdated.parties.declarationHoldersData
          savedHolder mustBe Some(DeclarationHoldersData(Seq(declarationHolder2)))
        }

      }
    }

  }
}
