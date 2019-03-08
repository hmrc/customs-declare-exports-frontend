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

import forms.supplementary.ItemType
import forms.supplementary.ItemType.{nationalAdditionalCodesKey, taricAdditionalCodesKey}
import play.api.data.Forms.{optional, seq, text}
import play.api.data.{Form, Forms}
import utils.validators.forms.FieldValidator._
import utils.validators.forms.{Invalid, Valid, ValidationResult, Validator}

object ItemTypeValidator extends Validator[ItemType] {

  private val combinedNomenclatureCodeMaxLength = 8
  private val taricAdditionalCodeLength = 4
  private val taricAdditionalCodesMaxAmount = 99
  private val nationalAdditionalCodeMaxLength = 4
  private val nationalAdditionalCodesMaxAmount = 99
  private val descriptionOfGoodsMaxLength = 280
  private val cusCodeLength = 8
  private val statisticalValueMaxLength = 15
  private val statisticalValueDecimalPlaces = 2

  override def validateOnAddition(element: ItemType): ValidationResult =
    Form(mappingWithValidationForAddition)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  override def validateOnSaveAndContinue(element: ItemType): ValidationResult =
    Form(mappingWithValidation)
      .fillAndValidate(element)
      .fold[ValidationResult](formWithErrors => Invalid(formWithErrors.errors), _ => Valid)

  val mappingWithValidationForAddition = Forms.mapping(
    "combinedNomenclatureCode" -> text(),
    taricAdditionalCodesKey -> seq(
      text()
        .verifying(
          "supplementary.itemType.taricAdditionalCodes.error.length",
          hasSpecificLength(taricAdditionalCodeLength)
        )
        .verifying("supplementary.itemType.taricAdditionalCodes.error.specialCharacters", isAlphanumeric)
    ).verifying(
        "supplementary.itemType.taricAdditionalCodes.error.maxAmount",
        codes => codes.size <= taricAdditionalCodesMaxAmount
      )
      .verifying("supplementary.itemType.taricAdditionalCodes.error.duplicate", areAllElementsUnique),
    nationalAdditionalCodesKey -> seq(
      text()
        .verifying(
          "supplementary.itemType.nationalAdditionalCode.error.length",
          noLongerThan(nationalAdditionalCodeMaxLength)
        )
        .verifying("supplementary.itemType.nationalAdditionalCode.error.specialCharacters", isAlphanumeric)
    ).verifying(
        "supplementary.itemType.nationalAdditionalCode.error.maxAmount",
        codes => codes.size <= nationalAdditionalCodesMaxAmount
      )
      .verifying("supplementary.itemType.nationalAdditionalCode.error.duplicate", areAllElementsUnique),
    "descriptionOfGoods" -> text(),
    "cusCode" -> optional(text()),
    "statisticalValue" -> text()
  )(ItemType.apply)(ItemType.unapply)

  val mappingWithValidation = Forms.mapping(
    "combinedNomenclatureCode" -> text()
      .verifying("supplementary.itemType.combinedNomenclatureCode.error.empty", nonEmpty)
      .verifying(
        "supplementary.itemType.combinedNomenclatureCode.error.length",
        isEmpty or noLongerThan(combinedNomenclatureCodeMaxLength)
      )
      .verifying("supplementary.itemType.combinedNomenclatureCode.error.specialCharacters", isEmpty or isAlphanumeric),
    "taricAdditionalCode" -> seq(
      text()
        .verifying(
          "supplementary.itemType.taricAdditionalCodes.error.length",
          hasSpecificLength(taricAdditionalCodeLength)
        )
        .verifying("supplementary.itemType.taricAdditionalCodes.error.specialCharacters", isAlphanumeric)
    ).verifying(
        "supplementary.itemType.taricAdditionalCodes.error.maxAmount",
        codes => codes.size <= taricAdditionalCodesMaxAmount
      )
      .verifying("supplementary.itemType.taricAdditionalCodes.error.duplicate", areAllElementsUnique),
    "nationalAdditionalCode" -> seq(
      text()
        .verifying(
          "supplementary.itemType.nationalAdditionalCode.error.length",
          noLongerThan(nationalAdditionalCodeMaxLength)
        )
        .verifying("supplementary.itemType.nationalAdditionalCode.error.specialCharacters", isAlphanumeric)
    ).verifying(
        "supplementary.itemType.nationalAdditionalCode.error.maxAmount",
        codes => codes.size <= nationalAdditionalCodesMaxAmount
      )
      .verifying("supplementary.itemType.nationalAdditionalCode.error.duplicate", areAllElementsUnique),
    "descriptionOfGoods" -> text()
      .verifying("supplementary.itemType.description.error.empty", nonEmpty)
      .verifying(
        "supplementary.itemType.description.error.length",
        isEmpty or noLongerThan(descriptionOfGoodsMaxLength)
      ),
    "cusCode" -> optional(
      text()
        .verifying("supplementary.itemType.cusCode.error.length", hasSpecificLength(cusCodeLength))
        .verifying("supplementary.itemType.cusCode.error.specialCharacters", isAlphanumeric)
    ),
    "statisticalValue" -> text()
      .verifying("supplementary.itemType.statisticalValue.error.empty", nonEmpty)
      .verifying(
        "supplementary.itemType.statisticalValue.error.length",
        input => input.isEmpty || noLongerThan(statisticalValueMaxLength)(input.replaceAll("\\.", ""))
      )
      .verifying(
        "supplementary.itemType.statisticalValue.error.wrongFormat",
        isEmpty or isDecimalWithNoMoreDecimalPlacesThan(statisticalValueDecimalPlaces)
      )
  )(ItemType.apply)(ItemType.unapply)

}
