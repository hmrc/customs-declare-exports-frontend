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
import forms.Mapping.requiredRadio
import play.api.data.format.{Formats, Formatter}
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json.{JsString, JsonValidationError, Reads, Writes}
import utils.validators.forms.FieldValidator.isContainedIn

sealed abstract class ModeOfTransportCodes(val value: String)

object ModeOfTransportCodes extends DeclarationPage {

  case object Maritime extends ModeOfTransportCodes("1")
  case object Rail extends ModeOfTransportCodes("2")
  case object Road extends ModeOfTransportCodes("3")
  case object Air extends ModeOfTransportCodes("4")
  case object PostalConsignment extends ModeOfTransportCodes("5")
  case object FixedTransportInstallations extends ModeOfTransportCodes("7")
  case object InlandWaterway extends ModeOfTransportCodes("8")
  case object Unknown extends ModeOfTransportCodes("9")

  val allowedModeOfTransportCodes: Set[ModeOfTransportCodes] =
    Set(Maritime, Rail, Road, Air, PostalConsignment, FixedTransportInstallations, InlandWaterway, Unknown)

  private val reverseLookup: Map[String, ModeOfTransportCodes] = allowedModeOfTransportCodes.map(entry => entry.value -> entry).toMap

  def apply(code: String): Option[ModeOfTransportCodes] = reverseLookup.get(code)

  def formatter(errorKey: String): Formatter[ModeOfTransportCodes] = new Formatter[ModeOfTransportCodes] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ModeOfTransportCodes] =
      Formats.stringFormat
        .bind(key, data)
        .right
        .flatMap(value => reverseLookup.get(value).map(Right.apply).getOrElse(Left(Seq(FormError.apply(key, errorKey)))))

    override def unbind(key: String, code: ModeOfTransportCodes): Map[String, String] =
      Map(key -> code.value)
  }

  implicit val reads: Reads[ModeOfTransportCodes] = Reads.StringReads.collect(JsonValidationError("error.unknown"))(reverseLookup)

  implicit val writes: Writes[ModeOfTransportCodes] = Writes(code => JsString(code.value))

  private val mapping = Forms.single(
    "code" ->
      requiredRadio("declaration.transportInformation.borderTransportMode.error.empty")
        .verifying("declaration.transportInformation.borderTransportMode.error.incorrect", isContainedIn(reverseLookup.keySet))
        .transform[ModeOfTransportCodes](choice => ModeOfTransportCodes(choice).get, choice => choice.value)
  )

  val form: Form[ModeOfTransportCodes] = Form(mapping)
}
