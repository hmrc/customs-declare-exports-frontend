/*
 * Copyright 2022 HM Revenue & Customs
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

import base.UnitSpec
import play.api.data.FormError

class TimeSpec extends UnitSpec {

  "Time mapping validation rules" should {

    "return errors" when {

      "provided with no hour" in {

        val input = Map("minute" -> "17")
        val expectedErrors = Seq(FormError("", "dateTime.time.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with no minute" in {

        val input = Map("hour" -> "15")
        val expectedErrors = Seq(FormError("", "dateTime.time.error"))

        testFailedValidationErrors(input, expectedErrors)
      }

      "provided with hour" which {

        "is less than 0" in {

          val input = Map("hour" -> "-1", "minute" -> "17")
          val expectedErrors = Seq(FormError("hour", "dateTime.time.hour.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 23" in {

          val input = Map("hour" -> "24", "minute" -> "17")
          val expectedErrors = Seq(FormError("hour", "dateTime.time.hour.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special characters" in {

          val input = Map("hour" -> "F#", "minute" -> "17")
          val expectedErrors = Seq(FormError("hour", "dateTime.time.hour.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      "provided with minute" which {

        "is less than 0" in {

          val input = Map("hour" -> "15", "minute" -> "-1")
          val expectedErrors = Seq(FormError("minute", "dateTime.time.minute.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "is more than 59" in {

          val input = Map("hour" -> "15", "minute" -> "60")
          val expectedErrors = Seq(FormError("minute", "dateTime.time.minute.error"))

          testFailedValidationErrors(input, expectedErrors)
        }

        "contains alphanumerical or special characters" in {

          val input = Map("hour" -> "15", "minute" -> "F#")
          val expectedErrors = Seq(FormError("minute", "dateTime.time.minute.error"))

          testFailedValidationErrors(input, expectedErrors)
        }
      }

      def testFailedValidationErrors(input: Map[String, String], expectedErrors: Seq[FormError]): Unit = {
        val form = Time.form.bind(input)
        expectedErrors.foreach(form.errors must contain(_))
      }
    }

    "return no errors" when {

      "provided with correct data" in {

        val input = Map("hour" -> "15", "minute" -> "17")
        val form = Time.form.bind(input)

        form.hasErrors must be(false)
      }

      "provided with empty data" in {

        val input = Map.empty[String, String]
        val form = Time.form.bind(input)

        form.hasErrors must be(false)
      }
    }

  }

}
