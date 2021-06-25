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

package models

import base.UnitSpec
import forms.declaration.declarationHolder.{AuthorizationTypeCodes, DeclarationHolder}
import models.declaration.{DeclarationHoldersData, ProcedureCodesData}
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import services.cache.ExportsDeclarationBuilder

import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class ExportsDeclarationSpec extends UnitSpec with ExportsDeclarationBuilder with OptionValues with MockitoSugar {

  "Amend" should {
    val currentTime = Instant.now()
    val clock = Clock.fixed(currentTime, ZoneOffset.UTC)

    "override required fields" in {
      val declaration =
        aDeclaration(withStatus(DeclarationStatus.COMPLETE), withCreatedDate(LocalDate.of(2019, 1, 1)), withUpdateDate(LocalDate.of(2019, 1, 1)))
      val amendedDeclaration = declaration.amend()(clock)
      amendedDeclaration.status mustBe DeclarationStatus.DRAFT
      amendedDeclaration.createdDateTime mustBe currentTime
      amendedDeclaration.updatedDateTime mustBe currentTime
      amendedDeclaration.sourceId.value mustBe declaration.id
    }
  }

  "Update Item" should {
    "preserve item sequence" in {

      val declaration = aDeclaration(withItems(2))

      declaration.items.map(_.sequenceId) must be(Seq(1, 2))

      val firstId = declaration.items.head.id

      val updatedDeclaration = declaration.updatedItem(firstId, item => item.copy(procedureCodes = Some(ProcedureCodesData(Some("code"), Seq.empty))))

      updatedDeclaration.items.map(_.sequenceId) must be(Seq(1, 2))
    }
  }

  "Exports Declaration" should {

    "return correct value for isDeclarantExports" when {

      "declarant is an exporter" in {

        val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL), withDeclarantIsExporter())

        declaration.isDeclarantExporter mustBe true
      }

      "declarant is not an exporter" in {

        val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL), withDeclarantIsExporter("No"))

        declaration.isDeclarantExporter mustBe false
      }

      "user didn't answer on this question" in {

        val declaration = aDeclaration(withType(DeclarationType.OCCASIONAL))

        declaration.isDeclarantExporter mustBe false
      }
    }
  }

  "ExportsDeclaration on isAdditionalDocumentationRequired" should {

    "return true" when {

      "only a single DeclarationHolder returns true when called with isAdditionalDocumentationRequired method" in {
        val declarationHolder = mock[DeclarationHolder]
        when(declarationHolder.isAdditionalDocumentationRequired).thenReturn(false)
        val declarationHolderRequiringAdditionalDocumentation = mock[DeclarationHolder]
        when(declarationHolderRequiringAdditionalDocumentation.isAdditionalDocumentationRequired).thenReturn(true)

        val declarationsHoldersData = DeclarationHoldersData(declarationHolderRequiringAdditionalDocumentation +: Seq.fill(13)(declarationHolder))

        val declaration = aDeclaration(withDeclarationHolders(declarationsHoldersData))

        declaration.isAdditionalDocumentationRequired mustBe true
      }

      "more than one DeclarationHolder returns true when called with isAdditionalDocumentationRequired method" in {
        val declarationHolder = mock[DeclarationHolder]
        when(declarationHolder.isAdditionalDocumentationRequired).thenReturn(false)
        val declarationHolderRequiringAdditionalDocumentation = mock[DeclarationHolder]
        when(declarationHolderRequiringAdditionalDocumentation.isAdditionalDocumentationRequired).thenReturn(true)

        val declarationsHoldersData =
          DeclarationHoldersData(Seq.fill(3)(declarationHolderRequiringAdditionalDocumentation) ++ Seq.fill(13)(declarationHolder))

        val declaration = aDeclaration(withDeclarationHolders(declarationsHoldersData))

        declaration.isAdditionalDocumentationRequired mustBe true
      }
    }

    "return false" when {

      "all DeclarationHolders return false when called with isAdditionalDocumentationRequired method" in {
        val declarationHolder = mock[DeclarationHolder]
        when(declarationHolder.isAdditionalDocumentationRequired).thenReturn(false)
        val declarationsHoldersData = DeclarationHoldersData(Seq.fill(13)(declarationHolder))

        val declaration = aDeclaration(withDeclarationHolders(declarationsHoldersData))

        declaration.isAdditionalDocumentationRequired mustBe false
      }
    }
  }
}
