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

package services

import com.github.tototoshi.csv.CSVReader
import services.model.OfficeOfExit

import scala.io.Source

object OfficeOfExits {

  val all: List[OfficeOfExit] = {

    val reader = CSVReader.open(Source.fromURL(getClass.getClassLoader.getResource("code-lists/office-of-exits.csv"), "UTF-8"))
    val codes: List[Map[String, String]] = reader.allWithHeaders()

    codes.map(row => OfficeOfExit(row("Code"), row("Description"))).sortBy(_.description)

  }
}
