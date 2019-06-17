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

import play.api.data.{Form, Forms}
import play.api.data.Forms.{optional, text}
import play.api.libs.json.Json

case class AdditionalFiscalReference(reference: Option[String])

object AdditionalFiscalReference {
  implicit val format = Json.format[AdditionalFiscalReference]

  val mapping = Forms.mapping(
    "additionalFiscalReference" -> optional(text())
  )(AdditionalFiscalReference.apply)(
    AdditionalFiscalReference.unapply)

  val form: Form[AdditionalFiscalReference] = Form(mapping)
}

case class AdditionalFiscalReferencesData(references: Seq[AdditionalFiscalReference])

object AdditionalFiscalReferencesData {
  implicit val format = Json.format[AdditionalFiscalReferencesData]

  val formId = "AdditionalFiscalReferences"

  val limit = 99
}
