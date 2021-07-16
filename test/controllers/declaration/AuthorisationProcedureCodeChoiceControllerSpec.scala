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
import forms.declaration.AuthorisationProcedureCodeChoice
import models.{ExportsDeclaration, Mode}
import models.DeclarationType._
import models.declaration.AuthorisationProcedureCode
import models.declaration.AuthorisationProcedureCode.Code1040
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.authorisation_procedure_code_choice

class AuthorisationProcedureCodeChoiceControllerSpec extends ControllerSpec {

  val authorisationProcedureCodeChoice = mock[authorisation_procedure_code_choice]

  val controller = new AuthorisationProcedureCodeChoiceController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    authorisationProcedureCodeChoice
  )(ec)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    withNewCaching(aDeclaration(withType(STANDARD)))
    when(authorisationProcedureCodeChoice.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(authorisationProcedureCodeChoice)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration())
    await(controller.displayPage(Mode.Normal)(request))
    theResponseForm
  }

  def theResponseForm: Form[AuthorisationProcedureCodeChoice] = {
    val captor: ArgumentCaptor[Form[AuthorisationProcedureCodeChoice]] = ArgumentCaptor.forClass(classOf[Form[AuthorisationProcedureCodeChoice]])
    verify(authorisationProcedureCodeChoice).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Authorisation Procedure Code Choice Controller" must {
    onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { request =>
      "the displayPage method is invoked" when {
        "the cache is empty" should {
          "return 200 (OK)" in {
            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
            theResponseForm.value mustBe empty
          }
        }

        "the cache is not empty" should {
          "return 200 (OK)" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(AuthorisationProcedureCodeChoice(Code1040))))
            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
            theResponseForm.value mustBe Some(AuthorisationProcedureCodeChoice(Code1040))
          }
        }
      }

      "the submit method is invoked" when {
        "the form contains incorrect values" should {
          "return 400 (BAD_REQUEST)" in {
            withNewCaching(request.cacheModel)
            val result = controller.submitForm(Mode.Normal)(postRequest(Json.obj()))

            status(result) must be(BAD_REQUEST)
            verifyTheCacheIsUnchanged()
          }
        }

        "the form contains valid value" should {
          "return 303 (SEE_OTHER)" when {
            AuthorisationProcedureCode.values.foreach { authorisationProcedureCode =>
              s"value equals '${authorisationProcedureCode}'" in {
                withNewCaching(request.cacheModel)
                val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> authorisationProcedureCode.toString)
                val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
                verify(authorisationProcedureCodeChoice, times(0)).apply(any(), any())(any(), any())
                verify(mockExportsCacheService).update(any[ExportsDeclaration])(any())
              }
            }
          }
        }
      }
    }

    onJourney(OCCASIONAL) { request =>
      "the displayPage method is invoked" should {
        "return 303 (SEE_OTHER)" in {
          val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> AuthorisationProcedureCode.Code1040.toString)
          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
        }
      }

      "the submit method is invoked" should {
        "return 303 (SEE_OTHER)" in {
          val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> AuthorisationProcedureCode.Code1040.toString)
          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          await(result) mustBe aRedirectToTheNextPage
        }
      }
    }
  }
}
