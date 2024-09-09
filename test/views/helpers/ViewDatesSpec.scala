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

import base.UnitSpec
import play.api.i18n.Lang
import play.api.test.Helpers.{stubLangs, stubMessagesApi}

import java.time.Instant
import java.util.Locale

class ViewDatesSpec extends UnitSpec {

  private val instant = Instant.parse("2023-08-31T23:55:00Z")

  "ViewDates" when {

    "the Locale is English" should {
      implicit val messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))

      "format date at time correctly" in {
        ViewDates.formatDate(instant) mustBe "1 September 2023"
        ViewDates.formatDateAtTime(instant) mustBe "1 September 2023 at 12:55am"
        ViewDates.formatTimeDate(instant) mustBe "00:55, 1 September 2023"
      }
    }

    "the Locale is Welsh" should {
      implicit val messages = stubMessagesApi(langs = stubLangs(List(Lang("cy")))).preferred(List(Lang("cy")))

      val months =
        Array("", "Ionawr", "Chwefror", "Mawrth", "Ebrill", "Mai", "Mehefin", "Gorffennaf", "Awst", "Medi", "Hydref", "Tachwedd", "Rhagfyr")

      "format date at time correctly" in {
        for (ix <- 1 to 12) {
          val instant = Instant.parse(f"2023-$ix%02d-01T11:00:00Z")
          val month = months(ix)

          ViewDates.formatDate(instant) mustBe s"1 $month 2023"

          val expectedTime = if ((4 to 10).contains(ix)) "12:00yh" else "11:00yb"
          ViewDates.formatDateAtTime(instant) mustBe s"1 $month 2023 am $expectedTime"

          val expectedHour = if ((4 to 10).contains(ix)) "12" else "11"
          ViewDates.formatTimeDate(instant) mustBe s"$expectedHour:00, 1 $month 2023"
        }
      }
    }
  }
}
