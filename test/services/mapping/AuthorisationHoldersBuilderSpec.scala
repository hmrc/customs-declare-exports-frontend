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

package services.mapping
import forms.declaration.DeclarationHolder
import models.declaration.DeclarationHoldersData
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class AuthorisationHoldersBuilderSpec extends WordSpec with Matchers with MockitoSugar with ExportsCacheModelBuilder {

  "AuthorisationHolders" should {
    "correctly mapped to wco AuthorisationHolder" in {

      val decHolder1 =
        new DeclarationHolder(authorisationTypeCode = Some("decHolder1TypeCode"), eori = Some("decHolder1Eori"))

      val decHolder2 =
        new DeclarationHolder(authorisationTypeCode = Some("decHolder2TypeCode"), eori = Some("decHolder2Eori"))

      val declarationHoldersData = new DeclarationHoldersData(Seq(decHolder1, decHolder2))
      implicit val cacheMap = mock[CacheMap]
      when(cacheMap.getEntry[DeclarationHoldersData](eqTo(DeclarationHoldersData.formId))(any()))
        .thenReturn(Some(declarationHoldersData))

      val mappedAuthHolder = AuthorisationHoldersBuilder.build
      mappedAuthHolder.isEmpty shouldBe false
      mappedAuthHolder.size shouldBe declarationHoldersData.holders.size
      mappedAuthHolder.get(0).getCategoryCode.getValue should be("decHolder1TypeCode")
      mappedAuthHolder.get(0).getID.getValue should be("decHolder1Eori")

      mappedAuthHolder.get(1).getCategoryCode.getValue should be("decHolder2TypeCode")
      mappedAuthHolder.get(1).getID.getValue should be("decHolder2Eori")
    }

    "build and add to declaration" when {
      "no holders" in {
        // Given
        val model = aCacheModel(withoutDeclarationHolders())
        val declaration = new Declaration()

        // When
        AuthorisationHoldersBuilder.buildThenAdd(model, declaration)

        // Then
        declaration.getAuthorisationHolder shouldBe empty
      }

      "multiple holders" in {
        // Given
        val model = aCacheModel(
          withDeclarationHolder(Some("auth code1"), Some("eori1")),
          withDeclarationHolder(Some("auth code2"), Some("eori2"))
        )
        val declaration = new Declaration()

        // When
        AuthorisationHoldersBuilder.buildThenAdd(model, declaration)

        // Then
        declaration.getAuthorisationHolder should have(size(2))
        declaration.getAuthorisationHolder.get(0).getID.getValue shouldBe "eori1"
        declaration.getAuthorisationHolder.get(1).getID.getValue shouldBe "eori2"
        declaration.getAuthorisationHolder.get(0).getCategoryCode.getValue shouldBe "auth code1"
        declaration.getAuthorisationHolder.get(1).getCategoryCode.getValue shouldBe "auth code2"
      }

      "auth code is empty" in {
        // Given
        val model = aCacheModel(withDeclarationHolder(None, Some("eori")))
        val declaration = new Declaration()

        // When
        AuthorisationHoldersBuilder.buildThenAdd(model, declaration)

        // Then
        declaration.getAuthorisationHolder shouldBe empty
      }

      "eori is empty" in {
        // Given
        val model = aCacheModel(withDeclarationHolder(Some("auth code"), None))
        val declaration = new Declaration()

        // When
        AuthorisationHoldersBuilder.buildThenAdd(model, declaration)

        // Then
        declaration.getAuthorisationHolder shouldBe empty
      }
    }
  }

}
