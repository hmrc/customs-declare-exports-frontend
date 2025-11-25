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

import connectors.CodeListConnector
import forms.DeclarationPage
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import models.{Amendment, FieldMapping}
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json._
import services.Countries.isValidCountryCode
import utils.validators.forms.FieldValidator._

case class TransportCountry(countryCode: Option[String]) extends Ordered[TransportCountry] with Amendment {

  override def compare(that: TransportCountry): Int =
    (countryCode, that.countryCode) match {
      case (None, None)                    => 0
      case (_, None)                       => 1
      case (None, _)                       => -1
      case (Some(current), Some(original)) => current.compare(original)
    }

  def value: String = countryCode.getOrElse("")
}

object TransportCountry extends DeclarationPage with FieldMapping {
  implicit val format: OFormat[TransportCountry] = Json.format[TransportCountry]

  val pointer: String = "meansOfTransportCrossingTheBorderNationality"

  val transportCountry = "transport-country"

  val prefix = "declaration.transportInformation.transportCountry"

  def form(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Form[TransportCountry] =
    Form(mapping(transportMode))

  private def mapping(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Mapping[TransportCountry] =
    Forms.mapping(
      transportCountry -> text
        .verifying(s"$prefix.country.error.invalid", input => input.isEmpty or isValidCountryCode(input))
    )(country => TransportCountry(Some(country)))(_.countryCode)


  //  private def mapping(transportMode: String)(implicit messages: Messages, connector: CodeListConnector): Mapping[TransportCountry] =
//    Forms.mapping(
//      transportCountry -> text
//        .verifying(s"$prefix.country.error.invalid", input => input.isEmpty or isValidCountryCode(input))
//    )(country => if(country.isEmpty) {
//      System.out.println("$$$")
//      TransportCountry(Some("Not provided"))
//    } else TransportCountry(Some(country)))(_.countryCode)


  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey("tariff.declaration.transportCountry.common"))
}
