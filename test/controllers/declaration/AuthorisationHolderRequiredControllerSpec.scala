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

import base.ControllerSpec
import controllers.declaration.routes.{AuthorisationHolderAddController, AuthorisationHolderSummaryController, SectionSummaryController}
import forms.common.{Eori, YesNoAnswer}
import forms.declaration.AuthorisationProcedureCodeChoice.{Choice1007, Choice1040, ChoiceOthers}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType._
import forms.declaration.authorisationHolder.AuthorisationHolder
import models.DeclarationType.{CLEARANCE, OCCASIONAL}
import models.ExportsDeclaration
import models.declaration.EoriSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.declaration.authorisationHolder.authorisation_holder_required

class AuthorisationHolderRequiredControllerSpec extends ControllerSpec with OptionValues {

  val mockPage = mock[authorisation_holder_required]

  val controller = new AuthorisationHolderRequiredController(
    mockAuthAction,
    mockJourneyAction,
    mockExportsCacheService,
    navigator,
    stubMessagesControllerComponents(),
    mockPage
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    authorizedUser()
    when(mockPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockPage)
    super.afterEach()
  }

  override def getFormForDisplayRequest(request: Request[AnyContentAsEmpty.type]): Form[_] = {
    withNewCaching(aDeclaration(withType(OCCASIONAL), withAuthorisationHolders()))
    await(controller.displayPage(request))
    theResponseForm
  }

  def theResponseForm: Form[YesNoAnswer] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[YesNoAnswer]])
    verify(mockPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  private def verifyPageInvoked(numberOfTimes: Int = 1): HtmlFormat.Appendable =
    verify(mockPage, times(numberOfTimes)).apply(any())(any(), any())

  "AuthorisationHolderRequiredController.displayOutcomePage" when {

    List(STANDARD_FRONTIER, STANDARD_PRE_LODGED) foreach { additionalType =>
      s"additional declaration type is $additionalType and" when {
        "AuthorisationProcedureCodeChoice is 'Code1040'" should {
          val declaration = withRequest(additionalType, withAuthorisationProcedureCodeChoice(Choice1040)).cacheModel

          verify200(declaration)
          verify303(declaration)
        }
      }
    }

    List(STANDARD_FRONTIER, STANDARD_PRE_LODGED) foreach { additionalType =>
      s"additional declaration type is $additionalType and" when {
        List(Choice1007, ChoiceOthers).foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '$choice'" should {
            val declaration = withRequest(additionalType, withAuthorisationProcedureCodeChoice(choice)).cacheModel

            if (additionalType == STANDARD_PRE_LODGED && choice == ChoiceOthers) {
              verify200(declaration)
              verify303(declaration)
            } else
              "redirect to the /add-authorisations-required page" in {
                withNewCaching(declaration)
                val result = controller.displayPage(getRequest())
                await(result) mustBe aRedirectToTheNextPage
                thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
              }
          }
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL) { request =>
      "the declaration does not contain any authorisation" should {
        verify200(request.cacheModel)
      }

      verify303(request.cacheModel)
    }

    List(SIMPLIFIED_FRONTIER, SIMPLIFIED_PRE_LODGED, SUPPLEMENTARY_SIMPLIFIED, SUPPLEMENTARY_EIDR) foreach { additionalType =>
      s"the additional declaration type is $additionalType" should {
        "redirect to the /add-authorisations-required page" in {
          withNewCaching(withRequest(additionalType).cacheModel)

          val result = controller.displayPage(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
        }
      }
    }

    def verify200(declaration: ExportsDeclaration): Unit =
      "return 200 (OK)" in {
        withNewCaching(declaration)

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK
        verifyPageInvoked()
      }

    def verify303(declaration: ExportsDeclaration): Unit =
      "but if the declaration contains already one or more authorisations" should {
        "instead redirect to the /authorisations-required page" in {
          val authorisationHolder = AuthorisationHolder(Some("ACE"), Some(Eori("GB56523343784324")), Some(EoriSource.OtherEori))
          withNewCaching(aDeclarationAfter(declaration, withAuthorisationHolders(authorisationHolder)))

          val result = controller.displayPage(getRequest())

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderSummaryController.displayPage
        }
      }

  }

  "AuthorisationHolderRequiredController.submitForm" when {

    List(STANDARD_FRONTIER, STANDARD_PRE_LODGED).foreach { additionalType =>
      s"additional declaration type is '$additionalType' and" when {
        List(Choice1040, ChoiceOthers) foreach { choice =>
          s"AuthorisationProcedureCodeChoice is '${choice.value}'" when {
            if (additionalType == STANDARD_FRONTIER && choice == ChoiceOthers) ()
            else {
              val declaration = withRequest(STANDARD_PRE_LODGED, withAuthorisationProcedureCodeChoice(choice)).cacheModel

              verify303ReturnedOnYes(declaration)
              verify303ReturnedOnNo(declaration)
              verify400(declaration)
            }
          }
        }
      }
    }

    onJourney(CLEARANCE, OCCASIONAL) { request =>
      verify303ReturnedOnYes(request.cacheModel)
      verify303ReturnedOnNo(request.cacheModel)
      verify400(request.cacheModel)
    }

    def verify303ReturnedOnYes(declaration: ExportsDeclaration): Unit =
      "the user submits the page answering Yes" should {
        "redirect to the /add-authorisations-required page" in {
          withNewCaching(declaration)

          val body = Json.obj("yesNo" -> "Yes")
          val result = controller.submitForm()(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe AuthorisationHolderAddController.displayPage
        }
      }

    def verify303ReturnedOnNo(declaration: ExportsDeclaration): Unit =
      "the user submits the page answering No" should {
        "redirect to the /summary-section/2 page" in {
          withNewCaching(declaration)

          val body = Json.obj("yesNo" -> "No")
          val result = controller.submitForm()(postRequest(body))

          await(result) mustBe aRedirectToTheNextPage
          thePageNavigatedTo mustBe SectionSummaryController.displayPage(2)
        }
      }

    def verify400(declaration: ExportsDeclaration): Unit =
      "the user submits the page but does not answer with yes or no" should {
        "return 400 (BAD_REQUEST)" in {
          withNewCaching(declaration)

          val body = Json.obj("yesNo" -> "")
          val result = controller.submitForm()(postRequest(body))

          status(result) mustBe BAD_REQUEST
          verifyPageInvoked()
        }
      }
  }
}
