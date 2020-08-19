/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.common.Eori
import models.DeclarationType._
import models.declaration.ExportDeclarationTestData
import models.declaration.ExportDeclarationTestData.correctDeclarationHolder
import org.scalatest.{MustMatchers, WordSpec}
import unit.base.JourneyTypeTestRunner

class DeclarationHolderSpec extends WordSpec with MustMatchers with JourneyTypeTestRunner {

  "DeclarationHolder optionalMapping" should {

    val mapping = DeclarationHolder.optionalMapping

    "return no errors" when {

      "provided with empty input" in {

        val input = Map.empty[String, String]

        mapping.bind(input).isRight mustBe true
      }

      "provided with empty String values" in {

        val input = Map("authorisationTypeCode" -> "", "eori" -> "")

        val result = mapping.bind(input)
        result.isRight mustBe true
      }

      "provided with all fields" in {

        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> "GB123456789012")

        mapping.bind(input).isRight mustBe true
      }
    }

    "return errors" when {

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

      "provided with incorrect code" in {

        val input = Map("authorisationTypeCode" -> "INCORRECT_CODE", "eori" -> "GB123456789012")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.invalid"
      }

      "provided with incorrect eori" in {

        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> "INCORRECT_EORI")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.eori.error.format"
      }

      "provided with incorrect code and no eori" in {

        val input = Map("authorisationTypeCode" -> "INCORRECT_CODE")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.invalid")
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

  "DeclarationHolder mandatoryMapping" should {

    val mapping = DeclarationHolder.mandatoryMapping

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

      "provided with incorrect code" in {

        val input = Map("authorisationTypeCode" -> "INCORRECT_CODE", "eori" -> "GB123456789012")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.declarationHolder.authorisationCode.invalid"
      }

      "provided with incorrect eori" in {

        val input = Map("authorisationTypeCode" -> correctDeclarationHolder.authorisationTypeCode.get, "eori" -> "INCORRECT_EORI")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 1
        errors.head.message mustBe "declaration.eori.error.format"
      }

      "provided with incorrect code and no eori" in {

        val input = Map("authorisationTypeCode" -> "INCORRECT_CODE")

        val result = mapping.bind(input)

        result.isLeft mustBe true
        val errors = result.left.get
        errors.size mustBe 2
        errors.map(_.message) must contain("declaration.declarationHolder.authorisationCode.invalid")
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

  "DeclarationHolder on form method" when {

    onSimplified { implicit request =>
      "provided with empty holders list" should {
        "return mandatoryMapping" in {

          val holders = Seq.empty[DeclarationHolder]

          val result = DeclarationHolder.form(holders)

          result.mapping mustBe DeclarationHolder.mandatoryMapping
        }
      }

      "provided with non-empty holders list" should {
        "return mandatoryMapping" in {

          val holders = Seq(ExportDeclarationTestData.correctDeclarationHolder)

          val result = DeclarationHolder.form(holders)

          result.mapping mustBe DeclarationHolder.mandatoryMapping
        }
      }
    }

    onJourney(STANDARD, SUPPLEMENTARY, OCCASIONAL, CLEARANCE) { implicit request =>
      "provided with empty holders list" should {
        "return optionalMapping" in {

          val holders = Seq.empty[DeclarationHolder]

          val result = DeclarationHolder.form(holders)

          result.mapping mustBe DeclarationHolder.optionalMapping
        }
      }

      "provided with non-empty holders list" should {
        "return mandatoryMapping" in {

          val holders = Seq(ExportDeclarationTestData.correctDeclarationHolder)

          val result = DeclarationHolder.form(holders)

          result.mapping mustBe DeclarationHolder.mandatoryMapping
        }
      }
    }
  }

  "DeclarationHolder on fromId method" should {

    "return DeclarationHolder with both fields empty" when {

      val expectedResult = DeclarationHolder(None, None)

      "provided with empty String" in {

        val input = ""

        DeclarationHolder.fromId(input) mustBe expectedResult
      }

      "provided with String containing only '-' character" in {

        val input = "-"

        DeclarationHolder.fromId(input) mustBe expectedResult
      }
    }

    "return DeclarationHolder with authorisationTypeCode only" when {

      val authorisationTypeCode = "ACE"
      val expectedResult = DeclarationHolder(Some(authorisationTypeCode), None)

      "provided with String containing NO '-' character" in {

        val input = s"${authorisationTypeCode}"

        DeclarationHolder.fromId(input) mustBe expectedResult
      }

      "provided with String containing '-' as the last character" in {

        val input = s"${authorisationTypeCode}-"

        DeclarationHolder.fromId(input) mustBe expectedResult
      }
    }

    "return DeclarationHolder with both fields populated" when {

      val authorisationTypeCode = "ACE"
      val eori = "PL213472539481923"
      val expectedResult = DeclarationHolder(Some(authorisationTypeCode), Some(Eori(eori)))

      "provided with String containing '-' character in the middle" in {

        val input = s"${authorisationTypeCode}-${eori}"

        DeclarationHolder.fromId(input) mustBe expectedResult
      }

      "provided with String containing more than one '-' character" in {

        val input = s"${authorisationTypeCode}-${eori}-this_part_does_not_matter"

        DeclarationHolder.fromId(input) mustBe expectedResult
      }
    }
  }

}
