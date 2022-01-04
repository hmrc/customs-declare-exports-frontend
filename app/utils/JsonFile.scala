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

package utils

import play.api.libs.json.{JsArray, JsString, Json, Reads}

import scala.util.{Failure, Success, Try}

object JsonFile {
  def readFromJsonFile[T](file: String, deserializer: (String, String) => T): List[T] = {
    val jsonInputStream = getClass.getResourceAsStream(file)

    Json.parse(jsonInputStream) match {
      case JsArray(cs) =>
        cs.toList.collect {
          case JsArray(Seq(label: JsString, code: JsString)) =>
            deserializer(label.value, code.value)
        }
      case _ => throwError(file)
    }
  }

  def getJsonArrayFromFile[T](file: String, reader: Reads[T]): List[T] = {
    val jsonInputStream = getClass.getResourceAsStream(file)

    Try(Json.parse(jsonInputStream)) match {
      case Success(JsArray(jsValues)) =>
        val items = jsValues.toList.map { jsValue =>
          reader.reads(jsValue).asOpt
        }

        if (items.contains(None)) {
          throw new IllegalArgumentException(s"One or more entries could not be parsed in JSON file: '$file'")
        }

        items.flatten

      case Success(_)  => throwError(file)
      case Failure(ex) => throw new IllegalArgumentException(s"Failed to read JSON file: '$file'", ex)
    }
  }

  private def throwError(jsonFile: String) =
    throw new IllegalArgumentException(s"Could not read JSON array from file: '$jsonFile'")
}
