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

package forms.common

import forms.common.Date._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString, JsValue}

class DateSpec extends WordSpec with MustMatchers {
  import DateSpec._

  "Date mapping validation rules" should {

    "return errors" when {

      "provided with year only" in {

        val input = Map("year" -> "2003")
        val expectedErrors = Seq(FormError("", "dateTime.date.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with month only" in {

        val input = Map("month" -> "7")
        val expectedErrors = Seq(FormError("", "dateTime.date.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with day only" in {

        val input = Map("day" -> "13")
        val expectedErrors = Seq(FormError("", "dateTime.date.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no year" in {

        val input = Map("month" -> "7", "day" -> "13")
        val expectedErrors = Seq(FormError("", "dateTime.date.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no month" in {

        val input = Map("year" -> "2003", "day" -> "13")
        val expectedErrors = Seq(FormError("", "dateTime.date.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no day" in {

        val input = Map("year" -> "2003", "month" -> "7")
        val expectedErrors = Seq(FormError("", "dateTime.date.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with year" which {

        "is less than 2000" in {

          val input = Map("year" -> "1999", "month" -> "7", "day" -> "13")
          val expectedErrors = Seq(FormError("year", "dateTime.date.year.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 2099" in {

          val input = Map("year" -> "2100", "month" -> "7", "day" -> "13")
          val expectedErrors = Seq(FormError("year", "dateTime.date.year.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special character" in {

          val input = Map("year" -> "20A#", "month" -> "7", "day" -> "13")
          val expectedErrors = Seq(FormError("year", "dateTime.date.year.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with month" which {

        "is less than 1" in {

          val input = Map("year" -> "2003", "month" -> "0", "day" -> "13")
          val expectedErrors = Seq(FormError("month", "dateTime.date.month.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 12" in {

          val input = Map("year" -> "2003", "month" -> "13", "day" -> "13")
          val expectedErrors = Seq(FormError("month", "dateTime.date.month.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special character" in {

          val input = Map("year" -> "2003", "month" -> "C#", "day" -> "13")
          val expectedErrors = Seq(FormError("month", "dateTime.date.month.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with day" which {

        "is less than 1" in {

          val input = Map("year" -> "2003", "month" -> "7", "day" -> "0")
          val expectedErrors = Seq(FormError("day", "dateTime.date.day.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 31" in {

          val input = Map("year" -> "2003", "month" -> "7", "day" -> "32")
          val expectedErrors = Seq(FormError("day", "dateTime.date.day.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special character" in {

          val input = Map("year" -> "2003", "month" -> "7", "day" -> "C#")
          val expectedErrors = Seq(FormError("day", "dateTime.date.day.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
        val form = Date.form().bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "return no errors" when {

      "provided with correct data" in {

        val input = correctDateJSON
        val form = Date.form().bind(input)

        form.hasErrors must be(false)
      }

      "provided with empty data" in {

        val input = emptyDateJSON
        val form = Date.form().bind(input)

        form.hasErrors must be(false)
      }
    }

  }

}

object DateSpec {

  val correctDate = Date(
    year = Some("2003"),
    month = Some("07"),
    day = Some("13")
  )
  val incorrectDate = Date(
    year = Some("1999"),
    month = Some("13"),
    day = Some("33")
  )
  val emptyDate = Date(None, None, None)

  val correctDateJSON: JsValue = JsObject(Map(
    yearKey -> JsString(correctDate.year.get),
    monthKey -> JsString(correctDate.month.get),
    dayKey -> JsString(correctDate.day.get)
  ))
  val incorrectDateJSON: JsValue = JsObject(Map(
    yearKey -> JsString(incorrectDate.year.get),
    monthKey -> JsString(incorrectDate.month.get),
    dayKey -> JsString(incorrectDate.day.get)
  ))
  val emptyDateJSON: JsValue = JsObject(Map(
    yearKey -> JsString(""),
    monthKey -> JsString(""),
    dayKey -> JsString("")
  ))


}