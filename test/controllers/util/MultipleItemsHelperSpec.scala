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

package controllers.util

import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import uk.gov.hmrc.http.InternalServerException

class MultipleItemsHelperSpec extends WordSpec with MustMatchers {
  import MultipleItemsHelperSpec._

  "MultipleItemsHelper on add method" should {
    "return sequence with value" when {
      "input data is correct" in {
        MultipleItemsHelper.add(correctForm, Seq(), limit) must be(Right(Seq(correctValue)))
      }
    }

    "return form with errors" when {
      "limit of elements is reached" in {
        val expectedOutput = Left(correctForm.copy(errors = limitError))

        MultipleItemsHelper.add(correctForm, Seq(), 0) must be(expectedOutput)
      }

      "elements is duplicated" in {
        val expectedOutput = Left(correctForm.copy(errors = duplicationError))

        MultipleItemsHelper.add(correctForm, Seq(correctValue), limit) must be(expectedOutput)
      }

      "input is incorrect" in {
        MultipleItemsHelper.add(incorrectForm, Seq(), limit) must be(Left(incorrectForm))
      }
    }
  }

  "MultipleItemsHelper on remove method" should {
    "throw InternalServerError when id is not defined" in {
      assertThrows[InternalServerException] {
        MultipleItemsHelper.remove(None, Seq())
      }
    }

    "return updated sequence without selected item" in {
      val idToRemove = Some("1")
      val cachedData = Seq(TestForm("ABC"), TestForm("DEF"))
      val expectedOutput = Seq(TestForm("ABC"))

      MultipleItemsHelper.remove(idToRemove, cachedData) must be(expectedOutput)
    }
  }

  "MultipleItemsHelper on continue" should {
    "return errors" when {
      "user fill inputs" in {
        val expectedOutput = correctForm.copy(errors = continueError)

        MultipleItemsHelper.continue(correctForm, Seq(), false) must be(expectedOutput)
      }

      "user doesn't add any data and screen is mandatory" in {
        val expectedOutput = testForm.copy(errors = mandatoryError)

        MultipleItemsHelper.continue(testForm, Seq(), true) must be(expectedOutput)
      }
    }

    "return form without errors" when {
      "inputs are empty and cache contains data" in {
        MultipleItemsHelper.continue(testForm, Seq(TestForm("ABC")), true) must be(testForm)
      }
    }
  }

  "MultipleItemsHelper on save and continue" should {
    "add item when form is not empty" in {
      val expectedOutput = Right(Seq(correctValue))

      MultipleItemsHelper.saveAndContinue(correctForm, Seq(), true, limit) must be(expectedOutput)
    }

    "return form with errors when cache is empty and page is mandatory" in {
      val expectedOutput = Left(testForm.copy(errors = mandatoryError))

      MultipleItemsHelper.saveAndContinue(testForm, Seq(), true, limit) must be(expectedOutput)
    }

    "return sequence with actual cache when user has data in cache and has empty form" in {
      val cachedData = Seq(TestForm("ABC"))

      MultipleItemsHelper.saveAndContinue(testForm, cachedData, true, limit) must be(Right(cachedData))
    }
  }
}

object MultipleItemsHelperSpec {
  case class TestForm(value: String)

  object TestForm {
    implicit val format = Json.format[TestForm]
  }

  val errorMessage = "Incorrect value"
  val correctValue = TestForm("Correct value")
  val incorrectValue = TestForm("Incorrect value")

  val duplicationError = Seq(FormError("", "supplementary.duplication"))
  val limitError = Seq(FormError("", "supplementary.limit"))
  val continueError = Seq(FormError("", "supplementary.continue.error"))
  val mandatoryError = Seq(FormError("", "supplementary.continue.mandatory"))

  val correctJson: JsValue = JsObject(Map("value" -> JsString(correctValue.value)))
  val incorrectJson: JsValue = JsObject(Map("value" -> JsString(incorrectValue.value)))

  val limit = 10

  val testMapping =
    mapping("value" -> text().verifying(errorMessage, _ == correctValue.value))(TestForm.apply)(TestForm.unapply)

  val testForm: Form[TestForm] = Form(testMapping)

  val correctForm = testForm.bind(correctJson)
  val incorrectForm = testForm.bind(incorrectJson)
}
