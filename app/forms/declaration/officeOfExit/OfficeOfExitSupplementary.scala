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

package forms.declaration.officeOfExit

import forms.MetadataPropertiesConvertable
import play.api.data.Forms
import play.api.data.Forms.text
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class OfficeOfExitSupplementary(officeId: String) extends MetadataPropertiesConvertable {

  override def toMetadataProperties(): Map[String, String] = Map("declaration.exitOffice.id" -> officeId)
}

object OfficeOfExitSupplementary {
  implicit val format = Json.format[OfficeOfExitSupplementary]

  val mapping = Forms.mapping(
    "officeId" -> text()
      .verifying("declaration.officeOfExit.empty", nonEmpty)
      .verifying("declaration.officeOfExit.length", isEmpty or hasSpecificLength(8))
      .verifying("declaration.officeOfExit.specialCharacters", isEmpty or isAlphanumeric)
  )(OfficeOfExitSupplementary.apply)(OfficeOfExitSupplementary.unapply)
}
