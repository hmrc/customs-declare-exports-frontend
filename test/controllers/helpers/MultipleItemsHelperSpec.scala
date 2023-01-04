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

package controllers.helpers

import base.UnitSpec
import play.api.data.Forms.{mapping, text}
import play.api.data.{Form, FormError}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

class MultipleItemsHelperSpec extends UnitSpec {
  import MultipleItemsHelperSpec._

  "MultipleItemsHelper on add method" should {
    "return sequence with value" when {
      "input data is correct" in {
        MultipleItemsHelper.add(correctForm, Seq(), limit, fieldId = valueFieldName, valueMessageKey) must be(Right(Seq(correctValue)))
      }
    }

    "return form with errors" when {
      "limit of elements is reached" in {
        val expectedOutput = Left(correctForm.copy(errors = limitError(valueFieldName)))

        MultipleItemsHelper.add(correctForm, Seq(), 0, fieldId = valueFieldName, valueMessageKey) must be(expectedOutput)
      }

      "elements is duplicated" in {
        val expectedOutput = Left(correctForm.copy(errors = duplicationError(valueFieldName)))

        MultipleItemsHelper.add(correctForm, Seq(correctValue), limit, fieldId = valueFieldName, valueMessageKey) must be(expectedOutput)
      }

      "input is incorrect" in {
        MultipleItemsHelper.add(incorrectForm, Seq(), limit, fieldId = valueFieldName, valueMessageKey) must be(Left(incorrectForm))
      }
    }
  }

  "MultipleItemsHelper on remove method" should {

    "return cache when item not defined" in {
      val cachedData = Seq(TestForm("ABC"), TestForm("DEF"))
      val filter: TestForm => Boolean = _.value == "99"
      MultipleItemsHelper.remove(cachedData, filter) must be(cachedData)
    }

    "return updated sequence without selected item" in {
      val filter: TestForm => Boolean = _.value == "DEF"
      val cachedData = Seq(TestForm("ABC"), TestForm("DEF"))
      val expectedOutput = Seq(TestForm("ABC"))

      MultipleItemsHelper.remove(cachedData, filter) must be(expectedOutput)
    }
  }

  "MultipleItemsHelper on continue" should {
    "return errors" when {
      "user fill inputs" in {
        val expectedOutput = correctForm.copy(errors = continueError(valueFieldName))

        MultipleItemsHelper.continue(correctForm, Seq(), isMandatory = false, fieldId = valueFieldName) must be(expectedOutput)
      }

      "user doesn't add any data and screen is mandatory" in {
        val expectedOutput = testForm.copy(errors = mandatoryError(valueFieldName))

        MultipleItemsHelper.continue(testForm, Seq(), isMandatory = true, fieldId = valueFieldName) must be(expectedOutput)
      }
    }

    "return form without errors" when {
      "inputs are empty and cache contains data" in {
        MultipleItemsHelper.continue(testForm, Seq(TestForm("ABC")), isMandatory = true, fieldId = valueFieldName) must be(testForm)
      }
    }
  }

  "MultipleItemsHelper on save and continue" should {
    "add item when form is not empty" in {
      val expectedOutput = Right(Seq(correctValue))

      MultipleItemsHelper.saveAndContinue(correctForm, Seq(), isMandatory = true, limit, fieldId = valueFieldName, valueMessageKey) must be(
        expectedOutput
      )
    }

    "return form with errors when cache is empty and page is mandatory" in {
      val expectedOutput = Left(testForm.copy(errors = mandatoryError(valueFieldName)))

      MultipleItemsHelper.saveAndContinue(testForm, Seq(), isMandatory = true, limit, fieldId = valueFieldName, valueMessageKey) must be(
        expectedOutput
      )
    }

    "return sequence with actual cache when user has data in cache and has empty form" in {
      val cachedData = Seq(TestForm("ABC"))

      MultipleItemsHelper.saveAndContinue(testForm, cachedData, isMandatory = true, limit, fieldId = valueFieldName, valueMessageKey) must be(
        Right(cachedData)
      )
    }
  }

  "MultipleItemsHelper on appendAll" should {
    "append item when defined" in {
      val expectedOutput = Seq(1, 2, 3, 4)

      MultipleItemsHelper.appendAll(Seq(1, 2), Some(3), Some(4)) must be(expectedOutput)
    }

    "not append item when not defined" in {
      val expectedOutput = Seq("A", "B", "C", "D")

      MultipleItemsHelper.appendAll(Seq("A", "B", "C"), None, Some("D")) must be(expectedOutput)
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

  def duplicationError(id: String = "") = Seq(FormError(id, s"${valueMessageKey}.error.duplicate"))
  def limitError(id: String = "") = Seq(FormError(id, "supplementary.limit"))
  def continueError(id: String = "") = Seq(FormError(id, "supplementary.continue.error"))
  def mandatoryError(id: String = "") = Seq(FormError(id, "supplementary.continue.mandatory"))

  val valueFieldName = "value"
  val valueMessageKey = "value"
  val correctJson: JsValue = JsObject(Map(valueFieldName -> JsString(correctValue.value)))
  val incorrectJson: JsValue = JsObject(Map(valueFieldName -> JsString(incorrectValue.value)))

  val limit = 10

  val testMapping =
    mapping(valueFieldName -> text().verifying(errorMessage, _ == correctValue.value))(TestForm.apply)(TestForm.unapply)

  val testForm: Form[TestForm] = Form(testMapping)

  val correctForm = testForm.bind(correctJson, UnitSpec.JsonBindMaxChars)
  val incorrectForm = testForm.bind(incorrectJson, UnitSpec.JsonBindMaxChars)
}
