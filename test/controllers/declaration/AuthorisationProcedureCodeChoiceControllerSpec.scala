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
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AuthorisationProcedureCodeChoice
import forms.declaration.AuthorisationProcedureCodeChoice.Choice1040
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{apply => _, _}
import models.DeclarationType._
import models.declaration.AuthorisationProcedureCode
import models.declaration.AuthorisationProcedureCode.Code1040
import models.{ExportsDeclaration, Mode}
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

  "Authorisation Procedure Code Choice Controller" when {

    "the displayPage method is invoked" when {

      "the cache is empty" should {

        onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { request =>
          "return 200 (OK)" in {
            withNewCaching(request.cacheModel)

            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
            theResponseForm.value mustBe empty
          }
        }

        onJourney(CLEARANCE) { request =>
          "it is EntryIntoDeclarantsRecords" should {
            "return 200 (OK)" in {
              withNewCaching(aDeclarationAfter(request.cacheModel, withEntryIntoDeclarantsRecords(YesNoAnswers.yes)))

              val result = controller.displayPage(Mode.Normal)(getRequest())

              status(result) must be(OK)
              theResponseForm.value mustBe empty
            }
          }
        }
      }

      "the cache is not empty" should {

        onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED) { request =>
          "return 200 (OK)" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(Choice1040)))

            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
            theResponseForm.value mustBe Choice1040
          }
        }

        onJourney(CLEARANCE) { request =>
          "it is EntryIntoDeclarantsRecords" should {
            "return 200 (OK)" in {
              withNewCaching(
                aDeclarationAfter(
                  request.cacheModel,
                  withAuthorisationProcedureCodeChoice(Choice1040),
                  withEntryIntoDeclarantsRecords(YesNoAnswers.yes)
                )
              )

              val result = controller.displayPage(Mode.Normal)(getRequest())

              status(result) must be(OK)
              theResponseForm.value mustBe Choice1040
            }
          }
        }
      }

      onJourney(OCCASIONAL) { request =>
        "redirect to DeclarationHolderRequiredController" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withType(request.declarationType)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
        }
      }

      onJourney(CLEARANCE) { request =>
        "it is NOT EntryIntoDeclarantsRecords" should {
          "redirect to DeclarationHolderRequiredController" in {
            withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationProcedureCodeChoice(Choice1040)))

            val result = controller.displayPage(Mode.Normal)(getRequest())

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
          }
        }
      }
    }

    "the submit method is invoked" when {

      onJourney(OCCASIONAL) { request =>
        "return 303 (SEE_OTHER)" in {
          withNewCaching(request.cacheModel)
          val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> Code1040.toString)

          val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
        }
      }

      "the form contains incorrect value" when {
        onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { request =>
          "return 400 (BAD_REQUEST)" in {
            withNewCaching(request.cacheModel)

            val result = controller.submitForm(Mode.Normal)(postRequest(Json.obj()))

            status(result) must be(BAD_REQUEST)
            verifyTheCacheIsUnchanged()
          }
        }
      }

      "the form contains correct value" should {

        onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { request =>
          "return 303 (SEE_OTHER)" when {
            AuthorisationProcedureCode.values.foreach { authorisationProcedureCode =>
              s"AuthorisationProcedureCode equals '${authorisationProcedureCode}'" in {
                withNewCaching(request.cacheModel)
                val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> authorisationProcedureCode.toString)

                val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

                await(result) mustBe aRedirectToTheNextPage
                verify(authorisationProcedureCodeChoice, times(0)).apply(any(), any())(any(), any())
                verify(mockExportsCacheService).update(any[ExportsDeclaration])(any())
              }
            }
          }
        }

        onJourney(STANDARD) { request =>
          "AdditionalDeclarationType is pre-lodged and AuthorisationProcedureCode is 1040" should {
            "redirect to DeclarationHolderRequiredController" in {
              withNewCaching(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED)))
              val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> Code1040.toString)

              val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
            }
          }

          "AdditionalDeclarationType is pre-lodged and AuthorisationProcedureCode is 'Other'" should {
            "redirect to DeclarationHolderRequiredController" in {
              withNewCaching(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED)))
              val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> AuthorisationProcedureCode.CodeOther.toString)

              val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage(Mode.Normal)
            }
          }

          "AdditionalDeclarationType is pre-lodged and AuthorisationProcedureCode is 1007" should {
            "redirect to DeclarationHolderSummaryController" in {
              withNewCaching(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_PRE_LODGED)))
              val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> AuthorisationProcedureCode.Code1007.toString)

              val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.DeclarationHolderSummaryController.displayPage(Mode.Normal)
            }
          }

          AuthorisationProcedureCode.values.foreach { authorisationProcedureCode =>
            s"AdditionalDeclarationType is frontier and AuthorisationProcedureCode is '${authorisationProcedureCode}''" should {
              "redirect to DeclarationHolderSummaryController" in {
                withNewCaching(request.cacheModel.copy(additionalDeclarationType = Some(STANDARD_FRONTIER)))
                val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> authorisationProcedureCode.toString)

                val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe routes.DeclarationHolderSummaryController.displayPage(Mode.Normal)
              }
            }
          }
        }

        onJourney(SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) { request =>
          AuthorisationProcedureCode.values.foreach { authorisationProcedureCode =>
            s"AuthorisationProcedureCode is '${authorisationProcedureCode}'" should {
              "redirect to DeclarationHolderSummaryController" in {
                withNewCaching(request.cacheModel)
                val correctForm = Json.obj(AuthorisationProcedureCodeChoice.formFieldName -> authorisationProcedureCode.toString)

                val result = controller.submitForm(Mode.Normal)(postRequest(correctForm))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe routes.DeclarationHolderSummaryController.displayPage(Mode.Normal)
              }
            }
          }
        }
      }
    }
  }
}
