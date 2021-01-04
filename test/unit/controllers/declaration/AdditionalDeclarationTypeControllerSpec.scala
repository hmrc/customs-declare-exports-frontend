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

import controllers.declaration.AdditionalDeclarationTypeController
import controllers.util.SaveAndContinue
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.AdditionalDeclarationType
import models.DeclarationType.DeclarationType
import models.{DeclarationType, Mode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.JsString
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.declaration.additionaldeclarationtype.declaration_type

class AdditionalDeclarationTypeControllerSpec extends ControllerSpec {

  val additionalDeclarationTypePage = mock[declaration_type]

  val controller = new AdditionalDeclarationTypeController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    additionalDeclarationTypePage
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(additionalDeclarationTypePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    reset(additionalDeclarationTypePage)
  }

  def theResponseForm: Form[AdditionalDeclarationType] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[AdditionalDeclarationType]])
    verify(additionalDeclarationTypePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  "Display Page" should {
    "return 200 (OK)" when {
      "cache is empty" when {
        for (decType: DeclarationType <- DeclarationType.values) {
          s"during $decType journey" in {
            withNewCaching(aDeclaration(withType(decType)))

            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
          }
        }
      }

      "cache is populated" when {
        "during supplementary journey" in {
          withNewCaching(
            aDeclaration(withType(DeclarationType.SUPPLEMENTARY), withAdditionalDeclarationType(AdditionalDeclarationType.SUPPLEMENTARY_EIDR))
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "during standard journey" in {
          withNewCaching(
            aDeclaration(withType(DeclarationType.STANDARD), withAdditionalDeclarationType(AdditionalDeclarationType.STANDARD_PRE_LODGED))
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "during simplified journey" in {
          withNewCaching(
            aDeclaration(withType(DeclarationType.SIMPLIFIED), withAdditionalDeclarationType(AdditionalDeclarationType.SIMPLIFIED_FRONTIER))
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "during occasional journey" in {
          withNewCaching(
            aDeclaration(withType(DeclarationType.OCCASIONAL), withAdditionalDeclarationType(AdditionalDeclarationType.OCCASIONAL_FRONTIER))
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }

        "during clearance request" in {
          withNewCaching(
            aDeclaration(withType(DeclarationType.CLEARANCE), withAdditionalDeclarationType(AdditionalDeclarationType.CLEARANCE_FRONTIER))
          )

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }
    }
  }

  "Submit" should {
    "return 400 (BAD_REQUEST) for is invalid" when {
      for (decType: DeclarationType <- DeclarationType.values) {
        s"during $decType journey" in {
          withNewCaching(aDeclaration(withType(decType)))

          val result = controller.submitForm(Mode.Normal)(postRequest(JsString("x")))

          status(result) must be(BAD_REQUEST)
        }
      }
    }

    "Continue to the next page" when {
      "during supplementary journey" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SUPPLEMENTARY)))

        val correctForm = Seq("additionalDeclarationType" -> "Z", SaveAndContinue.toString -> "")
        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsignmentReferencesController.displayPage()
      }

      "during standard journey" in {
        withNewCaching(aDeclaration(withType(DeclarationType.STANDARD)))

        val correctForm = Seq("additionalDeclarationType" -> "D", SaveAndContinue.toString -> "")
        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsignmentReferencesController.displayPage()
      }

      "during simplified journey" in {
        withNewCaching(aDeclaration(withType(DeclarationType.SIMPLIFIED)))

        val correctForm = Seq("additionalDeclarationType" -> "F", SaveAndContinue.toString -> "")
        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsignmentReferencesController.displayPage()
      }

      "during occasional journey" in {
        withNewCaching(aDeclaration(withType(DeclarationType.OCCASIONAL)))

        val correctForm = Seq("additionalDeclarationType" -> "B", SaveAndContinue.toString -> "")
        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsignmentReferencesController.displayPage()
      }

      "during clearance request" in {
        withNewCaching(aDeclaration(withType(DeclarationType.CLEARANCE)))

        val correctForm = Seq("additionalDeclarationType" -> "K", SaveAndContinue.toString -> "")
        val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe controllers.declaration.routes.ConsignmentReferencesController.displayPage()
      }
    }
  }
}
