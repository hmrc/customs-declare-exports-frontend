/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json._

import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Try}

object EnumJson {

  private def enumReads[E <: Enumeration](anEnum: E): Reads[anEnum.Value] = {
    case JsString(s) =>
      Try(JsSuccess(anEnum.withName(s))) recoverWith { case _: NoSuchElementException =>
        Failure(new InvalidEnumException(anEnum.getClass.getSimpleName, s))
      } get
    case _ => JsError("String value expected")
  }

  implicit def enumWrites[E <: Enumeration](anEnum: E): Writes[anEnum.Value] = (v: anEnum.Value) => JsString(v.toString)

  implicit def format[E <: Enumeration](anEnum: E): Format[anEnum.Value] =
    Format(enumReads(anEnum), enumWrites(anEnum))
}

class InvalidEnumException(className: String, input: String)
    extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")
