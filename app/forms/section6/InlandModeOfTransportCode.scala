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
import models.ExportsFieldPointer.ExportsFieldPointer
import models.{Amendment, FieldMapping}
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}

case class InlandModeOfTransportCode(inlandModeOfTransportCode: Option[ModeOfTransportCode] = None)
    extends Ordered[InlandModeOfTransportCode] with Amendment {

  override def compare(that: InlandModeOfTransportCode): Int =
    (inlandModeOfTransportCode, that.inlandModeOfTransportCode) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }

  def value: String = inlandModeOfTransportCode.fold("")(_.toString)
}

object InlandModeOfTransportCode extends DeclarationPage with FieldMapping {

  val pointer: ExportsFieldPointer = "inlandModeOfTransportCode.inlandModeOfTransportCode"

  implicit val format: OFormat[InlandModeOfTransportCode] = Json.format[InlandModeOfTransportCode]

  val formId = "InlandModeOfTransportCode"

  private val mapping = Forms
    .mapping(
      "inlandModeOfTransportCode" ->
        optional(of(ModeOfTransportCode.formatter("declaration.warehouse.inlandTransportDetails.error.incorrect")))
    )(InlandModeOfTransportCode.apply)(InlandModeOfTransportCode.unapply)

  def form: Form[InlandModeOfTransportCode] = Form(mapping)
}
