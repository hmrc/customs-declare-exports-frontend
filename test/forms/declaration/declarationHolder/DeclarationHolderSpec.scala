/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.declaration.declarationHolder

import base.ExportsTestData._
import base.JourneyTypeTestRunner
import forms.common.{DeclarationPageBaseSpec, Eori}
import forms.declaration.additionaldeclarationtype.AdditionalDeclarationType.{preLodgedTypes, STANDARD_PRE_LODGED}
import forms.declaration.declarationHolder.AuthorizationTypeCodes.{CSE, EXRR}
import forms.declaration.declarationHolder.DeclarationHolder._
import models.declaration.EoriSource
import models.declaration.ExportDeclarationTestData.correctDeclarationHolder
import org.scalatest.Inspectors.forAll
import play.api.data.FormError

class DeclarationHolderSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  private val eoriSource = EoriSource.OtherEori.toString
  private val authorisationTypeCode = correctDeclarationHolder.authorisationTypeCode.get

  "DeclarationHolder mandatoryMapping" should {

    val mapping = DeclarationHolder.mapping(eori, Some(STANDARD_PRE_LODGED))

    "mapping.bind return no errors" when {
      "provided with all fields" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode, "eori" -> eori, "eoriSource" -> eoriSource)
        mapping.bind(input).isRight mustBe true
      }

      "provided with a authorisationTypeCode and eoriSource of 'UserEori' but no eori value, set eori value to users eori" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode, "eoriSource" -> EoriSource.UserEori.toString)
        val boundForm = mapping.bind(input)

        boundForm.isRight mustBe true
        boundForm.toOption.get.authorisationTypeCode mustBe Some(authorisationTypeCode)
        boundForm.toOption.get.eori mustBe Some(Eori(eori))
        boundForm.toOption.get.eoriSource mustBe Some(EoriSource.UserEori)
      }
    }

    "mapping.bind return errors" when {

      "provided with no values" in {
        val input = Map.empty[String, String]
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.declarationHolder.eori.error.radio")
      }

      "provided with empty String values" in {
        val input = Map("authorisationTypeCode" -> "", "eori" -> "", "eoriSource" -> "")
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.declarationHolder.eori.error.radio")
      }

      "provided with code only" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.eori.error.radio"
      }

      "provided with eori only" in {
        val input = Map("eori" -> eori)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
        errors.last.message mustBe "declaration.declarationHolder.eori.error.radio"
      }

      "provided with eoriSource only" in {
        val input = Map("eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
        errors.last.message mustBe "error.required"
      }

      "provided with a eoriSource and eori but no authorisationTypeCode value" in {
        val input = Map("eori" -> eori, "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
      }

      "provided with a authorisationTypeCode and eoriSource of 'OtherEori' but no eori value" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode, "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "error.required"
      }

      "provided with a authorisationTypeCode and eori but no eoriSource value" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode, "eori" -> eori)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.eori.error.radio"
      }

      "provided with incorrect eori" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode, "eori" -> "INCORRECT_EORI", "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.eori.error.format"
      }

      "provided with 'EXRR' as authorisationTypeCode with a pre-lodged declaration" in {
        val input = Map("authorisationTypeCode" -> EXRR, "eori" -> eori, "eoriSource" -> eoriSource)
        forAll(preLodgedTypes) { additionalDeclarationType =>
          val result = DeclarationHolder.mapping(eori, Some(additionalDeclarationType)).bind(input)

          result.isLeft mustBe true
          val errors = result.left.toOption.get
          errors.size mustBe 1
          errors.head.message mustBe "declaration.declarationHolder.EXRR.error.prelodged"
        }
      }
    }

    "mapping.unbind returns expected eoriSource value" when {
      val populatedDecHolder =
        DeclarationHolder(authorisationTypeCode = Some("ACE"), eori = Some(Eori(eori)), Some(EoriSource.UserEori))

      "DeclarationHolder's eoriSource and eori fields are present" in {
        val result = mapping
          .unbind(populatedDecHolder)
          .get("eoriSource")

        result.isDefined mustBe true
        result.get mustBe "UserEori"
      }

      "DeclarationHolder's eoriSource field is None but eori is present" in {
        val result = mapping
          .unbind(populatedDecHolder.copy(eoriSource = None))
          .get("eoriSource")

        result.isDefined mustBe true
        result.get mustBe "OtherEori"
      }

      "DeclarationHolder's eoriSource field is None and eori is not present" in {
        val result = mapping
          .unbind(populatedDecHolder.copy(eoriSource = None, eori = None))
          .get("eoriSource")

        result.isDefined mustBe true
        result.get mustBe ""
      }
    }
  }

  "DeclarationHolder on requireAdditionalDocumentation" should {

    "return true" when {
      AuthorizationTypeCodes.codesRequiringDocumentation.foreach { code =>
        s"authorisationTypeCode contains $code code" in {
          DeclarationHolder(Some(code), None, None).isAdditionalDocumentationRequired mustBe true
        }
      }
    }

    "return false" when {
      "authorisationTypeCode contains code that is NOT present in AuthorizationTypeCodes.CodesRequiringDocumentation" in {
        val code = "AWR"
        DeclarationHolder(Some(code), None, None).isAdditionalDocumentationRequired mustBe false
      }
    }
  }

  "DeclarationHolder" when {
    testTariffContentKeys(DeclarationHolder, "tariff.declaration.addAuthorisationRequired")
  }

  "DeclarationHolderRequired" when {
    testTariffContentKeys(DeclarationHolderRequired, "tariff.declaration.isAuthorisationRequired")
  }

  "DeclarationHolder.validateMutuallyExclusiveAuthCodes" when {
    def holder(code: String) = DeclarationHolder(Some(code), None, None)
    def error(code: String) = FormError(AuthorisationTypeCodeId, s"declaration.declarationHolder.${code}.error.exclusive")

    "the user enters a new 'CSE' authorisation and the cache already includes an 'EXRR' one" should {
      "return a FormError" in {
        val result = validateMutuallyExclusiveAuthCodes(Some(holder(CSE)), List(holder(EXRR)))
        result.get mustBe error(CSE)
      }
    }

    "the user enters a new 'EXRR' authorisation and the cache already includes a 'CSE' one" should {
      "return a FormError" in {
        val result = validateMutuallyExclusiveAuthCodes(Some(holder(EXRR)), List(holder(CSE)))
        result.get mustBe error(EXRR)
      }
    }

    "the user does not enter an authorisation code" should {
      "return None" in {
        validateMutuallyExclusiveAuthCodes(None, List(holder(CSE))) mustBe None
        validateMutuallyExclusiveAuthCodes(None, List(holder(EXRR))) mustBe None
      }
    }
  }
}
