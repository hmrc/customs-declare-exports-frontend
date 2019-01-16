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

package forms.supplementary

import play.api.data.{Form, Forms}
import play.api.data.Forms._
import play.api.libs.json.Json

case class AddDocument(
  enterDocumentTypeCode: String,
  identifier: String,
  status: String,
  issuingAuthority: String,
  dateOfValidity: String,
  measurementUnitAndQualifier: String,
  additionalInformation: Boolean
)

object Document {
  implicit val format = Json.format[AddDocument]

  val formId = "AddDocument"

  val mapping = Forms.mapping(
    "enterDocumentTypeCode" -> text(),
    "identifier" -> text(),
    "status" -> text(),
    "issuingAuthority" -> text(),
    "dateOfValidity" -> text(),
    "measurementUnitAndQualifier" -> text(),
    "additionalInformation" -> boolean
  )(AddDocument.apply)(AddDocument.unapply)

  def form(): Form[AddDocument] = Form(mapping)

  def toMetadataProperties(document: AddDocument): Map[String, String] = ???
}
