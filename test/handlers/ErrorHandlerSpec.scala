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

package handlers
import config.AppConfig
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.inject.Injector
import play.api.test.FakeRequest

class ErrorHandlerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite {

  val injector: Injector = app.injector

  val appConfig: AppConfig = injector.instanceOf[AppConfig]
  val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  val errorHandler = new ErrorHandler(appConfig, messagesApi)

  "ErrorHandlerSpec" should {
    "standardErrorTemplate" in {
      val result = errorHandler.standardErrorTemplate("Page Title", "Heading", "Message")(FakeRequest()).body

      result should include ("Page Title")
      result should include ("Heading")
      result should include ("Message")
    }
  }
}
