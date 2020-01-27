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

package forms.common

import play.api.data.Forms.text
import play.api.data.Mapping
import play.api.libs.json.{Format, JsResult, JsString, JsValue, Reads}
import utils.validators.forms.FieldValidator._

case class Eori(value: String) {
  override def toString() = value
}

object Eori {
  def build(value: String): Eori = new Eori(value.toUpperCase)

  implicit val format: Format[Eori] = new Format[Eori] {
    override def writes(o: Eori): JsValue = JsString(o.value)

    private val mappedReads = Reads.StringReads.map(value => Eori.build(value))

    override def reads(json: JsValue): JsResult[Eori] = mappedReads.reads(json)
  }

  def mapping(errorPrefix: String): Mapping[Eori] =
    text()
      .verifying(s"${errorPrefix}.eori.empty", nonEmpty)
      .verifying(s"${errorPrefix}.eori.error.format", isValidEORIPattern and noLongerThan(17) and noShorterThan(3))
      .transform(build, unapply(_).getOrElse(""))
}
