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

package services.mapping.declaration.consignment
import forms.Choice.AllowedChoiceValues
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import services.mapping.goodsshipment.consignment.ConsignmentCarrierBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class DeclarationConsignmentBuilderSpec
    extends WordSpec with Matchers with MockitoSugar with BeforeAndAfterEach with ExportsDeclarationBuilder {

  private val freightBuilder = mock[FreightBuilder]
  private val iteneraryBuilder = mock[IteneraryBuilder]
  private val consignmentCarrierBuilder = mock[ConsignmentCarrierBuilder]

  private def builder = new DeclarationConsignmentBuilder(freightBuilder, iteneraryBuilder, consignmentCarrierBuilder)

  override def afterEach(): Unit =
    reset(freightBuilder, iteneraryBuilder, consignmentCarrierBuilder)

  "DeclarationConsignmentBuilder" should {

    "build then add" when {

      "standard journey" in {
        // Given
        val model = aDeclaration(withChoice(AllowedChoiceValues.StandardDec))
        val declaration = new Declaration()

        // When
        builder.buildThenAdd(model, declaration)

        // Then
        declaration.getConsignment should not be null
        verify(freightBuilder).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(iteneraryBuilder).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(consignmentCarrierBuilder).buildThenAdd(refEq(model), any[Declaration.Consignment])
      }

      "other journey" in {
        // Given
        val model = aDeclaration(withChoice("other"))
        val declaration = new Declaration()

        // When
        builder.buildThenAdd(model, declaration)

        // Then
        declaration.getConsignment shouldBe null
        verify(freightBuilder, never()).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(iteneraryBuilder, never()).buildThenAdd(refEq(model), any[Declaration.Consignment])
        verify(consignmentCarrierBuilder, never()).buildThenAdd(refEq(model), any[Declaration.Consignment])
      }
    }
  }

}
