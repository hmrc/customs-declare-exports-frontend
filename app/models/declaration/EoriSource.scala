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

package models.declaration

import play.api.libs.json.{JsString, JsonValidationError, Reads, Writes}

sealed trait EoriSource

object EoriSource {
  case object UserEori extends EoriSource
  case object OtherEori extends EoriSource

  val values = Seq(UserEori, OtherEori)

  lazy val lookupByValue: Map[String, EoriSource] = values.map(entry => entry.toString -> entry).toMap

  implicit val reads: Reads[EoriSource] = Reads.StringReads.collect(JsonValidationError("error.unknown"))(lookupByValue)
  implicit val writes: Writes[EoriSource] = Writes(code => JsString(code.toString))
}
