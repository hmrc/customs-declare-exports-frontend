/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.common

import forms.common.Date._
import base.UnitSpec
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class DateSpec extends UnitSpec {
  import DateSpec._

  val dateFormatError: String = "dateTime.date.error.format"
  val dateOutOfRangeError: String = "dateTime.date.error.outOfRange"
  val dayEmptyFieldError: String = "dateTime.date.day.error.empty"
  val monthEmptyFieldError: String = "dateTime.date.month.error.empty"
  val yearEmptyFieldError: String = "dateTime.date.year.error.empty"

  "Date mapping validation rules" should {

    "return errors" when {

      "provided with empty data" in {

        val input = Map.empty[String, String]
        val expectedErrors =
          Seq(FormError(yearKey, yearEmptyFieldError), FormError(monthKey, monthEmptyFieldError), FormError(dayKey, dayEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with year only" in {

        val input = Map("year" -> "2003")
        val expectedErrors = Seq(FormError(monthKey, monthEmptyFieldError), FormError(dayKey, dayEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with month only" in {

        val input = Map("month" -> "7")
        val expectedErrors = Seq(FormError(yearKey, yearEmptyFieldError), FormError(dayKey, dayEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with day only" in {

        val input = Map("day" -> "13")
        val expectedErrors = Seq(FormError(yearKey, yearEmptyFieldError), FormError(monthKey, monthEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no year" in {

        val input = Map("month" -> "7", "day" -> "13")
        val expectedErrors = Seq(FormError(yearKey, yearEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no month" in {

        val input = Map("year" -> "2003", "day" -> "13")
        val expectedErrors = Seq(FormError(monthKey, monthEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no day" in {

        val input = Map("year" -> "2003", "month" -> "7")
        val expectedErrors = Seq(FormError(dayKey, dayEmptyFieldError))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with year" which {

        "is less than 2000" in {

          val input = Map("year" -> "1999", "month" -> "7", "day" -> "13")
          val expectedErrors = Seq(FormError("", dateOutOfRangeError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 2099" in {

          val input = Map("year" -> "2100", "month" -> "7", "day" -> "13")
          val expectedErrors = Seq(FormError("", dateOutOfRangeError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special character" in {

          val input = Map("year" -> "20A#", "month" -> "7", "day" -> "13")
          val expectedErrors = Seq(FormError(yearKey, "error.number"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with month" which {

        "is less than 1" in {

          val input = Map("year" -> "2003", "month" -> "0", "day" -> "13")
          val expectedErrors = Seq(FormError("", dateFormatError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 12" in {

          val input = Map("year" -> "2003", "month" -> "13", "day" -> "13")
          val expectedErrors = Seq(FormError("", dateFormatError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special character" in {

          val input = Map("year" -> "2003", "month" -> "C#", "day" -> "13")
          val expectedErrors = Seq(FormError(monthKey, "error.number"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with day" which {

        "is less than 1" in {

          val input = Map("year" -> "2003", "month" -> "7", "day" -> "0")
          val expectedErrors = Seq(FormError("", dateFormatError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 31" in {

          val input = Map("year" -> "2003", "month" -> "7", "day" -> "32")
          val expectedErrors = Seq(FormError("", dateFormatError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is 31-st of February" in {

          val input = Map("year" -> "2003", "month" -> "02", "day" -> "31")
          val expectedErrors = Seq(FormError("", dateFormatError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special character" in {

          val input = Map("year" -> "2003", "month" -> "7", "day" -> "C#")
          val expectedErrors = Seq(FormError(dayKey, "error.number"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with date" which {

        "is 1999-12-31" in {

          val input = Map("year" -> "1999", "month" -> "12", "day" -> "31")
          val expectedErrors = Seq(FormError("", dateOutOfRangeError))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is 2100-01-01" in {

          val input = Map("year" -> "2100", "month" -> "1", "day" -> "1")
          val expectedErrors = Seq(FormError("", dateOutOfRangeError))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
        val form = Date.form.bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "return no errors" when {

      "provided with correct data" in {

        val input = correctDateJSON
        val form = Date.form.bind(input, JsonBindMaxChars)

        form.errors must equal(Seq.empty)
      }

      "provided with date on the lower limit" in {

        val input = JsObject(Map(yearKey -> JsString("2000"), monthKey -> JsString("1"), dayKey -> JsString("1")))
        val form = Date.form.bind(input, JsonBindMaxChars)

        form.errors must equal(Seq.empty)
      }

      "provided with date on the upper limit" in {

        val input = JsObject(Map(yearKey -> JsString("2099"), monthKey -> JsString("12"), dayKey -> JsString("31")))
        val form = Date.form.bind(input, JsonBindMaxChars)

        form.errors must equal(Seq.empty)
      }

      "provided with correct data but with '0' before month and day" in {

        val input: JsValue =
          JsObject(Map(yearKey -> JsString("2003"), monthKey -> JsString("01"), dayKey -> JsString("02")))
        val form = Date.form.bind(input, JsonBindMaxChars)

        form.errors must equal(Seq.empty)
      }
    }

  }

  "Date to102Format method" should {

    "return date in yyyyMMdd format" in {

      val date = correctDate
      date.toDisplayFormat must equal("13/07/2020")
    }
  }

}

object DateSpec {

  private val correctYearValue = 2020
  private val correctMonthValue = 7
  private val correctDayValue = 13

  private val incorrectYearValue = 1999
  private val incorrectMonthValue = 13
  private val incorrectDayValue = 32

  val correctDate = Date(year = Some(correctYearValue), month = Some(correctMonthValue), day = Some(correctDayValue))
  val incorrectDate =
    Date(year = Some(incorrectYearValue), month = Some(incorrectMonthValue), day = Some(incorrectDayValue))

  val correctDateJSON: JsValue = JsObject(
    Map(
      yearKey -> JsString(correctDate.year.get.toString),
      monthKey -> JsString(correctDate.month.get.toString),
      dayKey -> JsString(correctDate.day.get.toString)
    )
  )
  val incorrectDateJSON: JsValue = JsObject(
    Map(
      yearKey -> JsString(incorrectDate.year.get.toString),
      monthKey -> JsString(incorrectDate.month.get.toString),
      dayKey -> JsString(incorrectDate.day.get.toString)
    )
  )

}
