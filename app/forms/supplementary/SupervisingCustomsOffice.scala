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
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class SupervisingCustomsOffice(office: Option[String]) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] =
    Map("declaration.supervisingOffice.id" -> office.getOrElse(""))
}

object SupervisingCustomsOffice {
  implicit val format = Json.format[SupervisingCustomsOffice]

  val mapping = Forms.mapping(
    "supervisingCustomsOffice" -> optional(
      text().verifying("supplementary.supervisingCustomsOffice.error", isAlphanumeric and hasSpecificLength(8))
    )
  )(SupervisingCustomsOffice.apply)(SupervisingCustomsOffice.unapply)

  val formId = "SupervisingCustomsOffice"

  def form(): Form[SupervisingCustomsOffice] = Form(mapping)
}
