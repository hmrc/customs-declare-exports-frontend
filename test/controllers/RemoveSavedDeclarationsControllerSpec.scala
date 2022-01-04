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

package controllers

import base.ControllerWithoutFormSpec
import forms.RemoveDraftDeclaration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.remove_declaration

class RemoveSavedDeclarationsControllerSpec extends ControllerWithoutFormSpec {

  trait SetUp {
    val removeDeclarationPage = mock[remove_declaration]

    val controller = new RemoveSavedDeclarationsController(
      mockAuthAction,
      mockVerifiedEmailAction,
      mockCustomsDeclareExportsConnector,
      stubMessagesControllerComponents(),
      removeDeclarationPage
    )(ec)

    authorizedUser()

    when(removeDeclarationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Submissions controller" should {

    "return 200 (OK)" when {

      "display declaration about to be removed" in new SetUp {

        getDeclaration("123")

        val result = controller.displayPage("123")(getRequest())

        status(result) must be(OK)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "attempting to render a declaration that does not exist" in new SetUp {
        declarationNotFound
        val result = controller.displayPage("UNKNOWN DECLARATION")(getRequest())

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SavedDeclarationsController.displayDeclarations().url))
      }

      "rejecting to delete a draft declaration" in new SetUp {
        getDeclaration("123")

        val body = Json.toJson(RemoveDraftDeclaration(false))
        val result = controller.removeDeclaration("123")(deleteRequest(body))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SavedDeclarationsController.displayDeclarations().url))
      }

      "deleting a draft declaration" in new SetUp {
        deleteDraftDeclaration()
        getDeclaration("123")

        val body = Json.toJson(RemoveDraftDeclaration(true))
        val result = controller.removeDeclaration("123")(deleteRequest(body))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) must be(Some(controllers.routes.SavedDeclarationsController.displayDeclarations().url))
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "attempting to render a declaration that does not exist" in new SetUp {
        getDeclaration("123")

        val body = JsObject(Map("remove" -> JsString("")))
        val result = controller.removeDeclaration("123")(deleteRequest(body))

        status(result) must be(BAD_REQUEST)
      }
    }
  }
}
