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

import forms.MetadataPropertiesConvertable
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.FormFieldValidator._

case class OfficeOfExit(id: String) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] = Map("declaration.exitOffice.ID" -> id)
}

object OfficeOfExit {
  implicit val format = Json.format[OfficeOfExit]

  val formId = "OfficeOfExit"

  val mapping = Forms.mapping(
    "officeId" -> text()
      .verifying("supplementary.officeOfExit.empty", _.trim.nonEmpty)
      .verifying("supplementary.officeOfExit.error", isEmpty or (isAlphanumeric and hasSpecificLength(8)))
  )(OfficeOfExit.apply)(OfficeOfExit.unapply)

  def form(): Form[OfficeOfExit] = Form(mapping)
}
