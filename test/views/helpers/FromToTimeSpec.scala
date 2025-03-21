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

import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.{Lang, Messages}
import views.common.UnitViewSpec

class FromToTimeSpec extends UnitViewSpec with BeforeAndAfterEach {

  trait Setup {
    val english = "en"
    val welsh = "cy"

    val from_00 = "2025-02-26T23:00Z"
    val to_00 = "2025-02-27T02:00Z"

    val from_01 = "2025-02-26T23:01Z"
    val to_01 = "2025-02-27T02:01Z"

    val from_30 = "2025-02-26T23:30Z"
    val to_30 = "2025-02-27T02:30Z"

    implicit val messages: Messages = mock[Messages]
  }

  "FromToTime" should {

    "Output in English correctly with 00 mins" in new Setup {
      when(messages.lang).thenReturn(Lang(english))

      val fromTo = FromToTime(from_00, to_00)
      fromTo.fromHour mustBe "11:00pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2:00am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in English correctly with 01 mins" in new Setup {
      when(messages.lang).thenReturn(Lang(english))

      val fromTo = FromToTime(from_01, to_01)
      fromTo.fromHour mustBe "11:01pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2:01am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in English correctly with 30 mins" in new Setup {
      when(messages.lang).thenReturn(Lang(english))

      val fromTo = FromToTime(from_30, to_30)
      fromTo.fromHour mustBe "11:30pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2:30am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in Welsh correctly with 00 mins" in new Setup {
      when(messages.lang).thenReturn(Lang(welsh))

      val fromTo = FromToTime(from_00, to_00)
      fromTo.fromHour mustBe "11:00yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2:00yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }

    "Output in Welsh correctly with 01 mins" in new Setup {
      when(messages.lang).thenReturn(Lang(welsh))

      val fromTo = FromToTime(from_01, to_01)
      fromTo.fromHour mustBe "11:01yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2:01yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }

    "Output in Welsh correctly with 30 mins" in new Setup {
      when(messages.lang).thenReturn(Lang(welsh))

      val fromTo = FromToTime(from_30, to_30)
      fromTo.fromHour mustBe "11:30yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2:30yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }
  }
}
