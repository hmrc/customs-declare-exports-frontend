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

package services.view

import mock.FeatureFlagMocks
import models.codes.GoodsLocationCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import services.GoodsLocationCodesService
import views.declaration.spec.UnitViewSpec

class GoodsLocationCodesSpec extends UnitViewSpec with BeforeAndAfterEach with FeatureFlagMocks {

  private lazy val glc = mock[GoodsLocationCodesService]
  private lazy val goodsLocationCodes = new GoodsLocationCodes(glc)

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(glc.all(any())).thenReturn(List(GoodsLocationCode("GBCUADLMNCPDP", "Adlington")))
    when(glc.cseCodes(any())).thenReturn(List(GoodsLocationCode("GBBUABDLEACSE", "Aberdeen")))

  }

  "GoodsLocationCodes.asListOfAutoCompleteItems" when {
    "content version is 7 for cse items" should {
      "return 'Goods Location Codes' from 'CSE' as AutoCompleteItems" in {

        val autoCompleteItems = goodsLocationCodes.asListOfAutoCompleteItems(7)

        autoCompleteItems.head mustBe AutoCompleteItem("GBBUABDLEACSE - Aberdeen", "GBBUABDLEACSE")

      }
    }
    "content version is not 7" should {
      "return 'Goods Location Codes' from 'CSE' as AutoCompleteItems" in {

        val autoCompleteItems = goodsLocationCodes.asListOfAutoCompleteItems(1)

        autoCompleteItems.head mustBe AutoCompleteItem("GBCUADLMNCPDP - Adlington", "GBCUADLMNCPDP")

      }
    }
  }

}
