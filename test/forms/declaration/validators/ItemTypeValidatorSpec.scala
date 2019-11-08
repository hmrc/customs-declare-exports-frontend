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
import forms.declaration.ItemTypeForm._
import models.DeclarationType.DeclarationType
import models.declaration.ItemType
import models.requests.{AuthenticatedRequest, JourneyRequest}
import models.{DeclarationType, IdentityData, SignedInUser}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.cache.ExportsDeclarationBuilder
import uk.gov.hmrc.auth.core.Enrolments
import utils.validators.forms.supplementary.ItemTypeValidator
import utils.validators.forms.{Invalid, Valid}

class ItemTypeValidatorSpec extends WordSpec with MustMatchers with ExportsDeclarationBuilder {
  import ItemTypeValidatorSpec._

  "validateOnAddition" should {
    implicit val `type`: DeclarationType = DeclarationType.STANDARD

    "return Invalid result with errors" when {

      "any TARIC additional code is not 4 characters long" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333", "4444"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(s"$taricAdditionalCodeKey[2]", "declaration.itemType.taricAdditionalCodes.error.length")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "any TARIC additional code contains special characters" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333$", "4444"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(s"$taricAdditionalCodeKey[2]", "declaration.itemType.taricAdditionalCodes.error.specialCharacters")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is more than 99 TARIC additional codes" in {
        val itemType =
          buildItemType(taricAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomAlphanumericString(4)))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(taricAdditionalCodeKey, "declaration.itemType.taricAdditionalCodes.error.maxAmount")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is duplicated TARIC additional code" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(taricAdditionalCodeKey, "declaration.itemType.taricAdditionalCodes.error.duplicate")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "any National additional code not in list" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("VATE", "ABC"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(s"$nationalAdditionalCodeKey[1]", "declaration.itemType.nationalAdditionalCode.error.invalid")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is more than 99 National additional codes" in {
        val itemType =
          buildItemType(nationalAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomAlphanumericString(4)))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(nationalAdditionalCodeKey, "declaration.itemType.nationalAdditionalCode.error.maxAmount")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

      "there is duplicated National additional code" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("VATE", "VATE"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(nationalAdditionalCodeKey, "declaration.itemType.nationalAdditionalCode.error.duplicate")))

