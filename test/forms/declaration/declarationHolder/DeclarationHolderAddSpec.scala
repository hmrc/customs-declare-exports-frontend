/*
 * Copyright 2021 HM Revenue & Customs
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

import base.JourneyTypeTestRunner
import forms.common.{DeclarationPageBaseSpec, Eori}
import models.declaration.ExportDeclarationTestData.correctDeclarationHolder

class DeclarationHolderAddSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  "DeclarationHolder mandatoryMapping" should {

    val mapping = DeclarationHolderAdd.mandatoryMapping

    "return no errors" when {

      "provided with all fields" in {

        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> "GB123456789012")

        mapping.bind(input).isRight mustBe true
      }
    }

    "return errors" when {

      "provided with empty input" in {

        val input = Map.empty[String, String]

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.eori.empty")
      }

      "provided with empty String values" in {

        val input = Map("authorisationTypeCode" -> "", "eori" -> "")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.eori.empty")
      }

      "provided with code only" in {

        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get)

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.eori.empty"
      }

      "provided with eori only" in {

        val input = Map("eori" -> "GB123456789012")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
      }

      "provided with no code" in {

        val input = Map("authorisationTypeCode" -> "", "eori" -> "GB123456789012")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
      }

      "provided with incorrect eori" in {

        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> "INCORRECT_EORI")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.eori.error.format"
      }

      "provided with no code and no eori" in {

        val input = Map("authorisationTypeCode" -> "", "eori" -> "")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.eori.empty")
      }

      "provided with no code and incorrect eori" in {

        val input = Map("eori" -> "INCORRECT_EORI")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.eori.error.format")
      }
    }
  }

  "DeclarationHolder on fromId method" should {

    "return DeclarationHolder with both fields empty" when {

      val expectedResult = DeclarationHolderAdd(None, None)

      "provided with empty String" in {
        val input = ""
        DeclarationHolderAdd.fromId(input) mustBe expectedResult
      }

      "provided with String containing only '-' character" in {
        val input = "-"
        DeclarationHolderAdd.fromId(input) mustBe expectedResult
      }
    }

    "return DeclarationHolder with authorisationTypeCode only" when {

      val authorisationTypeCode = "ACE"
      val expectedResult = DeclarationHolderAdd(Some(authorisationTypeCode), None)

      "provided with String containing NO '-' character" in {
        val input = s"${authorisationTypeCode}"
        DeclarationHolderAdd.fromId(input) mustBe expectedResult
      }

      "provided with String containing '-' as the last character" in {
        val input = s"${authorisationTypeCode}-"
        DeclarationHolderAdd.fromId(input) mustBe expectedResult
      }
    }

    "return DeclarationHolder with both fields populated" when {

      val authorisationTypeCode = "ACE"
      val eori = "PL213472539481923"
      val expectedResult = DeclarationHolderAdd(Some(authorisationTypeCode), Some(Eori(eori)))

      "provided with String containing '-' character in the middle" in {
        val input = s"${authorisationTypeCode}-${eori}"
        DeclarationHolderAdd.fromId(input) mustBe expectedResult
      }

      "provided with String containing more than one '-' character" in {
        val input = s"${authorisationTypeCode}-${eori}-this_part_does_not_matter"
        DeclarationHolderAdd.fromId(input) mustBe expectedResult
      }
    }
  }

  "DeclarationHolder on requireAdditionalDocumentation" should {

    "return true" when {

      AuthorizationTypeCodes.CodesRequiringDocumentation.foreach { code =>
        s"authorisationTypeCode contains $code code" in {

          DeclarationHolderAdd(Some(code), None).isAdditionalDocumentationRequired mustBe true
        }
      }
    }

    "return false" when {

      "authorisationTypeCode contains code that is NOT present in AuthorizationTypeCodes.CodesRequiringDocumentation" in {
        val code = "AWR"
        DeclarationHolderAdd(Some(code), None).isAdditionalDocumentationRequired mustBe false
      }
    }
  }

  "DeclarationHolder" when {
    testTariffContentKeys(DeclarationHolderAdd, "tariff.declaration.addAuthorisationRequired")
  }

  "DeclarationHolderRequired" when {
    testTariffContentKeys(DeclarationHolderRequired, "tariff.declaration.isAuthorisationRequired")
  }
}
