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

import base.{AuditedControllerSpec, ControllerSpec, Injector}
import connectors.CodeLinkConnector
import controllers.declaration.routes.NactCodeSummaryController
import forms.declaration.NactCode
import forms.declaration.NactCode.nactCodeKey
import forms.declaration.ZeroRatedForVat._
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.ProcedureCodesData.lowValueDeclaration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.{Assertion, OptionValues}
import play.api.data.Form
import play.api.libs.json.JsString
import play.api.mvc.{AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.zero_rated_for_vat

import scala.concurrent.Future

class ZeroRatedForVatControllerSpec extends ControllerSpec with AuditedControllerSpec with ErrorHandlerMocks with Injector with OptionValues {

  val zeroRatedForVatPage = mock[zero_rated_for_vat]
  val codeLinkConnector = mock[CodeLinkConnector]

  val controller =
    new ZeroRatedForVatController(
      mockAuthAction,
      mockJourneyAction,
      mockExportsCacheService,
      navigator,
      stubMessagesControllerComponents(),
      codeLinkConnector,
      zeroRatedForVatPage
    )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()

    when(zeroRatedForVatPage(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(codeLinkConnector.getValidProcedureCodesForTag(any())).thenReturn(Seq.empty)
  }

  override protected def afterEach(): Unit = {
    reset(zeroRatedForVatPage)
    super.afterEach()
  }

  def theResponseForm: Form[NactCode] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[NactCode]])
    verify(zeroRatedForVatPage)(any(), formCaptor.capture(), any())(any(), any())
    formCaptor.getValue
  }

  val item = anItem(withItemId("id"), withProcedureCodes(additionalProcedureCodes = Seq(lowValueDeclaration)))

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withItem(item)))
    await(controller.displayPage(item.id)(request))
    theResponseForm
  }

  "ZeroRatedForVatController for 'low value' declarations" should {

    onJourney(OCCASIONAL, SIMPLIFIED, STANDARD) { request =>
      val declaration = aDeclarationAfter(request.cacheModel, withItem(item))

      "return 200 (OK)" when {

        "display page method is invoked with empty cache" in {
          withNewCaching(declaration)

          val result = controller.displayPage(item.id)(getRequest())

          status(result) must be(OK)
        }

        "display page method is invoked with data in cache" in {
          val nactCode = NactCode(VatZeroRatedYes)
          withNewCaching(aDeclaration(withItem(anItem(withNactCodes(nactCode)))))

          val result = controller.displayPage(item.id)(getRequest())

          status(result) must be(OK)
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "user provide wrong action" in {
          withNewCaching(declaration)

          val wrongForm = Seq(("zeroRatedForVat", VatZeroRatedYes), ("WrongAction", ""))
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(wrongForm: _*))

          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }

        "incorrect data" in {
          withNewCaching(declaration)

          val wrongForm = Seq(("zeroRatedForVat", ""), saveAndContinueActionUrlEncoded)
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(wrongForm: _*))

          status(result) must be(BAD_REQUEST)
          verifyNoAudit()
        }
      }

      "return 303 (SEE_OTHER)" when {

        "VatZeroRatedYes" in {
          withNewCaching(declaration)

          val correctForm = Seq((nactCodeKey, VatZeroRatedYes), saveAndContinueActionUrlEncoded)
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)
          verifyAudit()
        }

        "VatZeroRatedReduced" in {
          withNewCaching(declaration)

          val correctForm = Seq((nactCodeKey, VatZeroRatedReduced), saveAndContinueActionUrlEncoded)
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)
          verifyAudit()
        }

        "VatZeroRatedExempt" in {
          withNewCaching(declaration)

          val correctForm = Seq((nactCodeKey, VatZeroRatedExempt), saveAndContinueActionUrlEncoded)
          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)
          verifyAudit()
        }

        "VatZeroRatedPaid" in {
          withNewCaching(declaration)
          val correctForm = Seq((nactCodeKey, VatZeroRatedPaid), saveAndContinueActionUrlEncoded)

          val result = controller.submitForm(item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)
          verifyAudit()
        }
      }
    }
  }

  "ZeroRatedForVatController for 'NON-low value' declarations" should {
    val item = anItem(withItemId("id"), withNactExemptionCode(NactCode("Some code")))

    "be redirected to /national-additional-codes-list" when {
      onJourney(OCCASIONAL, SIMPLIFIED) { request =>
        "the 'displayPage' method is invoked" in {
          verifyRedirect(controller.displayPage(item.id)(getRequest()))
        }

        "the 'submitForm' method is invoked" in {
          verifyRedirect(controller.submitForm(item.id)(postRequest(JsString(""))))
        }

        def verifyRedirect(fun: => Future[Result]): Assertion = {
          withNewCaching(aDeclarationAfter(request.cacheModel, withItem(item)))

          await(fun) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe NactCodeSummaryController.displayPage(item.id)

          theCacheModelUpdated.items.head.nactExemptionCode mustBe None
        }
      }
    }
  }
}
