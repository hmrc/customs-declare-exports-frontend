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
import controllers.declaration.routes.{DeclarantDetailsController, DucrChoiceController}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationTypePage.radioButtonGroupId
import models.DeclarationType._
import models.DeclarationType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.additional_declaration_type

class AdditionalDeclarationTypeControllerSpec extends ControllerSpec {

  val additionalDeclarationTypePage = mock[additional_declaration_type]

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
    when(additionalDeclarationTypePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(additionalDeclarationTypePage)
  }

  def theResponseForm: Form[AdditionalDeclarationType] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[AdditionalDeclarationType]])
    verify(additionalDeclarationTypePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage()(request))
    theResponseForm
  }

  "AdditionalDeclarationTypeController.displayPage" should {

    "return 200 (OK)" when {

      "cache is empty and" when {
        DeclarationType.values.foreach { declarationType =>
          s"the journey selected was $declarationType" in {
            withNewCaching(aDeclaration(withType(declarationType)))
            val result = controller.displayPage()(getRequest())
            status(result) must be(OK)
          }
        }
      }

      "cache was already populated and" when {
        AdditionalDeclarationType.values.foreach { additionalType =>
          val declarationType = AdditionalDeclarationType.declarationType(additionalType)
          s"the journey selected was $declarationType and" when {
            s"the AdditionalDeclarationType selected was $additionalType" in {
              withNewCaching(aDeclaration(withType(declarationType), withAdditionalDeclarationType(additionalType)))
              val result = controller.displayPage()(getRequest())
              status(result) must be(OK)
            }
          }
        }
      }
    }
  }

  "AdditionalDeclarationTypeController.submitForm" should {

    "return 400 (BAD_REQUEST)" when {

      DeclarationType.values.foreach { declarationType =>
        s"the journey selected was $declarationType and" when {

          s"no value has been selected" in {
            withNewCaching(aDeclaration(withType(declarationType)))
            val result = controller.submitForm()(postRequest(JsString("")))
            status(result) must be(BAD_REQUEST)
          }

          s"the value selected is not a valid AdditionalDeclarationType" in {
            withNewCaching(aDeclaration(withType(declarationType)))
            val result = controller.submitForm()(postRequest(JsString("x")))
            status(result) must be(BAD_REQUEST)
          }
        }
      }
    }

    "continue to the next page" when {
      AdditionalDeclarationType.values.foreach { additionalType =>
        val declarationType = AdditionalDeclarationType.declarationType(additionalType)
        s"the journey selected was $declarationType and" when {
          s"the AdditionalDeclarationType selected was $additionalType" in {
            withNewCaching(aDeclaration(withType(declarationType), withAdditionalDeclarationType(additionalType)))

            val body = Json.obj(radioButtonGroupId -> additionalType.toString)
            val result = controller.submitForm()(postRequest(body))

            status(result) mustBe SEE_OTHER

            val expectedPage =
              if (declarationType == CLEARANCE) DucrChoiceController.displayPage
              else DeclarantDetailsController.displayPage()

            thePageNavigatedTo mustBe expectedPage
          }
        }
      }
    }
  }
}