        testFailedValidationOnAddition(itemType, expectedValidationResult)
      }

    }

    "return Valid result" when {

      "Combined Nomenclature Code is none for Simplified Dec" in {
        val itemType = buildItemType(combinedNomenclatureCode = None)

        ItemTypeValidator.validateOnAddition(itemType)(req(DeclarationType.SIMPLIFIED)) must be(Valid)
      }

      "Combined Nomenclature Code is empty for Simplified Dec" in {
        val itemType = buildItemType(combinedNomenclatureCode = Some(""))

        ItemTypeValidator.validateOnAddition(itemType)(req(DeclarationType.SIMPLIFIED)) must be(Valid)
      }

      "provided with correct data with single value for multi-value fields" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("AB12"), nationalAdditionalCode = Seq("VATE"))

        ItemTypeValidator.validateOnAddition(itemType) must be(Valid)
      }

      "provided with correct data with multiple values for multi-value fields" in {
        val itemType =
          buildItemType(taricAdditionalCode = Seq("AB12", "Q123"), nationalAdditionalCode = Seq("VATE", "VATR"))

        ItemTypeValidator.validateOnAddition(itemType) must be(Valid)
      }

      "provided with empty mandatory fields and correct values for multi-value fields" in {
        val itemType =
          buildItemType(taricAdditionalCode = Seq("AB12", "Q123"), nationalAdditionalCode = Seq("VATE", "VATR"))

        ItemTypeValidator.validateOnAddition(itemType) must be(Valid)
      }

    }

    def testFailedValidationOnAddition(input: ItemType, expectedResult: Invalid): Unit =
      ItemTypeValidator.validateOnAddition(input) match {
        case validationResult: Invalid => expectedResult.errors.foreach(validationResult.errors must contain(_))
        case Valid                     => fail()
      }
  }

  "validateOnSaveAndContinue" should {
    implicit val `type`: DeclarationType = DeclarationType.STANDARD

    "return Failure result with errors" when {

      "any TARIC additional code is not 4 characters long" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333", "4444"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(s"$taricAdditionalCodeKey[2]", "declaration.itemType.taricAdditionalCodes.error.length")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "any TARIC additional code contains special characters" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "2222", "333$", "4444"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(s"$taricAdditionalCodeKey[2]", "declaration.itemType.taricAdditionalCodes.error.specialCharacters")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is more than 99 TARIC additional codes" in {
        val itemType =
          buildItemType(taricAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomAlphanumericString(4)))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(taricAdditionalCodeKey, "declaration.itemType.taricAdditionalCodes.error.maxAmount")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is duplicated TARIC additional code" in {
        val itemType = buildItemType(taricAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(taricAdditionalCodeKey, "declaration.itemType.taricAdditionalCodes.error.duplicate")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "any National additional code is not in list" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("VATE", "ABC"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(s"$nationalAdditionalCodeKey[1]", "declaration.itemType.nationalAdditionalCode.error.invalid")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is more than 99 National additional codes" in {
        val itemType =
          buildItemType(nationalAdditionalCode = (1 to 100).map(_ => TestHelper.createRandomAlphanumericString(4)))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(nationalAdditionalCodeKey, "declaration.itemType.nationalAdditionalCode.error.maxAmount")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

      "there is duplicated National additional code" in {
        val itemType = buildItemType(nationalAdditionalCode = Seq("1111", "1111"))
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(nationalAdditionalCodeKey, "declaration.itemType.nationalAdditionalCode.error.duplicate")))

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
        val expectedValidationResult =
          Invalid(errors = Seq(FormError(statisticalValueKey, "declaration.itemType.statisticalValue.error.wrongFormat")))

        testFailedValidationOnSaveAndContinue(itemType, expectedValidationResult)
      }

    }

    "return Valid result" when {
      implicit val `type`: models.DeclarationType.Value = DeclarationType.STANDARD

      "Combined Nomenclature Code is empty for Simplified Dec" in {
        val itemType = ItemType(
          taricAdditionalCodes = Seq("11AA"),
          nationalAdditionalCodes = Seq("VATE"),
          cusCode = Some("12345678"),
          statisticalValue = "1234567890.12"
        )

        ItemTypeValidator.validateOnSaveAndContinue(itemType)(req(DeclarationType.SIMPLIFIED)) must be(Valid)
      }

      "provided with correct data with single value for every field" in {
        val itemType = ItemType(
          taricAdditionalCodes = Seq("11AA"),
          nationalAdditionalCodes = Seq("VATE"),
          cusCode = Some("12345678"),
          statisticalValue = "1234567890.12"
        )

        ItemTypeValidator.validateOnSaveAndContinue(itemType) must be(Valid)
      }

      "provided with correct data with multiple values where possible" in {
        val itemType = ItemType(
          taricAdditionalCodes = Seq("11AA", "22BB", "33CC"),
          nationalAdditionalCodes = Seq("VATE", "VATR"),
          cusCode = Some("12345678"),
          statisticalValue = "1234567890.12"
        )

        ItemTypeValidator.validateOnSaveAndContinue(itemType) must be(Valid)
      }

      "provided with correct data for mandatory fields only" in {
        val itemType =
          ItemType(taricAdditionalCodes = Seq.empty, nationalAdditionalCodes = Seq.empty, cusCode = None, statisticalValue = "1234567890.12")

        ItemTypeValidator.validateOnSaveAndContinue(itemType) must be(Valid)
      }
    }

    def testFailedValidationOnSaveAndContinue(input: ItemType, expectedResult: Invalid)(implicit req: JourneyRequest[AnyContent]): Unit =
      ItemTypeValidator.validateOnSaveAndContinue(input) match {
        case validationResult: Invalid => expectedResult.errors.foreach(validationResult.errors must contain(_))
        case Valid                     => fail()
      }
  }

  private implicit def req(implicit `type`: DeclarationType): JourneyRequest[AnyContent] = new JourneyRequest[AnyContent](
    new AuthenticatedRequest[AnyContent](FakeRequest(), SignedInUser("eori", Enrolments(Set.empty), IdentityData())),
    aDeclaration(withType(`type`))
  )

}

object ItemTypeValidatorSpec {
  def buildItemType(
    combinedNomenclatureCode: Option[String] = None,
    taricAdditionalCode: Seq[String] = Seq.empty,
    nationalAdditionalCode: Seq[String] = Seq.empty,
    descriptionOfGoods: String = "",
    cusCode: Option[String] = None,
    unDangerousGoodsCode: Option[String] = None,
    statisticalValue: String = ""
  ): ItemType = ItemType(
    taricAdditionalCodes = taricAdditionalCode,
    nationalAdditionalCodes = nationalAdditionalCode,
    cusCode = cusCode,
    statisticalValue = statisticalValue
  )
}
