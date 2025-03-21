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

package views.helpers

import models.requests.JourneyRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, RequestHeader}
import views.common.UnitViewSpec

import java.util.Locale

class FromToTimeSpec extends UnitViewSpec with BeforeAndAfterEach  {

  val from = "2025-02-26T23:00Z"
  val to = "2025-02-27T02:00Z"

  val ENGLISH_LOCALE: Locale = Locale.forLanguageTag("en")
  val WELSH_LOCALE: Locale = Locale.forLanguageTag("cy");

  val ENGLISH_LANG: Lang = Lang(ENGLISH_LOCALE)
  val WELSH_LANG: Lang = Lang(WELSH_LOCALE)

  implicit val messagesApi: MessagesApi = mock[MessagesApi]
  val messages: Messages = mock[Messages]
  override implicit val request: JourneyRequest[AnyContent] = mock[JourneyRequest[AnyContent]]


  override protected def beforeEach(): Unit =  {
    super.beforeEach()
    Mockito.reset(request)

    when(messagesApi.preferred(any[RequestHeader])).thenReturn(messages)
  }

  "FromToTime" should {

    "Output in English correctly" in {
      when(request.lang(messagesApi)).thenReturn(ENGLISH_LANG)

      val fromTo = FromToTime(from, to)

      fromTo.fromHour mustBe "11pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in Welsh correctly" in {
      when(request.lang(messagesApi)).thenReturn(WELSH_LANG)

      val fromTo = FromToTime(from, to)

      fromTo.fromHour mustBe "11yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }
  }
}
