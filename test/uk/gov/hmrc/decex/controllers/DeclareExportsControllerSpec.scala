/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.decex.config.AppConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DeclareExportsControllerSpec extends UnitSpec with WithFakeApplication {

  val env = Environment.simple()
  val configuration = Configuration.load(env)

  val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  val appConfig = new AppConfig(configuration, env)

  val controller = new DeclareExports(messageApi, appConfig)

  "GET /select-role" should {

    "Go to the 'Select Role' page" in {
      val result = controller.selectRole(FakeRequest("GET", "/select-role"))
      val bodyText = contentAsString(result)

      //I want to select based on id so I think browser tests are the way forward...
      bodyText should include ("Please choose a role")
    }
  }
}
