/*
 * Copyright 2020 HM Revenue & Customs
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

import utils.FileReader

import scala.util.matching.Regex

case class HolderOfAuthorisationCode(code: String, description: String)

object HolderOfAuthorisationCode {

  private val regex: Regex = """^"?(\w+)"?,"?([^"\n]+)"?$""".r

  lazy val all: List[HolderOfAuthorisationCode] =
    FileReader("code-lists/holder-of-authorisation-codes.csv").tail.map {
      case regex(code: String, description: String) =>
        HolderOfAuthorisationCode(code, description)
    }.sortBy(_.description)

}


/*
TODO
1. add description to list summary screen
2. look at CYA screen
 */