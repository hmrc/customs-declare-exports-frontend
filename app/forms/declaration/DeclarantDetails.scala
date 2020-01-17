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
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class DeclarantDetails(details: EntityDetails)

object DeclarantDetails extends DeclarationPage {
  implicit val format = Json.format[DeclarantDetails]

  val id = "DeclarantDetails"

  val declarantMapping = Forms
    .mapping(
      "eori" ->
        text()
          .verifying("supplementary.eori.empty", nonEmpty)
          .verifying("supplementary.eori.error.format", isEmpty or (isValidEORIPattern and noLongerThan(17) and noShorterThan(3)))
    )(eori => EntityDetails(Some(eori), None))(entityDetails => entityDetails.eori)

  val mapping = Forms.mapping("details" -> declarantMapping)(DeclarantDetails.apply)(DeclarantDetails.unapply)

  def form(): Form[DeclarantDetails] = Form(mapping)
}
