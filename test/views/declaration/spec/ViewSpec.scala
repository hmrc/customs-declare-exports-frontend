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

import base.{ExportsTestData, ViewValidator}
import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import forms.Choice
import models.requests.{AuthenticatedRequest, JourneyRequest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, Flash, Request, Result}
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.FakeRequestCSRFSupport._

import play.api.test.Helpers._

import scala.concurrent.Future

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
  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())
  implicit protected def htmlBodyOf(page: String): Document = Jsoup.parse(page)
  implicit protected def htmlBodyOf(result: Future[Result]): Document = htmlBodyOf(contentAsString(result))

  def assertMessage(key: String, expected: String): Unit = messages(key) must be(expected)

  def fakeJourneyRequest(choice: String): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(AuthenticatedRequest(fakeRequest, ExportsTestData.newUser("", "")), new Choice(choice))

  SharedMetricRegistries.clear()
}
