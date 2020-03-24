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
import forms.DeclarationPage
import models.DeclarationType.{CLEARANCE, DeclarationType}
import play.api.data.{Form, Forms}
import play.api.libs.json.Json

case class ExporterDetails(details: EntityDetails)

object ExporterDetails extends DeclarationPage {
  implicit val format = Json.format[ExporterDetails]

  val id = "ExporterDetails"

  val defaultMapping = Forms.mapping("details" -> EntityDetails.mapping)(ExporterDetails.apply)(ExporterDetails.unapply)

  val optionalMapping = Forms.mapping("details" -> EntityDetails.optionalMapping)(ExporterDetails.apply)(ExporterDetails.unapply)

  def form(declarationType: DeclarationType): Form[ExporterDetails] = declarationType match {
    case CLEARANCE => Form(optionalMapping)
    case _         => Form(defaultMapping)
  }
}
