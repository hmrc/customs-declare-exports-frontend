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

package forms.declaration

import forms.{Ducr, Lrn}
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class ConsignmentReferences(ducr: Ducr, lrn: Lrn)

object ConsignmentReferences {

  private val ucrMaxLength = 35

  val mapping =
    Forms.mapping("ducr" -> Ducr.ducrMapping, "lrn" -> Lrn.mapping("supplementary.consignmentReferences.lrn"))(ConsignmentReferences.apply)(
      ConsignmentReferences.unapply
    )

  implicit val format = Json.format[ConsignmentReferences]
  val id = "ConsignmentReferences"

  def form(): Form[ConsignmentReferences] = Form(mapping)
}
