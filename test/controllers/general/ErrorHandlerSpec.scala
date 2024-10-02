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

package controllers.general

import base.{Injector, UnitWithMocksSpec}
import config.AppConfig
import controllers.general.routes.UnauthorisedController
import models.AuthKey.enrolment
import models.UnauthorisedReason.{UserIsAgent, UserIsNotEnrolled}
import org.jsoup.Jsoup
import org.scalatest.{Assertion, OptionValues}
import play.api.Configuration
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import tools.Stubs
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession, UnsupportedAffinityGroup}
import views.html.general.error_template

import java.net.URLEncoder
import scala.concurrent.ExecutionContext.global

class ErrorHandlerSpec extends UnitWithMocksSpec with Stubs with OptionValues with Injector {

  override val configuration: Configuration = Configuration(
    "urls.login" -> "http://localhost:9949/auth-login-stub/gg-sign-in",
    "urls.loginContinue" -> "http://localhost:6791/customs-declare-exports/choice"
  )

  private val errorPage = instanceOf[error_template]
  private val appConfig = instanceOf[AppConfig]
  private val messagesApi = instanceOf[MessagesApi]

  private val request = FakeRequest("GET", "/foo")

  val errorHandler = new ErrorHandler(messagesApi, errorPage)(appConfig, global)

  def urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

  "ErrorHandler.standardErrorTemplate" should {

    "include default title, heading and message" in {
      val html = errorHandler.defaultErrorTemplate()(request)
      checkView(html, "There is a problem", "There is a problem with a service", "Try again later")
    }

    "include expected title, heading and message" in {
      val heading = "Error Title"
      val message = "Error Message"
      val html = errorHandler.defaultErrorTemplate("error.unknown", heading, message)(request)
      checkView(html, "Unknown error", heading, message)
    }

    def checkView(html: Html, expectedTitle: String, expectedHeading: String, expectedMessage: String): Assertion = {
      val view = Jsoup.parse(html.body)

      view.getElementsByTag("title").first.text must startWith(expectedTitle)
      view.getElementsByTag("h1").first.text mustBe expectedHeading
      view.getElementsByClass("govuk-body-m").first.text mustBe expectedMessage
    }
  }

  "ErrorHandler" should {

    "handle no active session authorisation exception" in {
      val error = new NoActiveSession("A user is not logged in") {}
      val result = errorHandler.resolveError(request, error)

      status(result) mustBe Status.SEE_OTHER

      val choice = "http://localhost:6791/customs-declare-exports/choice"
      val expectedLocation =
        s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=${urlEncode(choice)}"

      redirectLocation(result) mustBe Some(expectedLocation)
    }

    "handle insufficient enrolments authorisation exception" in {
      val error = InsufficientEnrolments(enrolment)
      val result = errorHandler.resolveError(request, error)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(UnauthorisedController.onPageLoad(UserIsNotEnrolled).url)
    }

    "handle unsupported affinity group exception" in {
      val error = UnsupportedAffinityGroup()
      val result = errorHandler.resolveError(request, error)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(UnauthorisedController.onAgentKickOut(UserIsAgent).url)
    }
  }
}
