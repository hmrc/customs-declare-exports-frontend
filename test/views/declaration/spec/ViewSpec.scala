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
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, Flash}
import play.api.test.FakeRequest
import services.{Countries, Country}

trait ViewSpec extends PlaySpec with OneAppPerSuite with ViewValidator {

  lazy val basePrefix = "supplementary."
  lazy val addressPrefix = "supplementary.address."

  lazy val injector: Injector = app.injector
  implicit lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]

  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  implicit lazy val flash: Flash = new Flash()
  implicit lazy val countries: List[Country] = Countries.allCountries

  def assertMessage(key: String, expected: String): Unit = messages(key) must be(expected)

  def fakeJourneyRequest(choice: String): JourneyRequest[AnyContentAsEmpty.type] =
    JourneyRequest(AuthenticatedRequest(fakeRequest, ExportsTestData.newUser("", "")), new Choice(choice))

  SharedMetricRegistries.clear()
}
