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

import base.{AuditedControllerSpec, ControllerSpec}
import controllers.declaration.routes.{AuthorisationHolderAddController, AuthorisationHolderRequiredController, AuthorisationHolderSummaryController}
import forms.common.YesNoAnswer.Yes
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.STANDARD_PRE_LODGED
import forms.declaration.authorisationHolder.AuthorisationHolder
import mock.ErrorHandlerMocks
import models.DeclarationType._
import models.declaration.{AuthorisationHolders, EoriSource}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, verifyNoInteractions, when}
import org.scalatest.{GivenWhenThen, OptionValues}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import play.twirl.api.HtmlFormat.Appendable
import views.html.declaration.authorisationHolder.authorisation_holder_remove

class AuthorisationHolderRemoveControllerSpec
    extends ControllerSpec with AuditedControllerSpec with ErrorHandlerMocks with GivenWhenThen with OptionValues {

  val mockRemovePage = mock[authorisation_holder_remove]

  val controller = new AuthorisationHolderRemoveController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockRemovePage
  )(ec, auditService)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    setupErrorHandler()
    when(mockRemovePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockRemovePage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withAuthorisationHolders(authorisationHolder)))
    await(controller.displayPage(id)(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockRemovePage).apply(any(), captor.capture())(any(), any())
    captor.getValue
  }

  def theAuthorisationHolder: AuthorisationHolder = {
    val captor = ArgumentCaptor.forClass(classOf[AuthorisationHolder])
    verify(mockRemovePage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def verifyRemovePageInvoked(numberOfTimes: Int = 1): Appendable =
    verify(mockRemovePage, times(numberOfTimes)).apply(any(), any())(any(), any())

  val authorisationHolder: AuthorisationHolder = AuthorisationHolder(Some("ACE"), Some(Eori("GB123456543443")), Some(EoriSource.OtherEori))
  val authorisationHolder_2: AuthorisationHolder = AuthorisationHolder(Some("ACF"), Some(Eori("GB123456543445")), Some(EoriSource.OtherEori))
  val id = authorisationHolder.id

  "AuthorisationHolderRemoveController on displayOutcomePage" must {

    onEveryDeclarationJourney() { request =>
      "return 200 (OK)" that {
        "display page method is invoked and cache is empty" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val result = controller.displayPage(id)(getRequest())

          status(result) mustBe OK
          verifyRemovePageInvoked()

          theAuthorisationHolder mustBe authorisationHolder
        }
      }

      "redirect to /authorisations-required" when {
        "display page method is invoked with invalid holderId" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val result = controller.displayPage("invalid")(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(AuthorisationHolderSummaryController.displayPage.url)

          verifyNoInteractions(mockRemovePage)
        }
      }
    }
  }

  "AuthorisationHolderRemoveController on submitForm" should {

    onEveryDeclarationJourney() { request =>
      "return 400 (BAD_REQUEST)" when {

        "provided with invalid holderId" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val body = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm("invalid")(postRequest(body))

          status(result) mustBe BAD_REQUEST
          verifyNoInteractions(mockRemovePage)
          verifyNoAudit()
        }

        "user submits an invalid answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val body = Json.obj("yesNo" -> "invalid")
          val result = controller.submitForm(id)(postRequest(body))

          status(result) mustBe BAD_REQUEST
          verifyRemovePageInvoked()
          verifyNoAudit()
        }
      }
    }
  }

  "AuthorisationHolderRemoveController on submitForm" should {

    "redirect to the /authorisations-required page" when {

      onEveryDeclarationJourney() { request =>
        "user submits a 'Yes' answer and" when {
          "after removal, cache still contains at least one AuthorisationHolder" in {
            val holdersData = AuthorisationHolders(List(authorisationHolder, authorisationHolder_2), isRequired = Yes)
            withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(holdersData)))

            val body = Json.obj("yesNo" -> "Yes")
            val result = controller.submitForm(id)(postRequest(body))

            await(result) mustBe aRedirectToTheNextPage
            thePageNavigatedTo mustBe AuthorisationHolderSummaryController.displayPage

            And("'isRequired' should be set to None when the journey requires to skip /is-authorisation-required")
            val expectedHoldersData = request.declarationType match {
              case CLEARANCE | OCCASIONAL => AuthorisationHolders(Seq(authorisationHolder_2), Yes)
              case _                      => AuthorisationHolders(Seq(authorisationHolder_2), None)
            }
            theCacheModelUpdated.parties.declarationHoldersData mustBe Some(expectedHoldersData)
            verifyAudit()
          }
        }

        "user submits a 'No' answer" in {
          withNewCaching(aDeclarationAfter(request.cacheModel, withAuthorisationHolders(authorisationHolder)))

          val body = Json.obj("yesNo" -> "No")
          val result = controller.submitForm(id)(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderSummaryController.displayPage

          verifyTheCacheIsUnchanged()
          verifyNoAudit()
        }
      }
    }
  }

  "AuthorisationHolderRemoveController on submitForm" when {

    "user submits a 'Yes' answer and" when {
      val body = Json.obj("yesNo" -> "Yes")

      "after removal, cache contains NO AuthorisationHolders and" when {

        List(STANDARD, SUPPLEMENTARY, SIMPLIFIED).foreach { declarationType =>
          s"journey is $declarationType" should {
            val declaration = withRequestOfType(declarationType).cacheModel

            "redirect to the /add-authorisation-required page" in {
              withNewCaching(aDeclarationAfter(declaration, withAuthorisationHolders(authorisationHolder)))

              val result = controller.submitForm(id)(postRequest(body))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
              theCacheModelUpdated.parties.declarationHoldersData mustBe None
              verifyAudit()
            }
          }
        }

        List(CLEARANCE, OCCASIONAL).foreach { declarationType =>
          s"journey is $declarationType" should {
            val declaration = withRequestOfType(declarationType).cacheModel

            "redirect to the /is-authorisation-required page" in {
              withNewCaching(aDeclarationAfter(declaration, withAuthorisationHolders(authorisationHolder)))

              val result = controller.submitForm(id)(postRequest(body))

              await(result) mustBe aRedirectToTheNextPage
              thePageNavigatedTo mustBe AuthorisationHolderRequiredController.displayPage

              theCacheModelUpdated.parties.declarationHoldersData mustBe None
              verifyAudit()
            }
          }
        }

        List(Choice1040, ChoiceOthers) foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '${choice.value}' and" when {
            "additional declaration type is STANDARD_PRE_LODGED" should {
              val declaration = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(choice)).cacheModel

              "redirect to the /is-authorisation-required page" in {
                withNewCaching(aDeclarationAfter(declaration, withAuthorisationHolders(authorisationHolder)))

                val result = controller.submitForm(id)(postRequest(body))

                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe AuthorisationHolderRequiredController.displayPage

                theCacheModelUpdated.parties.declarationHoldersData mustBe None
                verifyAudit()
              }
            }
          }
        }
      }
    }
  }
}
