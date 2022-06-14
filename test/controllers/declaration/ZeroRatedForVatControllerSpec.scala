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

import base.{ControllerSpec, Injector}
import forms.declaration.NactCode.{VatZeroRatedExempt, VatZeroRatedPaid, VatZeroRatedReduced, VatZeroRatedYes}
import forms.declaration.{NactCode, ZeroRatedForVat}
import mock.ErrorHandlerMocks
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additionalActors.zero_rated_for_vat

class ZeroRatedForVatControllerSpec extends ControllerSpec with ErrorHandlerMocks with Injector {

  val zeroRatedForVatPage = mock[zero_rated_for_vat]

  val id = "id"
  val item = anItem(withItemId(id))
  val nactCode = NactCode(VatZeroRatedYes)
  val declarationWithZeroRated = aDeclaration(withItem(anItem(withNactCodes(nactCode))))

  val controller =
    new ZeroRatedForVatController(mockAuthAction, mockJourneyAction, mockExportsCacheService, navigator, stubMessagesControllerComponents())(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    setupErrorHandler()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

    when(zeroRatedForVatPage(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(zeroRatedForVatPage)
    super.afterEach()
  }

  def theResponseForm: Form[ZeroRatedForVat] = {
    val formCaptor = ArgumentCaptor.forClass(classOf[Form[ZeroRatedForVat]])
    verify(zeroRatedForVatPage)(any(), any(), formCaptor.capture())(any(), any())
    formCaptor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal, item.id)(request))
    theResponseForm
  }

  "Declaration Additional Actors controller" should {

    "return 200 (OK)" when {

      "display page method is invoked with empty cache" in {

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked with data in cache" in {

        withNewCaching(declarationWithZeroRated)

        val result = controller.displayPage(Mode.Normal, item.id)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user provide wrong action" in {

        val wrongAction = Seq(("zeroRatedForVat", VatZeroRatedYes), ("WrongAction", ""))

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

      "incorrect data" in {

        val wrongAction = Seq(("zeroRatedForVat", ""), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(wrongAction: _*))

        status(result) must be(BAD_REQUEST)
      }

    }

    "return 303 (SEE_OTHER)" when {

      "VatZeroRatedYes" in {

        val correctForm = Seq(("zeroRatedForVat", VatZeroRatedYes), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(Mode.Normal)
      }

      "VatZeroRatedReduced" in {

        val correctForm = Seq(("zeroRatedForVat", VatZeroRatedReduced), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(Mode.Normal)
      }

      "VatZeroRatedExempt" in {

        val correctForm = Seq(("zeroRatedForVat", VatZeroRatedExempt), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(Mode.Normal)
      }

      "VatZeroRatedPaid" in {

        val correctForm = Seq(("zeroRatedForVat", VatZeroRatedPaid), saveAndContinueActionUrlEncoded)

        val result = controller.submitForm(Mode.Normal, item.id)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.AdditionalActorsSummaryController.displayPage(Mode.Normal)
      }
    }
  }
}
