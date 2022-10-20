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
import controllers.routes.RootController
import forms.common.YesNoAnswer.YesNoAnswers
import forms.declaration.AuthorisationProcedureCodeChoice
import forms.declaration.AuthorisationProcedureCodeChoice.{allProcedureCodes, formFieldName}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{apply => _}
import models.DeclarationType._
import models.ExportsDeclaration
import models.declaration.AuthorisationProcedureCode
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
    await(controller.displayPage()(request))
    theResponseForm
  }

  def theResponseForm: Form[AuthorisationProcedureCodeChoice] = {
    val captor: ArgumentCaptor[Form[AuthorisationProcedureCodeChoice]] = ArgumentCaptor.forClass(classOf[Form[AuthorisationProcedureCodeChoice]])
    verify(authorisationProcedureCodeChoice).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(authorisationProcedureCodeChoice, times(numberOfTimes)).apply(any(), any())(any(), any())

  "AuthorisationProcedureCodeChoiceController.displayPage" should {

    "return 200 (OK)" when {

      "the cached authorisationProcedureCodeChoice is None and" when {

        List(STANDARD, SUPPLEMENTARY, SIMPLIFIED) foreach { declarationType =>
          s"on $declarationType journey" in {
            verify200(withRequestOfType(declarationType).cacheModel, None)
          }
        }

        "on Clearance journey and" when {
          "EntryIntoDeclarantsRecords is 'Yes'" in {
            verify200(withRequestOfType(CLEARANCE, withEntryIntoDeclarantsRecords(YesNoAnswers.yes)).cacheModel, None)
          }
        }
      }

      allProcedureCodes.foreach { choice =>
        s"the cached authorisationProcedureCodeChoice is $choice and" when {
          val modifier = withAuthorisationProcedureCodeChoice(choice)

          List(STANDARD, SUPPLEMENTARY, SIMPLIFIED) foreach { declarationType =>
            s"on $declarationType journey" in {
              verify200(withRequestOfType(declarationType, modifier).cacheModel, choice)
            }
          }

          "on Clearance journey and" when {
            "EntryIntoDeclarantsRecords is 'Yes'" in {
              verify200(withRequestOfType(CLEARANCE, withEntryIntoDeclarantsRecords(YesNoAnswers.yes), modifier).cacheModel, choice)
            }
          }
        }
      }

      def verify200(declaration: ExportsDeclaration, expectedValue: Option[AuthorisationProcedureCodeChoice]): Unit = {
        withNewCaching(declaration)

        val result = controller.displayPage()(getRequest())

        status(result) must be(OK)
        theResponseForm.value mustBe expectedValue
      }
    }

    "redirect to DeclarationHolderRequiredController" when {

      "on Occasional journey" in {
        withNewCaching(withRequestOfType(OCCASIONAL).cacheModel)

        val result = controller.displayPage()(getRequest())

        await(result) mustBe aRedirectToTheNextPage
        thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage()
      }

      "on Clearance journey and" when {
        "it is NOT EntryIntoDeclarantsRecords" in {
          withNewCaching(withRequestOfType(CLEARANCE).cacheModel)

          val result = controller.displayPage()(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage()
        }
      }
    }
  }

  "AuthorisationProcedureCodeChoiceController.submitForm" when {

    s"on Occasional journey and" when {
      AuthorisationProcedureCode.values.foreach { code =>
        s"AuthorisationProcedureCode is '${code}'" should {
          "redirect to the start page" in {
            withNewCaching(withRequestOfType(OCCASIONAL).cacheModel)

            val result = controller.submitForm()(postRequest(Json.obj(formFieldName -> code.toString)))

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(RootController.displayPage().url)
          }
        }
      }
    }

    List(STANDARD, SUPPLEMENTARY, SIMPLIFIED, CLEARANCE) foreach { declarationType =>
      s"on $declarationType journey and" when {

        allProcedureCodes.foreach { choice =>
          s"the radio button selected by the user is '${choice.value}'" should {
            "redirect to the /is-authorisation-required page" in {
              withNewCaching(withRequestOfType(declarationType).cacheModel)

              val result = controller.submitForm()(postRequest(Json.obj(formFieldName -> choice.value.code.toString)))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe routes.DeclarationHolderRequiredController.displayPage()

              verifyPageInvoked(0)
              theCacheModelUpdated.parties.authorisationProcedureCodeChoice mustBe choice
            }
          }
        }

        "the user does not select any radio button" should {
          "return 400 (BAD_REQUEST)" in {
            withNewCaching(withRequestOfType(declarationType).cacheModel)

            val result = controller.submitForm()(postRequest(Json.obj()))

            status(result) must be(BAD_REQUEST)
            verifyPageInvoked()
            verifyTheCacheIsUnchanged()
          }
        }
      }
    }
  }
}
