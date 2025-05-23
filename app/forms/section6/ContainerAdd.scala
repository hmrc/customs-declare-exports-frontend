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

package forms.section6

import forms.DeclarationPage
import play.api.data.Forms.{optional, text}
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class ContainerAdd(id: Option[String])

object ContainerAdd extends DeclarationPage {
  implicit val format: OFormat[ContainerAdd] = Json.format[ContainerAdd]

  val maxContainerIdLength = 17
  val containerIdKey = "id"

  val mapping = Forms.mapping(
    "id" -> optional(
      text()
        .verifying("declaration.transportInformation.containerId.error.invalid", isAlphanumeric)
        .verifying("declaration.transportInformation.containerId.error.length", noLongerThan(maxContainerIdLength))
    ).verifying("declaration.transportInformation.containerId.empty", isSome(_))
  )(ContainerAdd.apply)(ContainerAdd.unapply)

  def form: Form[ContainerAdd] = Form(mapping)
}
