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

package views.declaration.spec

import java.time.Instant

import base.{ExportsTestData, ViewValidator}
import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import models.requests.{AuthenticatedRequest, JourneyRequest}
import models.{DeclarationStatus, ExportsDeclaration}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, Flash, Request}
import play.api.test.FakeRequest
import utils.FakeRequestCSRFSupport._

trait ViewSpec extends PlaySpec with GuiceOneAppPerSuite with ViewValidator with ViewMatchers {

  lazy val basePrefix = "supplementary."
  lazy val addressPrefix = "supplementary.address."
  val itemId = "a7sc78"

  lazy val injector: Injector = app.injector
  implicit lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]

  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val fakeRequest: Request[AnyContentAsEmpty.type] = FakeRequest("", "").withCSRFToken
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  implicit lazy val flash: Flash = new Flash()

  def assertMessage(key: String, expected: String): Unit = messages(key) must be(expected)

  def fakeJourneyRequest(choice: String): JourneyRequest[AnyContentAsEmpty.type] = {
    val cache = ExportsDeclaration(None, DeclarationStatus.COMPLETE, "sessionId", Instant.now(), Instant.now(), choice)
    JourneyRequest(AuthenticatedRequest(fakeRequest, ExportsTestData.newUser("", "")), cache)
  }

  SharedMetricRegistries.clear()
}
