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

package utils.validators.forms.supplementary

import forms.declaration.ItemTypeForm._
import models.declaration.ItemType
import models.requests.JourneyRequest
import play.api.data.Forms.{optional, seq, text}
import play.api.data.{Form, Forms}
import play.api.mvc.AnyContent
import services.NationalAdditionalCode
import utils.validators.forms.FieldValidator._
import utils.validators.forms.{Invalid, Valid, ValidationResult, Validator}

object ItemTypeValidator extends Validator[ItemType] {

  private val taricAdditionalCodeLength = 4
  private val taricAdditionalCodesMaxAmount = 99
  private val nationalAdditionalCodeMaxLength = 4
  private val nationalAdditionalCodesMaxAmount = 99
  private val cusCodeLength = 8
  private val unDangerousGoodsCodeLength = 4
  private val statisticalValueMaxLength = 15
  private val statisticalValueDecimalPlaces = 2

  override def validateOnAddition(element: ItemType)(implicit request: JourneyRequest[AnyContent]): ValidationResult =
    Form(addValidation)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  override def validateOnSaveAndContinue(element: ItemType)(implicit request: JourneyRequest[AnyContent]): ValidationResult =
    Form(submitValidation)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  private val mappingTARICAdditionalCode = seq(
    text()
      .verifying("declaration.itemType.taricAdditionalCodes.error.length", hasSpecificLength(taricAdditionalCodeLength))
      .verifying("declaration.itemType.taricAdditionalCodes.error.specialCharacters", isAlphanumeric)
  ).verifying("declaration.itemType.taricAdditionalCodes.error.maxAmount", codes => codes.size <= taricAdditionalCodesMaxAmount)
    .verifying("declaration.itemType.taricAdditionalCodes.error.duplicate", areAllElementsUnique)

  private val mappingNationalAdditionalCode = seq(
    text()
      .verifying("declaration.itemType.nationalAdditionalCode.error.invalid", isContainedIn(NationalAdditionalCode.all.map(_.value)))
  ).verifying("declaration.itemType.nationalAdditionalCode.error.maxAmount", codes => codes.size <= nationalAdditionalCodesMaxAmount)
    .verifying("declaration.itemType.nationalAdditionalCode.error.duplicate", areAllElementsUnique)

  private val mappingCUSCode = optional(
    text()
      .verifying("declaration.itemType.cusCode.error.length", hasSpecificLength(cusCodeLength))
      .verifying("declaration.itemType.cusCode.error.specialCharacters", isAlphanumeric)
  )

  private val mappingUNDangerousGoodsCode = optional(
    text()
      .verifying("declaration.itemType.unDangerousGoodsCode.error.length", hasSpecificLength(unDangerousGoodsCodeLength))
      .verifying("declaration.itemType.unDangerousGoodsCode.error.specialCharacters", isAlphanumeric)
  )

  private val mappingStatisticalValue = text()
    .verifying("declaration.itemType.statisticalValue.error.empty", nonEmpty)
    .verifying(
      "declaration.itemType.statisticalValue.error.length",
      input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll("\\.", ""))
    )
    .verifying(
      "declaration.itemType.statisticalValue.error.wrongFormat",
      isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces)
    )

  private val addValidation = Forms.mapping(
    taricAdditionalCodeKey -> mappingTARICAdditionalCode,
    nationalAdditionalCodeKey -> mappingNationalAdditionalCode,
    cusCodeKey -> optional(text()),
    unDangerousGoodsCodeKey -> optional(text()),
    statisticalValueKey -> text()
  )(ItemType.apply)(ItemType.unapply)

  private def submitValidation(implicit request: JourneyRequest[AnyContent]) =
    Forms.mapping(
      taricAdditionalCodeKey -> mappingTARICAdditionalCode,
      nationalAdditionalCodeKey -> mappingNationalAdditionalCode,
      cusCodeKey -> mappingCUSCode,
      unDangerousGoodsCodeKey -> mappingUNDangerousGoodsCode,
      statisticalValueKey -> mappingStatisticalValue
    )(ItemType.apply)(ItemType.unapply)

}
