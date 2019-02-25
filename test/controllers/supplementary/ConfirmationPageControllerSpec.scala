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

  private val lrn = "1234567890"
  private val conversationId = "12QW-34ER-56TY-78UI-90OP"

  private def getRequestWithFlash(uri: String): FakeRequest[AnyContentAsEmpty.type] =
    super
      .getRequest(uri)
      .withFlash("LRN" -> lrn, "ConversationId" -> conversationId)

  before {
    authorizedUser()
  }

  val confirmationPageUri = uriWithContextPath("/declaration/supplementary/confirmation")
  val rejectionPageUri = uriWithContextPath("/declaration/supplementary/rejection")

  "ConfirmationPageController on display confirmation page" should {

    "return 200 code" in {
      val result = route(app, getRequestWithFlash(confirmationPageUri)).get
      status(result) must be(OK)
    }

    "display the whole content" in {
      val resultAsString = contentAsString(route(app, getRequestWithFlash(confirmationPageUri)).get)

      resultAsString must include(messages("supplementary.confirmation.title"))
      resultAsString must include(messages("supplementary.confirmation.header"))
      resultAsString must include(messages("supplementary.confirmation.info"))
      resultAsString must include(messages("supplementary.confirmation.whatHappensNext"))
      resultAsString must include(messages("supplementary.confirmation.explanation"))
    }

    "display LRN from flash" in {
      val resultAsString = contentAsString(route(app, getRequestWithFlash(confirmationPageUri)).get)
      resultAsString must include(lrn)
    }

    "display a button that links to choice page" in {
      val resultAsString = contentAsString(route(app, getRequestWithFlash(confirmationPageUri)).get)

      resultAsString must include(messages("supplementary.confirmation.submitAnotherDeclaration"))
      resultAsString must include("a href=\"/customs-declare-exports/choice\" role=\"button\" class=\"button\"")
    }

    "display the link to the list of notifications for this submission" in {
      val resultAsString = contentAsString(route(app, getRequestWithFlash(confirmationPageUri)).get)

      resultAsString must include(messages("supplementary.confirmation.explanation.linkText"))
      resultAsString must include("<a href=\"/customs-declare-exports/notifications/" + conversationId + "\">")
    }
  }

  "ConfirmationPageController on display rejected confirmation page" should {
    "return 200 code" in {
      val result = route(app, getRequest(rejectionPageUri)).get
      status(result) must be(OK)
    }

    "display the whole content" in {
      val result = route(app, getRequest(rejectionPageUri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.confirmation.header"))
      stringResult must include(messages("supplementary.confirmation.rejection.header"))
      stringResult must include(messages("supplementary.confirmation.whatHappensNext"))
    }

    "display a button links to choice page" in {
      val result = route(app, getRequest(rejectionPageUri)).get
      val stringResult = contentAsString(result)

      stringResult must include(messages("supplementary.confirmation.submitAnotherDeclaration"))
      stringResult must include("a href=\"/customs-declare-exports/choice\" role=\"button\" class=\"button\"")
    }
  }
}
