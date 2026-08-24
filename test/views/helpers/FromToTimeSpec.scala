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

import java.time.Instant

class FromToTimeSpec extends UnitViewSpec with BeforeAndAfterEach {

  private val instant = Instant.parse("2023-08-31T23:55:00Z")

  private val welshMonths =
    List("Ionawr", "Chwefror", "Mawrth", "Ebrill", "Mai", "Mehefin", "Gorffennaf", "Awst", "Medi", "Hydref", "Tachwedd", "Rhagfyr")

  private def firstOfMonthAt11(monthOfYear: Int): Instant = Instant.parse(f"2023-$monthOfYear%02d-01T11:00:00Z")

  trait Setup {
    val english = Lang("en")
    val welsh = Lang("cy")

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
      when(messages.lang).thenReturn(english)

      val fromTo = FromToTime(from_00, to_00)
      fromTo.fromHour mustBe "11:00pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2:00am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in English correctly with 01 mins" in new Setup {
      when(messages.lang).thenReturn(english)

      val fromTo = FromToTime(from_01, to_01)
      fromTo.fromHour mustBe "11:01pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2:01am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in English correctly with 30 mins" in new Setup {
      when(messages.lang).thenReturn(english)

      val fromTo = FromToTime(from_30, to_30)
      fromTo.fromHour mustBe "11:30pm"
      fromTo.fromDate mustBe "Wednesday 26 February 2025"
      fromTo.toHour mustBe "2:30am"
      fromTo.toDate mustBe "Thursday 27 February 2025"
    }

    "Output in Welsh correctly with 00 mins" in new Setup {
      when(messages.lang).thenReturn(welsh)

      val fromTo = FromToTime(from_00, to_00)
      fromTo.fromHour mustBe "11:00yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2:00yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }

    "Output in Welsh correctly with 01 mins" in new Setup {
      when(messages.lang).thenReturn(welsh)

      val fromTo = FromToTime(from_01, to_01)
      fromTo.fromHour mustBe "11:01yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2:01yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }

    "Output in Welsh correctly with 30 mins" in new Setup {
      when(messages.lang).thenReturn(welsh)

      val fromTo = FromToTime(from_30, to_30)
      fromTo.fromHour mustBe "11:30yh"
      fromTo.fromDate mustBe "Dydd Mercher 26 Chwefror 2025"
      fromTo.toHour mustBe "2:30yb"
      fromTo.toDate mustBe "Dydd Iau 27 Chwefror 2025"
    }

    "Output the calendar year for the final week of December" in new Setup {
      when(messages.lang).thenReturn(english)

      // YYYY returns 2026 instead of 2025
      val fromTo = FromToTime("2025-12-29T23:00Z", "2025-12-31T02:00Z")
      fromTo.fromDate mustBe "Monday 29 December 2025"
      fromTo.toDate mustBe "Wednesday 31 December 2025"
    }
  }

  "the Locale is English" should {

    "format date at time correctly" in {
      FromToTime.formatDate(instant)(messages) mustBe "1 September 2023"
      FromToTime.formatDateAtTime(instant)(messages) mustBe "1 September 2023 at 12:55am"
    }
  }

  "the Locale is Welsh" should {

    "format date at time correctly" in {
      for (ix <- 1 to 12) {
        val instant = firstOfMonthAt11(ix)
        val month = welshMonths(ix - 1)

        FromToTime.formatDate(instant)(messagesCy) mustBe s"1 $month 2023"

        // British Summer Time runs from April to October, shifting 11:00 UTC to 12:00 local
        val expectedTime = if ((4 to 10).contains(ix)) "12:00yh" else "11:00yb"
        FromToTime.formatDateAtTime(instant)(messagesCy) mustBe s"1 $month 2023 am $expectedTime"
      }
    }
  }
}
