/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{JsString, JsonValidationError, Reads, Writes}

sealed abstract class ModeOfTransportCode(val value: String)

object ModeOfTransportCode {

  case object Maritime extends ModeOfTransportCode("1")
  case object Rail extends ModeOfTransportCode("2")
  case object Road extends ModeOfTransportCode("3")
  case object Air extends ModeOfTransportCode("4")
  case object PostalConsignment extends ModeOfTransportCode("5")
  case object RoRo extends ModeOfTransportCode("6")
  case object FixedTransportInstallations extends ModeOfTransportCode("7")
  case object InlandWaterway extends ModeOfTransportCode("8")
  case object Unknown extends ModeOfTransportCode("9")
  case object Empty extends ModeOfTransportCode("no-code")

  val meaningfulModeOfTransportCodes: Set[ModeOfTransportCode] =
    Set(Maritime, Rail, Road, Air, PostalConsignment, RoRo, FixedTransportInstallations, InlandWaterway, Unknown)

  def classicFormatter(errorKey: String): Formatter[ModeOfTransportCode] = formatter(meaningfulModeOfTransportCodes, errorKey)

  def clearanceJourneyFormatter(errorKey: String): Formatter[ModeOfTransportCode] = formatter(meaningfulModeOfTransportCodes + Empty, errorKey)

  private def formatter(modeOfTransportCodesAllowed: Set[ModeOfTransportCode], errorKey: String): Formatter[ModeOfTransportCode] =
    new Formatter[ModeOfTransportCode] {
      private val valueToCodeMapping = modeOfTransportCodesAllowed.map(entry => entry.value -> entry).toMap

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ModeOfTransportCode] =
        data
          .get(key)
          .flatMap(value => valueToCodeMapping.get(value).map(Right.apply))
          .getOrElse(Left(Seq(FormError.apply(key, errorKey))))

      override def unbind(key: String, code: ModeOfTransportCode): Map[String, String] =
        Map(key -> code.value)
    }

  private val valueToCodeAll: Map[String, ModeOfTransportCode] =
    (meaningfulModeOfTransportCodes + Empty).map(entry => entry.value -> entry).toMap

  implicit val reads: Reads[ModeOfTransportCode] = Reads.StringReads.collect(JsonValidationError("error.unknown"))(valueToCodeAll)

  implicit val writes: Writes[ModeOfTransportCode] = Writes(code => JsString(code.value))
}
