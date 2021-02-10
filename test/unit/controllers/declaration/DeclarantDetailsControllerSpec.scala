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

package unit.controllers.declaration

import controllers.declaration.DeclarantDetailsController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.DeclarantEoriConfirmation
import forms.declaration.DeclarantEoriConfirmation.isEoriKey
import models.requests.ExportsSessionKeys
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.declarant_details

class DeclarantDetailsControllerSpec extends ControllerSpec {

  private val declarantDetailsPage = mock[declarant_details]

  val controller = new DeclarantDetailsController(
    mockAuthAction,
    mockVerifiedEmailAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    declarantDetailsPage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))
    when(declarantDetailsPage.apply(any[Mode], any[Form[DeclarantEoriConfirmation]])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(declarantDetailsPage)
  }

  def theResponseForm: Form[DeclarantEoriConfirmation] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DeclarantEoriConfirmation]])
    verify(declarantDetailsPage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Declarant Details Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }

      "display page method is invoked and cache is not empty" in {

        withNewCaching(aDeclaration(withDeclarantDetails()))

        val result = controller.displayPage(Mode.Normal)(getRequest())

        status(result) must be(OK)
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form contains incorrect values" in {

        val incorrectForm = JsObject(Map(isEoriKey -> JsString("wrong")))

        val result = controller.submitForm(Mode.Normal)(postRequest(incorrectForm))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER) and redirect to is declarant exporter details page" when {

      "answer is yes" in {

        val correctForm = JsObject(Map(isEoriKey -> JsString(YesNoAnswers.yes)))

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.DeclarantExporterController.displayPage()
      }
    }

    "return 303 (SEE_OTHER) and redirect to Not Eligible page" when {

      "answer is no" in {

        val correctForm = JsObject(Map(isEoriKey -> JsString(YesNoAnswers.no)))

        val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

        session(result).get(ExportsSessionKeys.declarationId) must be(None)
      }
    }
  }
}
