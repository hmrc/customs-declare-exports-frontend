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

import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class NatureOfTransaction(natureType: String)

object NatureOfTransaction {
  implicit val format = Json.format[NatureOfTransaction]

  val formId = "TransactionType"

  val Purchase = "1"
  val Return = "2"
  val Donation = "3"
  val Processing = "4"
  val Processed = "5"
  val NationalPurposes = "6"
  val Military = "7"
  val Construction = "8"
  val Other = "9"

  val allowedTypes =
    Set(Purchase, Return, Donation, Processing, Processed, NationalPurposes, Military, Construction, Other)

  val mapping = Forms.mapping(
    "natureType" -> text()
      .verifying("declaration.natureOfTransaction.empty", nonEmpty)
      .verifying("declaration.natureOfTransaction.error", isEmpty or isContainedIn(allowedTypes))
  )(NatureOfTransaction.apply)(NatureOfTransaction.unapply)

  def adjustErrors(form: Form[NatureOfTransaction]): Form[NatureOfTransaction] = {
    val newErrors = form.errors.map { error =>
      if (error.message == "error.required") error.copy(messages = Seq("declaration.natureOfTransaction.empty"))
      else error
    }

    form.copy(errors = newErrors)
  }

  def form(): Form[NatureOfTransaction] = Form(mapping)
}
