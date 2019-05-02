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

package config.filters

import config.AppConfig
import play.api.{Application, Configuration, Environment}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class WhitelistFilterSpec extends PlaySpec with GuiceOneServerPerSuite {

  lazy val mockConfig = new MockAppConfig(app.configuration)

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(Configuration("whitelist.enabled" -> true))
      .routes({
        case ("GET", "/customs-declare-exports/start") => Action(Ok("success"))
        case _                                         => Action(Ok("failure"))
      })
      .build()

  "WhitelistFilter" when {

    "supplied with a non-whitelisted IP" should {

      lazy val fakeRequest =
        FakeRequest("GET", "/customs-declare-exports/start").withHeaders("True-Client-IP" -> "127.0.0.2")

      Call(fakeRequest.method, fakeRequest.uri)

      lazy val Some(result) = route(app, fakeRequest)

      "return status of 303" in {
        status(result) mustBe 303
      }
      "redirect to shutter page" in {
        redirectLocation(result) mustBe Some(mockConfig.shutterPage)
      }
    }

    "supplied with a whitelisted IP" should {

      lazy val fakeRequest =
        FakeRequest("GET", "/customs-declare-exports/start").withHeaders("True-Client-IP" -> "127.0.0.1")

      lazy val Some(result) = route(app, fakeRequest)

      "return status of 200" in {
        status(result) mustBe 200
      }

      "return success" in {
        contentAsString(result) mustBe "success"
      }
    }
  }
}

class MockAppConfig(override val runModeConfiguration: Configuration)
    extends AppConfig(runModeConfiguration, Environment.simple()) {
  override lazy val analyticsToken: String = ""
  override lazy val analyticsHost: String = ""
  override lazy val whiteListEnabled: Boolean = false
  override lazy val whitelistIps: Seq[String] = Seq("")
  override lazy val whitelistExcludedPaths: Seq[Call] = Nil
  override lazy val shutterPage: String = "https://www.tax.service.gov.uk/shutter/customs-declare-exports-shutter-page"

}
