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

package controllers.actions

import base.ControllerWithoutFormSpec
import base.ExportsTestData.{eori, newUser}
import config.{AppConfig, ExternalServicesConfig}
import controllers.{ChoiceController, routes}
import models.UnauthorisedReason.{UrlDirect, UserEoriNotAllowed, UserIsAgent, UserIsNotEnrolled}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.{BearerTokenExpired, InternalError, UnsupportedAffinityGroup}
import views.html.{choice_page, declaration_details}

import java.net.URLEncoder
import scala.concurrent.Future

class AuthActionSpec extends ControllerWithoutFormSpec {

  val page = instanceOf[declaration_details]

  val choicePage = mock[choice_page]
  override val appConfig = mock[AppConfig]
  val externalServicesConfig = mock[ExternalServicesConfig]

  override val mockAuthAction = new AuthActionImpl(mockAuthConnector, new EoriAllowList(Seq(eori)), mcc, metricsMock, appConfig)

  val controller = new ChoiceController(mockAuthAction, mockVerifiedEmailAction, mcc, choicePage)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.loginUrl).thenReturn("/unauthorised")
    when(appConfig.loginContinueUrl).thenReturn("/loginContinueUrl")
    when(choicePage()(any(), any())).thenReturn(HtmlFormat.empty)
    when(appConfig.maybeTdrHashSalt).thenReturn(None)
  }

  val tdrHashSalt = Some("SomeSuperSecret")

  "Auth Action" should {

    "allow user into choice page" when {
      "No allow list is active or user's EORI is on the allow list" in {
        authorizedUser()
        val result = controller.displayPage(getRequest())
        status(result) mustBe OK
      }

      "TDRSecret enrolment is required and correctly supplied by user" in {
        authorizedUser()
        when(appConfig.maybeTdrHashSalt).thenReturn(tdrHashSalt)

        val result = controller.displayPage(getRequest())
        status(result) mustBe OK
      }
    }

    "redirect to login page when a NoActiveSession type exception is thrown" in {
      val loginPageUrl = Some(s"${appConfig.loginUrl}?continue=${URLEncoder.encode(appConfig.loginContinueUrl, "UTF-8")}")
      unauthorizedUser(BearerTokenExpired())

      val result = controller.displayPage(getRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe loginPageUrl
    }

    "redirect to /unauthorised" when {
      "EORI number is missing" in {
        userWithoutEori()

        val result = controller.displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad(UserIsNotEnrolled).url)
      }

      "EORI is not on allow list" in {
        authorizedUser(newUser("GB1234567890", "external1"))

        val result = controller.displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad(UserEoriNotAllowed).url)
      }

      "EORI is on allow list and TDRSecret enrolment is required" when {

        "user does not enter a TDRSecret value" in {
          when(appConfig.maybeTdrHashSalt).thenReturn(tdrHashSalt)
          authorizedUser(newUser(eori, "external1", None))

          val result = controller.displayPage(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad(UserEoriNotAllowed).url)
        }

        "user enters a non-matching TDRSecret value" in {
          when(appConfig.maybeTdrHashSalt).thenReturn(tdrHashSalt)
          authorizedUser(newUser(eori, "external1", Some("IncorrectValue")))

          val result = controller.displayPage(getRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad(UserEoriNotAllowed).url)
        }
      }

      "on other 'AuthorisationException' errors" in {
        unauthorizedUser(InternalError("MissingResponseHeader"))

        val result = controller.displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad(UrlDirect).url)
      }
    }

    "redirect to /you-cannot-use-this-service when user is an Agent" in {
      unauthorizedUser(UnsupportedAffinityGroup())

      val result = controller.displayPage(getRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.UnauthorisedController.onAgentKickOut(UserIsAgent).url)
    }

    "redirect to login page for environment when External Id is missing" in {
      val loginPageUrl = Some(s"${appConfig.loginUrl}?continue=${URLEncoder.encode(appConfig.loginContinueUrl, "UTF-8")}")
      userWithoutExternalId()

      val result = controller.displayPage(getRequest())

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe loginPageUrl
    }

    "propagate the error if exception thrown is not an 'AuthorisationException' instance" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(new RuntimeException("error")))

      val result = controller.displayPage(getRequest())

      intercept[RuntimeException](status(result))
    }
  }
}
