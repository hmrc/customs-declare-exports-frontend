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
import play.api.data.Forms.text
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.{isContainedIn, isEmpty, nonEmpty}
import utils.validators.forms.FieldValidator._

case class FiscalInformation(onwardSupplyRelief: String)

object FiscalInformation {

  implicit val format = Json.format[FiscalInformation]

  object AllowedFiscalInformationAnswers {
    val yes = "Yes"
    val no = "No"
  }

  import AllowedFiscalInformationAnswers._

  val allowedValues: Seq[String] = Seq(yes, no)

  val mapping = Forms.mapping(
    "onwardSupplyRelief" -> text()
      .verifying("declaration.fiscalInformation.onwardSupplyRelief.error", nonEmpty)
      .verifying("declaration.fiscalInformation.onwardSupplyRelief.error", isEmpty or isContainedIn(allowedValues))
  )(FiscalInformation.apply)(FiscalInformation.unapply)

  val formId = "FiscalInformation"

  def form(): Form[FiscalInformation] = Form(mapping)
}
