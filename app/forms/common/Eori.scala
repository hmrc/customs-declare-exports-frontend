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

package forms.common

import com.typesafe.config.ConfigFactory
import models.ExportsFieldPointer.ExportsFieldPointer
import models.FieldMapping
import play.api.data.Forms.text
import play.api.data.Mapping
import play.api.libs.json._
import utils.validators.forms.FieldValidator._

case class Eori(value: String) extends Ordered[Eori] {
  override def compare(y: Eori): Int = value.compareTo(y.value)

  override def toString() = value
}

object Eori extends FieldMapping {
  def build(value: String): Eori = new Eori(value.toUpperCase)

  override val pointer: ExportsFieldPointer = "eori"

  implicit val format: Format[Eori] = new Format[Eori] {
    override def writes(o: Eori): JsValue = JsString(o.value)

    private val mappedReads = Reads.StringReads.map(value => Eori.build(value))

    override def reads(json: JsValue): JsResult[Eori] = mappedReads.reads(json)
  }
  private lazy val euEoriEnabled: Boolean =
    ConfigFactory.load().hasPath("features.euEori") && ConfigFactory.load().getBoolean("features.euEori")

  def mapping(messageKeyWhenEmpty: String = "declaration.eori.empty"): Mapping[Eori] = {
    val eoriValidator = if (euEoriEnabled) isValidEoriFlagged else isValidEori
    text()
      .verifying(messageKeyWhenEmpty, nonEmpty)
      .verifying("declaration.eori.error.format", isEmpty or eoriValidator)
      .transform(build, _.value)
  }
}
