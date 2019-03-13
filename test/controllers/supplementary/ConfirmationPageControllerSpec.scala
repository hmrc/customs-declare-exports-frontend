/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.supplementary

import base.CustomExportsBaseSpec
import org.scalatest.BeforeAndAfter
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ConfirmationPageControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  private val uri = uriWithContextPath("/declaration/supplementary/confirmation")

  private val lrn = "1234567890"
  private val conversationId = "12QW-34ER-56TY-78UI-90OP"

  private def getRequestWithFlash(uri: String): FakeRequest[AnyContentAsEmpty.type] =
    super
      .getRequest(uri)
      .withFlash("LRN" -> lrn, "ConversationId" -> conversationId)

  before {
    authorizedUser()
  }

  "Confirmation Page Controller on GET" should {

    "return 200 status code" in {

      val result = route(app, getRequest(uri)).get

      status(result) must be(OK)
    }

    "display LRN using Flash banner" in {

      val result = contentAsString(route(app, getRequestWithFlash(uri)).get)

      result must include(lrn)
    }

    "display the link to submission" in {

      val resultAsString = contentAsString(route(app, getRequestWithFlash(uri)).get)

      resultAsString must include(messages("supplementary.confirmation.explanation.linkText"))
      resultAsString must include("<a href=\"/customs-declare-exports/submissions\">")
    }
  }
}
