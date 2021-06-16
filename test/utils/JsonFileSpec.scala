/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import base.UnitSpec
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import connectors.CodeItem
import services.view.AutoCompleteItem

class JsonFileSpec extends UnitSpec {

  "JsonFile readFromJsonFile" should {

    val file = "/testJsonData.json"
    val wrongFile = "/wrongJsonData.json"
    val deserialiser: (String, String) => AutoCompleteItem = (a: String, b: String) => AutoCompleteItem(a, b)

    "read file and parse it correctly" when {

      "data is in an Array" in {
        val result = JsonFile.readFromJsonFile(file, deserialiser)
        val expectedResult = List(AutoCompleteItem("Chemical", "1"), AutoCompleteItem("Aquarium", "2"), AutoCompleteItem("Peppermint", "3"))
        result must be(expectedResult)
      }
    }

    "read file and throw an exception" when {

      "data is not in an Array" in {
        intercept[IllegalArgumentException](JsonFile.readFromJsonFile(wrongFile, deserialiser))
      }
    }

    "throw an exception" when {

      "file doesn't exist" in {
        intercept[MismatchedInputException](JsonFile.readFromJsonFile("wrongPath", deserialiser))
      }
    }
  }

  "JsonFile getJsonArrayFromFile" should {
    "successfully read a file" when {
      "file is populated with one entry" in {
        val result = JsonFile.getJsonArrayFromFile("/code-lists/oneCode.json", CodeItem.formats)
        val expectedResult = List(CodeItem("001", "English", "Welsh"))
        result must be(expectedResult)
      }

      "file is populated with multiple entry" in {
        val result = JsonFile.getJsonArrayFromFile("/code-lists/manyCodes.json", CodeItem.formats)
        val expectedResult = List(CodeItem("001", "English", "Welsh"), CodeItem("002", "English", "Welsh"), CodeItem("003", "English", "Welsh"))

        result must be(expectedResult)
      }
    }

    "throw an exception" when {
      "file does not exist" in {
        val file = "imaginary"
        val thrown = intercept[IllegalArgumentException](JsonFile.getJsonArrayFromFile(file, CodeItem.formats))
        thrown.getMessage must be(s"Failed to read JSON file: '$file'")
      }

      "file is empty" in {
        val file = "/code-lists/empty.json"
        val thrown = intercept[IllegalArgumentException](JsonFile.getJsonArrayFromFile(file, CodeItem.formats))
        thrown.getMessage must be(s"Failed to read JSON file: '$file'")
      }

      "one or more codes are badly formed" in {
        val file = "/code-lists/empty.json"
        val thrown = intercept[IllegalArgumentException](JsonFile.getJsonArrayFromFile(file, CodeItem.formats))
        thrown.getMessage must be(s"Failed to read JSON file: '$file'")
      }
    }
  }
}
