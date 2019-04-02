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

package forms.declaration.validators

import base.TestHelper
import forms.declaration.ItemType
import forms.declaration.ItemType._
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import utils.validators.forms.supplementary.ItemTypeValidator
import utils.validators.forms.{Invalid, Valid}

class ItemTypeValidatorSpec extends WordSpec with MustMatchers {
  import ItemTypeValidatorSpec._

  "ItemTypeValidator on validateOnAddition" should {

    "return Invalid result with errors" when {

      "any TARIC additional code is not 4 characters long" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333", "4444"))
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(s"$taricAdditionalCodesKey[2]", "declaration.itemType.taricAdditionalCodes.error.length"))
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "any TARIC additional code contains special characters" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333$", "4444"))
        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError(s"$taricAdditionalCodesKey[2]", "declaration.itemType.taricAdditionalCodes.error.specialCharacters")
          )
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is more than 99 TARIC additional codes" in {
        val itemType = buildItemType(taricAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomString(4)))
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(taricAdditionalCodesKey, "declaration.itemType.taricAdditionalCodes.error.maxAmount"))
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is duplicated TARIC additional code" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(taricAdditionalCodesKey, "declaration.itemType.taricAdditionalCodes.error.duplicate"))
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "any National additional code is longer than 4 characters" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "2222", "33333", "4444"))
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(s"$nationalAdditionalCodesKey[2]", "declaration.itemType.nationalAdditionalCode.error.length"))
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "any National additional code contains special characters" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "2222", "333%", "4444"))
        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError(
              s"$nationalAdditionalCodesKey[2]",
              "declaration.itemType.nationalAdditionalCode.error.specialCharacters"
            )
          )
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is more than 99 National additional codes" in {
        val itemType = buildItemType(nationalAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomString(4)))
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(nationalAdditionalCodesKey, "declaration.itemType.nationalAdditionalCode.error.maxAmount"))
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is duplicated National additional code" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(nationalAdditionalCodesKey, "declaration.itemType.nationalAdditionalCode.error.duplicate"))
        )

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

    }

    "return Valid result" when {

      "provided with correct data with single value for multi-value fields" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("AB12"), nationalAdditionalCode = Seq("CD34"))

        ItemTypeValidator.validateOnAddition(itemType) must be(Valid)
      }

      "provided with correct data with multiple values for multi-value fields" in {
        val itemType =
          buildItemType(taricAdditionalCode = Seq("AB12", "Q123"), nationalAdditionalCode = Seq("CD34", "TRY", "T45T"))

        ItemTypeValidator.validateOnAddition(itemType) must be(Valid)
      }

      "provided with empty mandatory fields and correct values for multi-value fields" in {
        val itemType =
          buildItemType(taricAdditionalCode = Seq("AB12", "Q123"), nationalAdditionalCode = Seq("CD34", "TRY", "T45T"))

        ItemTypeValidator.validateOnAddition(itemType) must be(Valid)
      }

    }

    def testFailedValidationOnAddition(input: ItemType, expectedResult: Invalid): Unit =
      ItemTypeValidator.validateOnAddition(input) match {
        case validationResult: Invalid => expectedResult.errors.foreach(validationResult.errors must contain(_))
        case Valid                     => fail()
      }
  }

  "ItemTypeValidator on validateOnSaveAndContinue" should {

    "return Failure result with errors" when {

      "Combined Nomenclature Code is empty" in {
        val itemType = buildItemType()
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(combinedNomenclatureCodeKey, "declaration.itemType.combinedNomenclatureCode.error.empty"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Combined Nomenclature Code is longer than 8 characters" in {
        val itemType = buildItemType(combinedNomenclatureCode = "ABCD12345")
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(combinedNomenclatureCodeKey, "declaration.itemType.combinedNomenclatureCode.error.length"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Combined Nomenclature Code contains special characters" in {
        val itemType = buildItemType(combinedNomenclatureCode = "1234!@#$")
        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError(
              combinedNomenclatureCodeKey,
              "declaration.itemType.combinedNomenclatureCode.error.specialCharacters"
            )
          )
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "any TARIC additional code is not 4 characters long" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333", "4444"))
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(s"$taricAdditionalCodesKey[2]", "declaration.itemType.taricAdditionalCodes.error.length"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "any TARIC additional code contains special characters" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333$", "4444"))
        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError(s"$taricAdditionalCodesKey[2]", "declaration.itemType.taricAdditionalCodes.error.specialCharacters")
          )
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is more than 99 TARIC additional codes" in {
        val itemType = buildItemType(taricAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomString(4)))
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(taricAdditionalCodesKey, "declaration.itemType.taricAdditionalCodes.error.maxAmount"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is duplicated TARIC additional code" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(taricAdditionalCodesKey, "declaration.itemType.taricAdditionalCodes.error.duplicate"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "any National additional code is longer than 4 characters" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "2222", "33333", "4444"))
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(s"$nationalAdditionalCodesKey[2]", "declaration.itemType.nationalAdditionalCode.error.length"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "any National additional code contains special characters" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "2222", "333%", "4444"))
        val expectedValidationResult = Invalid(
          errors = Seq(
            FormError(
              s"$nationalAdditionalCodesKey[2]",
              "declaration.itemType.nationalAdditionalCode.error.specialCharacters"
            )
          )
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is more than 99 National additional codes" in {
        val itemType = buildItemType(nationalAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomString(4)))
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(nationalAdditionalCodesKey, "declaration.itemType.nationalAdditionalCode.error.maxAmount"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is duplicated National additional code" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult = Invalid(
          errors =
            Seq(FormError(nationalAdditionalCodesKey, "declaration.itemType.nationalAdditionalCode.error.duplicate"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Description of goods is empty" in {
        val itemType = buildItemType()
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(descriptionOfGoodsKey, "declaration.itemType.description.error.empty")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Description of goods is longer than 280 characters" in {
        val itemType = buildItemType(descriptionOfGoods = TestHelper.createRandomString(281))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(descriptionOfGoodsKey, "declaration.itemType.description.error.length")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "CUS code is not 8 characters long" in {
        val itemType = buildItemType(cusCode = Some("ABC123"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(cusCodeKey, "declaration.itemType.cusCode.error.length")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "CUS code contains special characters" in {
        val itemType = buildItemType(cusCode = Some("ABC123#$"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(cusCodeKey, "declaration.itemType.cusCode.error.specialCharacters")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "UN Dangerous Goods Code is longer than 4 characters" in {
        val itemType = buildItemType(unDangerousGoodsCode = Some("ABCD5"))
        val expectedValidationResult =
          Invalid(
            errors = Seq(FormError(unDangerousGoodsCodeKey, "declaration.itemType.unDangerousGoodsCode.error.length"))
          )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "UN Dangerous Goods Code is shorter than 4 characters" in {
        val itemType = buildItemType(unDangerousGoodsCode = Some("123"))
        val expectedValidationResult =
          Invalid(
            errors = Seq(FormError(unDangerousGoodsCodeKey, "declaration.itemType.unDangerousGoodsCode.error.length"))
          )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "UN Dangerous Goods Code contains special characters" in {
        val itemType = buildItemType(unDangerousGoodsCode = Some("A7#$"))
        val expectedValidationResult =
          Invalid(
            errors = Seq(
              FormError(unDangerousGoodsCodeKey, "declaration.itemType.unDangerousGoodsCode.error.specialCharacters")
            )
          )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Statistical value is empty" in {
        val itemType = buildItemType()
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(statisticalValueKey, "declaration.itemType.statisticalValue.error.empty")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Statistical value contains more than 15 digits" in {
        val itemType = buildItemType(statisticalValue = "12345678901234.56")
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(statisticalValueKey, "declaration.itemType.statisticalValue.error.length")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "Statistical value contains non-digit characters" in {
        val itemType = buildItemType(statisticalValue = "123ABC")
        val expectedValidationResult = Invalid(
          errors = Seq(FormError(statisticalValueKey, "declaration.itemType.statisticalValue.error.wrongFormat"))
        )

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

    }

    "return Valid result" when {

      "provided with correct data with single value for every field" in {
        val itemType = ItemType(
          combinedNomenclatureCode = "12345678",
          taricAdditionalCodes = Seq("11AA"),
          nationalAdditionalCodes = Seq("12AB"),
          descriptionOfGoods = "Test description",
          cusCode = Some("12345678"),
          unDangerousGoodsCode = Some("1234"),
          statisticalValue = "1234567890.12"
        )

        ItemTypeValidator.validateOnSaveAndContinue(itemType) must be(Valid)
      }

      "provided with correct data with multiple values where possible" in {
        val itemType = ItemType(
          combinedNomenclatureCode = "12345678",
          taricAdditionalCodes = Seq("11AA", "22BB", "33CC"),
          nationalAdditionalCodes = Seq("12AB", "CD34"),
          descriptionOfGoods = "Test description",
          cusCode = Some("12345678"),
          unDangerousGoodsCode = Some("1234"),
          statisticalValue = "1234567890.12"
        )

        ItemTypeValidator.validateOnSaveAndContinue(itemType) must be(Valid)
      }

      "provided with correct data for mandatory fields only" in {
        val itemType = ItemType(
          combinedNomenclatureCode = "12345678",
          taricAdditionalCodes = Seq.empty,
          nationalAdditionalCodes = Seq.empty,
          descriptionOfGoods = "Test description",
          cusCode = None,
          unDangerousGoodsCode = None,
          statisticalValue = "1234567890.12"
        )

        ItemTypeValidator.validateOnSaveAndContinue(itemType) must be(Valid)
      }
    }

    def testFailedValidationOnSaveAndContinue(input: ItemType, expectedResult: Invalid): Unit =
      ItemTypeValidator.validateOnSaveAndContinue(input) match {
        case validationResult: Invalid => expectedResult.errors.foreach(validationResult.errors must contain(_))
        case Valid                     => fail()
      }
  }

}

object ItemTypeValidatorSpec {
  def buildItemType(
    combinedNomenclatureCode: String = "",
    taricAdditionalCode: Seq[String] = Seq.empty,
    nationalAdditionalCode: Seq[String] = Seq.empty,
    descriptionOfGoods: String = "",
    cusCode: Option[String] = None,
    unDangerousGoodsCode: Option[String] = None,
    statisticalValue: String = ""
  ): ItemType = ItemType(
    combinedNomenclatureCode = combinedNomenclatureCode,
    taricAdditionalCodes = taricAdditionalCode,
    nationalAdditionalCodes = nationalAdditionalCode,
    descriptionOfGoods = descriptionOfGoods,
    cusCode = cusCode,
    unDangerousGoodsCode = unDangerousGoodsCode,
    statisticalValue = statisticalValue
  )
}
