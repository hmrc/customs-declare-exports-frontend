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

package forms.declaration
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class Seal(id: String)

object Seal {
  implicit val format = Json.format[Seal]

  val formId = "Seal"

  val formMapping: Mapping[Seal] = Forms.mapping(
    "id" ->
      text()
        .verifying("standard.transport.sealId.empty.error", nonEmpty)
        .verifying("standard.transport.sealId.error.invalid", isEmpty or (isAlphanumeric and noLongerThan(20)))
  )(Seal.apply)(Seal.unapply)

  def form(): Form[Seal] = Form(formMapping)
  val sealsAllowed = 9999
}
