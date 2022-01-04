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
import controllers.helpers.SaveAndContinue
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{apply => _, values => _, _}
import models.DeclarationType._
import models.Mode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.JsString
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
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

  private val additionalDeclarationTypeMapping = Map(
    STANDARD -> STANDARD_PRE_LODGED,
    SUPPLEMENTARY -> SUPPLEMENTARY_EIDR,
    SIMPLIFIED -> SIMPLIFIED_FRONTIER,
    OCCASIONAL -> OCCASIONAL_FRONTIER,
    CLEARANCE -> CLEARANCE_FRONTIER
  )

  "Display Page" should {
    "return 200 (OK)" when {
      "cache is empty" when {
        for (decType: DeclarationType <- values) {
          s"during $decType journey" in {
            withNewCaching(aDeclaration(withType(decType)))

            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
          }
        }
      }

      "cache is populated" when {

        onEveryDeclarationJourney() { request =>
          s"during ${request.declarationType} journey" in {
            withNewCaching(
              aDeclaration(
                withType(request.declarationType),
                withAdditionalDeclarationType(additionalDeclarationTypeMapping(request.declarationType))
              )
            )

            val result = controller.displayPage(Mode.Normal)(getRequest())

            status(result) must be(OK)
          }
        }

        "during supplementary journey" in {
          withNewCaching(aDeclaration(withType(SUPPLEMENTARY), withAdditionalDeclarationType(SUPPLEMENTARY_EIDR)))

          val result = controller.displayPage(Mode.Normal)(getRequest())

          status(result) must be(OK)
        }
      }
    }
  }

  "Submit" should {

    "return 400 (BAD_REQUEST) for is invalid" when {

      onEveryDeclarationJourney() { request =>
        val decType = request.declarationType

        s"during $decType journey" in {
          withNewCaching(request.cacheModel)

          val result = controller.submitForm(Mode.Normal)(postRequest(JsString("x")))

          status(result) must be(BAD_REQUEST)
        }
      }
    }

    "Continue to the next page" when {

      onJourney(STANDARD, SUPPLEMENTARY, SIMPLIFIED, OCCASIONAL) { request =>
        s"during ${request.declarationType} journey" in {
          withNewCaching(request.cacheModel)

          val additionalDeclarationType = additionalDeclarationTypeMapping(request.declarationType)
          val correctForm = Seq("additionalDeclarationType" -> additionalDeclarationType.toString, SaveAndContinue.toString -> "")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe controllers.declaration.routes.DeclarantDetailsController.displayPage()
        }
      }

      onClearance { request =>
        s"during ${request.declarationType} journey" in {
          withNewCaching(request.cacheModel)

          val additionalDeclarationType = additionalDeclarationTypeMapping(request.declarationType)
          val correctForm = Seq("additionalDeclarationType" -> additionalDeclarationType.toString, SaveAndContinue.toString -> "")
          val result = controller.submitForm(Mode.Normal)(postRequestAsFormUrlEncoded(correctForm: _*))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo mustBe controllers.declaration.routes.ConsignmentReferencesController.displayPage()
        }
      }
    }
  }
}
