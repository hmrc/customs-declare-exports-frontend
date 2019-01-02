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

package handlers

import base.CustomExportsBaseSpec
import play.api.http.{HeaderNames, Status}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}

class ErrorHandlerSpec extends CustomExportsBaseSpec {

  val errorHandler = new ErrorHandler(appConfig, messagesApi)
  val req = FakeRequest("GET", "/foo")
  "ErrorHandlerSpec" should {
    "standardErrorTemplate" in {
      val result = errorHandler.standardErrorTemplate("Page Title", "Heading", "Message")(FakeRequest()).body

      result must include("Page Title")
      result must include("Heading")
      result must include("Message")
    }
  }
  "resolve error" should {

    "handle no active session authorisation exception" in {
      val res = errorHandler.resolveError(req, new NoActiveSession("A user is not logged in") {})
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(
        Some("/gg/sign-in?continue=%2Ffoo&origin=customs-declare-exports-frontend")
      )
    }

    "handle insufficient enrolments authorisation exception" in {
      val res = errorHandler.resolveError(req, new InsufficientEnrolments("HMRC-CUS-ORG"))
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some("/customs-declare-exports/unauthorised"))
    }

  }

}
