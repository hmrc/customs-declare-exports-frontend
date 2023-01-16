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

import base.{ControllerSpec, ExportsTestData, MockTaggedAuthCodes}
import controllers.declaration.routes.DeclarationHolderSummaryController
import forms.common.Eori
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.declarationHolder.AuthorizationTypeCodes.{CSE, EXRR}
import forms.declaration.declarationHolder.DeclarationHolder
import forms.declaration.declarationHolder.DeclarationHolder.AuthorisationTypeCodeId
import models.DeclarationType._
import models.declaration.{DeclarationHoldersData, EoriSource}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.{Html, HtmlFormat}
import views.html.declaration.declarationHolder.declaration_holder_add

class DeclarationHolderAddControllerSpec extends ControllerSpec with GivenWhenThen with MockTaggedAuthCodes with OptionValues {

  val mockAddPage = mock[declaration_holder_add]

  val controller = new DeclarationHolderAddController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    taggedAuthCodes,
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
    await(controller.displayPage(request))
    theDeclarationHolder
  }

  def theDeclarationHolder: Form[DeclarationHolder] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarationHolder]])
    verify(mockAddPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyAddPageInvoked(numberOfTimes: Int = 1): Html =
    verify(mockAddPage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val declarationHolder = DeclarationHolder(Some("ACE"), Some(Eori(ExportsTestData.eori)), Some(EoriSource.OtherEori))

  "DeclarationHolder Add Controller" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked" in {
          withNewCaching(request.cacheModel)

          val result = controller.displayPage(getRequest())

          status(result) mustBe OK
          verifyAddPageInvoked()

          theDeclarationHolder.value mustBe None
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user submits no data" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitForm()(postRequestAsFormUrlEncoded())

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user submits invalid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = List("authorisationTypeCode" -> "inva!id", "eori" -> "inva!id")
          val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user submits duplicate data" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(declarationHolder)))

          val requestBody =
            List("authorisationTypeCode" -> declarationHolder.authorisationTypeCode.get, "eori" -> declarationHolder.eori.map(_.value).get)
          val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds too many codes" in {
          val holders = Seq.fill(99)(declarationHolder)
          withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(DeclarationHoldersData(holders))))

          val requestBody =
            List("authorisationTypeCode" -> declarationHolder.authorisationTypeCode.get, "eori" -> declarationHolder.eori.map(_.value).get)
          val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }

        "user adds mutually exclusive data" when {

          "attempted to add EXRR when already having CSE present" in {
            val holder = DeclarationHolder(Some(CSE), Some(Eori(ExportsTestData.eori)), Some(EoriSource.OtherEori))
            withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(holder)))

            val requestBody = List("authorisationTypeCode" -> EXRR, "eori" -> ExportsTestData.eori)
            val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

            status(result) mustBe BAD_REQUEST
            verifyAddPageInvoked()
          }

          "attempted to add CSE when already having EXRR present" in {
            val holder = DeclarationHolder(Some(EXRR), Some(Eori(ExportsTestData.eori)), Some(EoriSource.OtherEori))
            withNewCaching(aDeclarationAfter(request.cacheModel, withDeclarationHolders(holder)))

            val requestBody = List("authorisationTypeCode" -> CSE, "eori" -> ExportsTestData.eori)
            val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

            status(result) mustBe BAD_REQUEST
            verifyAddPageInvoked()
          }
        }
      }

      "return 303 (SEE_OTHER)" when {

        "user submits valid data" in {
          withNewCaching(request.cacheModel)

          val requestBody = List(
            "authorisationTypeCode" -> declarationHolder.authorisationTypeCode.get,
            "eori" -> declarationHolder.eori.map(_.value).get,
            "eoriSource" -> "OtherEori"
          )
          val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe DeclarationHolderSummaryController.displayPage

          val savedHolder = theCacheModelUpdated.parties.declarationHoldersData
          savedHolder mustBe Some(DeclarationHoldersData(List(declarationHolder)))
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
          withNewCaching(aDeclarationAfter(request.cacheModel, withAdditionalDeclarationType(additionalDeclarationType)))

          val requestBody = List("authorisationTypeCode" -> EXRR, "eori" -> ExportsTestData.eori, "eoriSource" -> "OtherEori")
          val result = controller.submitForm()(postRequestAsFormUrlEncoded(requestBody: _*))

          status(result) mustBe BAD_REQUEST
          verifyAddPageInvoked()
        }
      }
    }

    "validateMutuallyExclusiveAuthCodes" when {
      def holder(code: String) = DeclarationHolder(Some(code), None, None)

      def error(code: String) = FormError(AuthorisationTypeCodeId, s"declaration.declarationHolder.${code}.error.exclusive")

      "the user enters a new 'CSE' authorisation and the cache already includes an 'EXRR' one" should {
        "return a FormError" in {
          val result = controller.validateMutuallyExclusiveAuthCodes(Some(holder(CSE)), List(holder(EXRR)))
          result.get mustBe error(CSE)
        }
      }

      "the user enters a new 'EXRR' authorisation and the cache already includes a 'CSE' one" should {
        "return a FormError" in {
          val result = controller.validateMutuallyExclusiveAuthCodes(Some(holder(EXRR)), List(holder(CSE)))
          result.get mustBe error(EXRR)
        }
      }

      "the user does not enter an authorisation code" should {
        "return None" in {
          controller.validateMutuallyExclusiveAuthCodes(None, List(holder(CSE))) mustBe None
          controller.validateMutuallyExclusiveAuthCodes(None, List(holder(EXRR))) mustBe None
        }
      }
    }
  }
}
