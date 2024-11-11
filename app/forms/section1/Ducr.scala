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

package forms.section1

import forms.DeclarationPage
import models.requests.JourneyRequest
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

import java.time.ZoneId

case class Ducr(ducr: String)

object Ducr extends DeclarationPage {

  implicit val format: OFormat[Ducr] = Json.format[Ducr]

  val mapping: Mapping[Ducr] =
    Forms.mapping(
      "ducr" ->
        text()
          .transform(_.toUpperCase, identity[String])
          .verifying("declaration.consignmentReferences.ducr.error.empty", nonEmpty)
          .verifying("declaration.consignmentReferences.ducr.error.invalid", isEmpty or isValidDucr)
    )(form2Data)(Ducr.unapply)

  val form: Form[Ducr] = Form(mapping)

  def form2Data(ducr: String): Ducr = new Ducr(ducr)

  def model2Form: Ducr => Option[String] =
    model => Some(model.ducr)

  def generateDucrPrefix(implicit request: JourneyRequest[_]): String = {
    val lastDigitOfYear = request.cacheModel.declarationMeta.createdDateTime.atZone(ZoneId.of("Europe/London")).getYear.toString.last
    val eori = request.eori.toUpperCase
    s"${lastDigitOfYear}${eori}-"
  }
}
