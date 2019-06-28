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

package utils

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.scalatest.{MustMatchers, WordSpec}
import services.model.AutoCompleteItem

class JsonFileSpec extends WordSpec with MustMatchers {
  val file = "/testJsonData.json"
  val wrongFile = "/wrongJsonData.json"
  val deserialiser: (String, String) => AutoCompleteItem = (a: String, b: String) => AutoCompleteItem(a, b)

  "JsonFile" should {

    "read file and parse it correctly" when {

      "data is in an Array" in {
        val result = JsonFile.readFromJsonFile(file, deserialiser)
        val expectedResult = List(
          AutoCompleteItem("Chemical", "1"),
          AutoCompleteItem("Aquarium", "2"),
          AutoCompleteItem("Peppermint", "3")
        )
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

}
