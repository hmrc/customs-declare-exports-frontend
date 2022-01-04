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

package forms

import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.json.{Format, JsString, Reads, Writes}
import utils.validators.forms.FieldValidator._

case class Lrn(value: String) {
  def isEmpty: Boolean = value.isEmpty
  def nonEmpty: Boolean = !isEmpty
}

object Lrn {
  implicit val format: Format[Lrn] =
    Format[Lrn](Reads.StringReads.map(Lrn.apply), Writes[Lrn](lrn => JsString(lrn.value)))

  private val lrnMaxLength = 22

  def mapping(prefix: String): Mapping[Lrn] =
    text()
      .verifying(s"$prefix.error.empty", nonEmpty)
      .verifying(s"$prefix.error.length", isEmpty or isNotAlphanumericWithSpace or noLongerThanAfterTrim(lrnMaxLength))
      .verifying(s"$prefix.error.specialCharacter", isEmpty or isAlphanumericWithSpace)
      .transform[Lrn](str => Lrn(str.trim), _.value)
}
