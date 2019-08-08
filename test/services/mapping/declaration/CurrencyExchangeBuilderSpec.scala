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

package services.mapping.declaration
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsDeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration

class CurrencyExchangeBuilderSpec extends WordSpec with Matchers with ExportsDeclarationBuilder {

  private def builder = new CurrencyExchangeBuilder()

  "CurrencyExchangeBuilder" should {

    "build then add" when {
      "no Total Number Of Items" in {
        // Given
        val model = aDeclaration(withoutTotalNumberOfItems())
        val declaration = new Declaration()
        // When
        builder.buildThenAdd(model, declaration)
        // Then
        declaration.getCurrencyExchange shouldBe empty
      }

      "exchange rate is empty" in {
        // Given
        val model = aDeclaration(withTotalNumberOfItems(exchangeRate = None))
        val declaration = new Declaration()
        // When
        builder.buildThenAdd(model, declaration)
        // Then
        declaration.getCurrencyExchange shouldBe empty
      }

      "exchange rate is populated" in {
        // Given
        val model = aDeclaration(withTotalNumberOfItems(exchangeRate = Some("123")))
        val declaration = new Declaration()
        // When
        builder.buildThenAdd(model, declaration)
        // Then
        declaration.getCurrencyExchange should have(size(1))
        declaration.getCurrencyExchange.get(0).getCurrencyTypeCode shouldBe null
        declaration.getCurrencyExchange.get(0).getRateNumeric.intValue() shouldBe 123
      }
    }
  }
}
