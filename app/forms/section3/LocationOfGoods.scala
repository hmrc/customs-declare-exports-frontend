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

package forms.section3

import connectors.CodeListConnector
import forms.DeclarationPage
import forms.common.YesNoAnswer
import forms.common.YesNoAnswer.YesNoAnswers
import forms.mappings.MappingHelper.requiredRadio
import models.DeclarationType.DeclarationType
import models.declaration.GoodsLocation
import models.viewmodels.TariffContentKey
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import services.Countries.isValidCountryCode
import services.view.GoodsLocationCodes
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class LocationOfGoods(code: String) {

  def toModel: GoodsLocation = GoodsLocation(
    country = code.slice(0, 2),
    typeOfLocation = code.slice(2, 3),
    qualifierOfIdentification = code.slice(3, 4),
    identificationOfLocation = code.drop(4)
  )
}

object LocationOfGoods extends DeclarationPage {

  implicit val format: OFormat[LocationOfGoods] = Json.format[LocationOfGoods]

  val suffixForGVMS = "GVM"

  val formId = "Location"

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

  def form(version: Int)(implicit messages: Messages, codeListConnector: CodeListConnector): Form[LocationOfGoods] =
    version match {
      case 3 | 5 => Form(mappingForVersion3And5)
      case _     => Form(mappingForOtherVersions(version == 7))
    }

  val radioGroupId = "locationOfGoods"
  val userChoice = "userChoice"
  val locationId = "glc"

  private val extractor = """.+ - \(([A-Z]+)\)""".r

  def gvmsGoodsLocationsForArrivedDecls(implicit messages: Messages): Seq[String] =
    (1 to 8).map { ix =>
      val extractor(code) = messages(s"declaration.locationOfGoods.radio.$ix")
      code
    } :+ userChoice

  private def mappingForVersion3And5(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[LocationOfGoods] =
    Forms.mapping(
      radioGroupId -> requiredRadio("declaration.locationOfGoods.error.empty", gvmsGoodsLocationsForArrivedDecls),
      userChoice -> conditionalFieldMapping(radioGroupId, userChoice)
    )(form2Data)(model2FormV3AndV5)

  private def mappingForOtherVersions(
    cseCodesOnly: Boolean
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[LocationOfGoods] =
    Forms.mapping(
      radioGroupId -> requiredRadio("declaration.locationOfGoods.code.required", YesNoAnswer.allowedValues),
      locationId -> conditionalFieldMapping(radioGroupId, YesNoAnswers.yes),
      "code" -> conditionalFieldMapping(radioGroupId, YesNoAnswers.no)
    )(form2Data)(model2Form(_, cseCodesOnly))

  private def conditionalFieldMapping(
    fieldId: String,
    answer: String
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Mapping[Option[String]] =
    mandatoryIfEqual(
      fieldId,
      answer,
      text()
        .transform(_.trim, (s: String) => s)
        .verifying("declaration.locationOfGoods.code.empty", nonEmpty)
        .verifying("declaration.locationOfGoods.code.error", isEmpty or isValidFormat)
        .verifying("declaration.locationOfGoods.code.error.length", isEmpty or (noShorterThan(10) and noLongerThan(39)))
    )

  private def isValidFormat(implicit messages: Messages, codeListConnector: CodeListConnector): String => Boolean =
    value =>
      validateCountry(messages, codeListConnector)(value) and
        validateLocationType(value) and
        validateQualifierCode(value) and
        isAlphanumeric(value)

  /**
   * Country is in two first characters in Location Code
   */
  private def validateCountry(implicit messages: Messages, codeListConnector: CodeListConnector): String => Boolean =
    (input: String) => {
      val countryCode = input.take(2).toUpperCase
      isValidCountryCode(countryCode)
    }

  private def form2Data(yesNo: String, search: Option[String], code: Option[String]): LocationOfGoods =
    (yesNo, search, code) match {
      case (YesNoAnswers.yes, Some(code), None) => LocationOfGoods(code.toUpperCase)
      case (YesNoAnswers.no, None, Some(code))  => LocationOfGoods(code.toUpperCase)
    }

  private def form2Data(radioId: String, maybeCode: Option[String]): LocationOfGoods =
    (radioId, maybeCode) match {
      case (`userChoice`, Some(code)) => LocationOfGoods(code.toUpperCase)
      case (code, None)               => LocationOfGoods(code.toUpperCase)
    }

  private def model2Form(
    locationOfGoods: LocationOfGoods,
    cseCodesOnly: Boolean
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Option[(String, Option[String], Option[String])] =
    GoodsLocationCodes
      .findByCode(locationOfGoods.code, cseCodesOnly)
      .map(_ => ("Yes", Some(locationOfGoods.code), None))
      .orElse(Some(("No", None, Some(locationOfGoods.code))))

  private def model2FormV3AndV5(
    locationOfGoods: LocationOfGoods
  )(implicit messages: Messages, codeListConnector: CodeListConnector): Option[(String, Option[String])] =
    GoodsLocationCodes
      .findByCode(locationOfGoods.code)
      .map { _ =>
        if (gvmsGoodsLocationsForArrivedDecls.contains(locationOfGoods.code)) (locationOfGoods.code, None)
        else (userChoice, Some(locationOfGoods.code))
      }
      .orElse(Some((userChoice, Some(locationOfGoods.code))))

  override def defineTariffContentKeys(decType: DeclarationType): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"tariff.declaration.locationOfGoods.${DeclarationPage.getJourneyTypeSpecialisation(decType)}"))
}
