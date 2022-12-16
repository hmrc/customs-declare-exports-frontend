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

import connectors.CodeListConnector
import forms.{AdditionalConstraintsMapping, ConditionalConstraint, DeclarationPage}
import forms.MappingHelper.requiredRadio
import forms.common.YesNoAnswer
import models.declaration.GoodsLocation
import models.DeclarationType.DeclarationType
import models.viewmodels.TariffContentKey
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms.text
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.Countries.isValidCountryCode
import services.GoodsLocationCodes
import utils.validators.forms.FieldValidator._

import scala.concurrent.Future

case class LocationOfGoods(code: String) {

  def toModel(): GoodsLocation = GoodsLocation(
    country = code.slice(0, 2),
    typeOfLocation = code.slice(2, 3),
    qualifierOfIdentification = code.slice(3, 4),
    identificationOfLocation = code.drop(4)
  )

}

object LocationOfGoods extends DeclarationPage {

  implicit val format: OFormat[LocationOfGoods] = Json.format[LocationOfGoods]

  val formId = "Location"

  /**
   * Country is in two first characters in Location Code
   */
  private def validateCountry(implicit messages: Messages, codeListConnector: CodeListConnector): String => Boolean =
    (input: String) => {
      val countryCode = input.take(2).toUpperCase
      isValidCountryCode(countryCode)
    }

  /**
   * Location Type is defined as third character in Location Code
   */
  private val validateLocationType: String => Boolean = (input: String) => {
    val correctLocationType: Set[String] = Set("A", "B", "C", "D")
    val predicate = isContainedIn(correctLocationType)
    input.drop(2).toUpperCase.headOption.map(_.toString).exists(predicate)
  }

  /**
   * Qualifier Code is defined in fourth characted in Location Code
   */
  private val validateQualifierCode: String => Boolean = (input: String) => {
    val correctQualifierCode: Set[String] = Set("U", "Y")
    val predicate = isContainedIn(correctQualifierCode)
    input.drop(3).toUpperCase.headOption.map(_.toString).exists(predicate)
  }

  private def mapping(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[LocationOfGoods] =
    Forms.mapping(
      "yesNo" -> requiredRadio("error.yesNo.required", YesNoAnswer.allowedValues),
      "glc" ->
        AdditionalConstraintsMapping(
          text().transform(_.trim, (s: String) => s),
          Seq(
            ConditionalConstraint(_.get("yesNo").exists(_.nonEmpty), "declaration.locationOfGoods.code.empty", nonEmpty),
            ConditionalConstraint(_.get("yesNo").exists(_.nonEmpty), "declaration.locationOfGoods.code.error", isEmpty or isValidFormat),
            ConditionalConstraint(
              _.get("yesNo").exists(_.nonEmpty),
              "declaration.locationOfGoods.code.error.length",
              isEmpty or (noShorterThan(10) and noLongerThan(39))
            )
          )
        ),
      "code" ->
        AdditionalConstraintsMapping(
          text().transform(_.trim, (s: String) => s),
          Seq(
            ConditionalConstraint(_.get("yesNo").exists(_.nonEmpty), "declaration.locationOfGoods.code.empty", nonEmpty),
            ConditionalConstraint(_.get("yesNo").exists(_.nonEmpty), "declaration.locationOfGoods.code.error", isEmpty or isValidFormat),
            ConditionalConstraint(
              _.get("yesNo").exists(_.nonEmpty),
              "declaration.locationOfGoods.code.error.length",
              isEmpty or (noShorterThan(10) and noLongerThan(39))
            )
          )
        )
    )(form2Data)(model2Form)

  private def isValidFormat(implicit messages: Messages, codeListConnector: CodeListConnector): String => Boolean =
    value =>
      validateCountry(messages, codeListConnector)(value) and
        validateLocationType(value) and
        validateQualifierCode(value) and
        isAlphanumeric(value)

  private def form2Data(yesNo: String, search: String, code: String): LocationOfGoods = LocationOfGoods(code.toUpperCase)

  private def model2Form(
    locationOfGoods: LocationOfGoods
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Option[(String, String, String)] =
    GoodsLocationCodes.findByCode(locationOfGoods.code) map (_ => ("Yes", "", locationOfGoods.code)) orElse Some(("No", "", locationOfGoods.code))

  def form()(implicit messages: Messages, codeListConnector: CodeListConnector): Form[LocationOfGoods] = Form(mapping)

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.locationOfGoods.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
