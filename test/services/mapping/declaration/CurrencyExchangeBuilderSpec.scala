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
import forms.declaration.{TotalNumberOfItems, TotalNumberOfItemsSpec}
import org.scalatest.{Matchers, WordSpec}
import services.cache.ExportsCacheModelBuilder
import uk.gov.hmrc.http.cache.client.CacheMap
import wco.datamodel.wco.dec_dms._2.Declaration

class CurrencyExchangeBuilderSpec extends WordSpec with Matchers with ExportsCacheModelBuilder {

  "CurrencyExchangeBuilder" should {
    "correctly map from CacheModel to the WCO-DEC CurrencyExchange instance" in {
      implicit val cacheMap: CacheMap =
        CacheMap(
          "CacheID",
          Map(TotalNumberOfItems.formId -> TotalNumberOfItemsSpec.correctTotalNumberOfItemsDecimalValuesJSON)
        )
      val currencyExchangeType = CurrencyExchangeBuilder.build(cacheMap)
      currencyExchangeType.get(0).getRateNumeric.doubleValue() should be(1212121.12345)
    }

    "build then add" when {
      "no Total Number Of Items" in {
        // Given
        val model = aCacheModel(
          withoutTotalNumberOfItems()
        )
        val declaration = new Declaration()
        // When
        CurrencyExchangeBuilder.buildThenAdd(model, declaration)
        // Then
        declaration.getCurrencyExchange shouldBe empty
      }

      "exchange rate is empty" in {
        // Given
        val model = aCacheModel(
          withTotalNumberOfItems(exchangeRate = None)
        )
        val declaration = new Declaration()
        // When
        CurrencyExchangeBuilder.buildThenAdd(model, declaration)
        // Then
        declaration.getCurrencyExchange shouldBe empty
      }

      "exchange rate is populated" in {
        // Given
        val model = aCacheModel(
          withTotalNumberOfItems(exchangeRate = Some("123"))
        )
        val declaration = new Declaration()
        // When
        CurrencyExchangeBuilder.buildThenAdd(model, declaration)
        // Then
        declaration.getCurrencyExchange should have(size(1))
        declaration.getCurrencyExchange.get(0).getCurrencyTypeCode shouldBe null
        declaration.getCurrencyExchange.get(0).getRateNumeric.intValue() shouldBe 123
      }
    }
  }
}
