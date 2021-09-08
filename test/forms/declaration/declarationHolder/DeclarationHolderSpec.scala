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

import base.ExportsTestData._
import base.JourneyTypeTestRunner
import forms.common.{DeclarationPageBaseSpec, Eori}
import models.declaration.EoriSource
import models.declaration.ExportDeclarationTestData.correctDeclarationHolder

class DeclarationHolderSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner {

  private val eoriSource = EoriSource.OtherEori.toString

  "DeclarationHolder mandatoryMapping" should {

    val mapping = DeclarationHolder.mapping(eori)

    "mapping.bind return no errors" when {
      "provided with all fields" in {
        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> eori, "eoriSource" -> eoriSource)

        mapping.bind(input).isRight mustBe true
      }

      "provided with a authorisationTypeCode and eoriSource of 'UserEori' but no eori value, set eori value to users eori" in {
        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eoriSource" -> EoriSource.UserEori.toString)

        val boundForm = mapping.bind(input)

        boundForm.isRight mustBe true
        boundForm.right.get.authorisationTypeCode mustBe Some(correctDeclarationHolder.authorisationTypeCode.get)
        boundForm.right.get.eori mustBe Some(Eori(eori))
        boundForm.right.get.eoriSource mustBe Some(EoriSource.UserEori)
      }
    }

    "mapping.bind return errors" when {
      "provided with no values" in {
        val input = Map.empty[String, String]
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.declarationHolder.eori.error.radio")
      }

      "provided with empty String values" in {
        val input = Map("authorisationTypeCode" -> "", "eori" -> "", "eoriSource" -> "")
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.declarationHolder.eori.error.radio")
      }

      "provided with code only" in {
        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.eori.error.radio"
      }

      "provided with eori only" in {
        val input = Map("eori" -> eori)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
        errors.last.message mustBe "declaration.declarationHolder.eori.error.radio"
      }

      "provided with eoriSource only" in {
        val input = Map("eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
        errors.last.message mustBe "error.required"
      }

      "provided with a eoriSource and eori but no authorisationTypeCode value" in {
        val input = Map("eori" -> eori, "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.empty"
      }

      "provided with a authorisationTypeCode and eoriSource of 'OtherEori' but no eori value" in {
        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "error.required"
      }

      "provided with a authorisationTypeCode and eori but no eoriSource value" in {
        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> eori)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.eori.error.radio"
      }

      "provided with incorrect eori" in {
        val input =
          Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> "INCORRECT_EORI", "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.eori.error.format"
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
      AuthorizationTypeCodes.CodesRequiringDocumentation.foreach { code =>
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
}
