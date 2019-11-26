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

import forms.Mapping.requiredRadio
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

case class NatureOfTransaction(natureType: String) {
  import NatureOfTransaction._

  def extractNatureType: String = natureType match {
    case Purchase         => "Purchase"
    case Return           => "Return"
    case Donation         => "Donation"
    case Processing       => "Processing"
    case Processed        => "Processed"
    case NationalPurposes => "National purposes"
    case Military         => "Military"
    case Construction     => "Construction"
    case Other            => "Other"
    case _                => "Invalid"
  }
}

object NatureOfTransaction {
  implicit val format: OFormat[NatureOfTransaction] = Json.format[NatureOfTransaction]

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

  val allowedTypes: Set[String] =
    Set(Purchase, Return, Donation, Processing, Processed, NationalPurposes, Military, Construction, Other)

  val mapping: Mapping[NatureOfTransaction] = Forms.mapping(
    "natureType" -> requiredRadio("declaration.natureOfTransaction.empty")
      .verifying("declaration.natureOfTransaction.error", isContainedIn(allowedTypes))
  )(NatureOfTransaction.apply)(NatureOfTransaction.unapply)

  def form(): Form[NatureOfTransaction] = Form(mapping)
}
