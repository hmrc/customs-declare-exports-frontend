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

package forms.section2.authorisationHolder

import base.ExportsTestData.eori
import base.{JourneyTypeTestRunner, MockTaggedCodes}
import forms.common.{DeclarationPageBaseSpec, Eori}
import forms.section1.AdditionalDeclarationType.{preLodgedTypes, STANDARD_PRE_LODGED}
import forms.section2.authorisationHolder.AuthorizationTypeCodes.EXRR
import models.declaration.EoriSource
import models.declaration.ExportDeclarationTestData.correctAuthorisationHolder
import models.viewmodels.TariffContentKey
import org.scalatest.Inspectors.forAll

class AuthorisationHolderSpec extends DeclarationPageBaseSpec with JourneyTypeTestRunner with MockTaggedCodes {

  override def getCommonTariffKeys(messageKey: String): Seq[TariffContentKey] =
    List(TariffContentKey(s"${messageKey}.common"))

  private val eoriSource = EoriSource.OtherEori.toString
  private val authorisationTypeCode = correctAuthorisationHolder.authorisationTypeCode.get

  "AuthorisationHolder mandatoryMapping" should {

    val mapping = AuthorisationHolder.mapping(eori, Some(STANDARD_PRE_LODGED))

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
        errors.map(_.message) must contain("declaration.authorisationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.authorisationHolder.eori.error.radio")
      }

      "provided with empty String values" in {
        val input = Map("authorisationTypeCode" -> "", "eori" -> "", "eoriSource" -> "")
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.authorisationHolder.authorisationCode.empty")
        errors.map(_.message) must contain("declaration.authorisationHolder.eori.error.radio")
      }

      "provided with code only" in {
        val input = Map("authorisationTypeCode" -> authorisationTypeCode)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.authorisationHolder.eori.error.radio"
      }

      "provided with eori only" in {
        val input = Map("eori" -> eori)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.head.message mustBe "declaration.authorisationHolder.authorisationCode.empty"
        errors.last.message mustBe "declaration.authorisationHolder.eori.error.radio"
      }

      "provided with eoriSource only" in {
        val input = Map("eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 2
        errors.head.message mustBe "declaration.authorisationHolder.authorisationCode.empty"
        errors.last.message mustBe "error.required"
      }

      "provided with a eoriSource and eori but no authorisationTypeCode value" in {
        val input = Map("eori" -> eori, "eoriSource" -> eoriSource)
        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.toOption.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.authorisationHolder.authorisationCode.empty"
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
        errors.head.message mustBe "declaration.authorisationHolder.eori.error.radio"
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
          val result = AuthorisationHolder.mapping(eori, Some(additionalDeclarationType)).bind(input)

          result.isLeft mustBe true
          val errors = result.left.toOption.get
          errors.size mustBe 1
          errors.head.message mustBe "declaration.authorisationHolder.EXRR.error.prelodged"
        }
      }
    }

    "mapping.unbind returns expected eoriSource value" when {
      val populatedDecHolder =
        AuthorisationHolder(authorisationTypeCode = Some("ACE"), eori = Some(Eori(eori)), Some(EoriSource.UserEori))

      "AuthorisationHolder's eoriSource and eori fields are present" in {
        val result = mapping
          .unbind(populatedDecHolder)
          .get("eoriSource")

        result.isDefined mustBe true
        result.get mustBe "UserEori"
      }

      "AuthorisationHolder's eoriSource field is None but eori is present" in {
        val result = mapping
          .unbind(populatedDecHolder.copy(eoriSource = None))
          .get("eoriSource")

        result.isDefined mustBe true
        result.get mustBe "OtherEori"
      }

      "AuthorisationHolder's eoriSource field is None and eori is not present" in {
        val result = mapping
          .unbind(populatedDecHolder.copy(eoriSource = None, eori = None))
          .get("eoriSource")

        result.isDefined mustBe true
        result.get mustBe ""
      }
    }
  }

  "AuthorisationHolder on requireAdditionalDocumentation" should {

    "return true" when {
      "authorisationTypeCode is equal to one of the expected Authorisation codes" in {
        taggedAuthCodes.codesRequiringDocumentation.foreach { code =>
          taggedAuthCodes.isAdditionalDocumentationRequired(AuthorisationHolder(Some(code), None, None)) mustBe true
        }
      }
    }

    "return false" when {
      "authorisationTypeCode contains code that is NOT present in AuthorizationTypeCodes.CodesRequiringDocumentation" in {
        val code = "AWR"
        taggedAuthCodes.isAdditionalDocumentationRequired(AuthorisationHolder(Some(code), None, None)) mustBe false
      }
    }
  }

  "AuthorisationHolder" when {
    testTariffContentKeys(AuthorisationHolder, "tariff.declaration.addAuthorisationRequired")
  }

  "AuthorisationHolderRequired" when {
    testTariffContentKeys(AuthorisationHolderRequired, "tariff.declaration.isAuthorisationRequired")
  }
  override def getClearanceTariffKeys(messageKey: String): Seq[TariffContentKey] =
    Seq(TariffContentKey(s"${messageKey}.clearance"))
}
