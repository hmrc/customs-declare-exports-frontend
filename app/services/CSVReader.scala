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

package services

import scala.io.Source
import scala.util.matching.Regex

trait CSVReader {

  val regexPattern: Regex

  def readIgnoringHeaderRow[T](filename: String)(convert: (String, String) => T): List[T] =
    read(filename).tail.map {
      case regexPattern(col1: String, col2: String) => convert(col1, col2)
    }

  private def read(path: String): List[String] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource(path), "UTF-8")
    try {
      source.getLines().toList
    } finally {
      source.close()
    }
  }
}
