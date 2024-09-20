/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.drafts

import base.ControllerWithoutFormSpec
import controllers.routes.SavedDeclarationsController
import models.ExportsDeclaration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{redirectLocation, status}
import play.twirl.api.HtmlFormat
import views.html.drafts.remove_declaration

import scala.concurrent.Future

class RemoveDraftDeclarationControllerSpec extends ControllerWithoutFormSpec {

  private val removeDeclaration = mock[remove_declaration]

  private val controller =
    new RemoveDraftDeclarationController(mockAuthAction, mockVerifiedEmailAction, mockCustomsDeclareExportsConnector, mcc, removeDeclaration)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(removeDeclaration.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = reset(mockCustomsDeclareExportsConnector, removeDeclaration)

  "RemoveDraftDeclarationController.displayPage" should {

    "return 200 (OK)" in {
      val declaration = aDeclaration()
      when(mockCustomsDeclareExportsConnector.findDeclaration(any())(any(), any()))
        .thenReturn(Future.successful(Some(declaration)))

      val result = controller.displayPage(declaration.id)(getRequest())

      status(result) mustBe OK

      val captor: ArgumentCaptor[ExportsDeclaration] = ArgumentCaptor.forClass(classOf[ExportsDeclaration])
      verify(removeDeclaration).apply(captor.capture(), any())(any(), any())
      captor.getValue mustBe declaration
    }

    "redirect to '/saved-declarations'" when {
      "the declaration is NOT found" in {
        when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.displayPage("some id")(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SavedDeclarationsController.displayDeclarations().url)
      }
    }
  }

  "RemoveDraftDeclarationController.removeDeclaration" should {

    "redirect to '/saved-declarations'" when {

      "the user confirms the removal" in {
        when(mockCustomsDeclareExportsConnector.deleteDraftDeclaration(anyString())(any(), any()))
          .thenReturn(Future.successful(()))

        val result = controller.removeDeclaration("some id")(postRequest(Json.obj("remove" -> "true")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SavedDeclarationsController.displayDeclarations().url)
      }

      "the user does not fill the form and" when {
        "the declaration is not found" in {
          when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
            .thenReturn(Future.successful(None))

          val result = controller.removeDeclaration("some id")(postRequest(Json.obj("remove" -> "")))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(SavedDeclarationsController.displayDeclarations().url)
        }
      }
    }

    "not remove the declaration" when {
      "the user does not confirm the removal" in {
        val result = controller.removeDeclaration("some id")(postRequest(Json.obj("remove" -> "false")))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SavedDeclarationsController.displayDeclarations().url)

        verifyNoInteractions(mockCustomsDeclareExportsConnector)
      }
    }

    "return Bad_Request(400)" when {
      "the user does not fill the form and" when {
        "the declaration is found" in {
          val declaration = aDeclaration()
          when(mockCustomsDeclareExportsConnector.findDeclaration(anyString())(any(), any()))
            .thenReturn(Future.successful(Some(declaration)))

          val result = controller.removeDeclaration(declaration.id)(postRequest(Json.obj("remove" -> "")))

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
}
