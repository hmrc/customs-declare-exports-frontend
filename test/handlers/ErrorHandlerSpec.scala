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

package handlers

import base.{Injector, UnitWithMocksSpec}
import config.AppConfig
import models.AuthKey.enrolment
import org.scalatest.OptionValues
import play.api.Configuration
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import tools.Stubs
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession, UnsupportedAffinityGroup}
import views.html.error_template
import controllers.routes
import models.UnauthorisedReason.{UserIsAgent, UserIsNotEnrolled}

import java.net.URLEncoder
import scala.concurrent.Future

class ErrorHandlerSpec extends UnitWithMocksSpec with Stubs with OptionValues with Injector {

  override val configuration: Configuration = Configuration(
    "urls.login" -> "http://localhost:9949/auth-login-stub/gg-sign-in",
    "urls.loginContinue" -> "http://localhost:6791/customs-declare-exports/start"
  )

  val errorPage = instanceOf[error_template]
  val appConfig = instanceOf[AppConfig]
  val request = FakeRequest("GET", "/foo")

  val errorHandler = new ErrorHandler(stubMessagesApi(), errorPage)(appConfig)

  def urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

  "ErrorHandlerSpec" should {

    "standardErrorTemplate" in {

      val result = errorHandler.standardErrorTemplate("title", "heading", "message")(request).body

      result must include("title")
      result must include("heading")
      result must include("message")
    }
  }

  "resolve error" should {

    "handle no active session authorisation exception" in {

      val error = new NoActiveSession("A user is not logged in") {}
      val result = Future.successful(errorHandler.resolveError(request, error))
      val start = "http://localhost:6791/customs-declare-exports/start"

      status(result) mustBe Status.SEE_OTHER

      val expectedLocation =
        s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=${urlEncode(start)}"

      redirectLocation(result) mustBe Some(expectedLocation)
    }

    "handle insufficient enrolments authorisation exception" in {

      val error = InsufficientEnrolments(enrolment)
      val result = Future.successful(errorHandler.resolveError(request, error))

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad(UserIsNotEnrolled).url)
    }

    "handle unsupported affinity group exception" in {
      val error = UnsupportedAffinityGroup()
      val result = Future.successful(errorHandler.resolveError(request, error))

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.UnauthorisedController.onAgentKickOut(UserIsAgent).url)
    }
  }
}
