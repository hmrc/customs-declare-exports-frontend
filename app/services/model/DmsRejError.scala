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

package services.model

import play.api.Logger
import play.api.libs.json.Json

case class DmsRejError(code: String, description: String)

object DmsRejError {

  private val logger = Logger(this.getClass)

  implicit val format = Json.format[DmsRejError]

  def apply(list: List[String]): DmsRejError = list match {
    case code :: description :: Nil => DmsRejError(code, description)
    case error =>
      logger.warn("Incorrect error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }
}
