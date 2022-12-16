/*
 * Copyright 2022 HM Revenue & Customs
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

import base.UnitWithMocksSpec
import config.AppConfig
import connectors.{FileBasedCodeListConnector, GoodsLocationCodesConnector}
import forms.declaration.declarationHolder.AuthorizationTypeCodes.{EXRR, MIB}
import forms.declaration.declarationHolder.DeclarationHolder
import mock.FeatureFlagMocks
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach

import java.util.Locale.ENGLISH

class GoodsLocationCodesSpec extends UnitWithMocksSpec with BeforeAndAfterEach with FeatureFlagMocks {

  private val appConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.glcCse16l)
      .thenReturn("/code-lists/goods-locations-codes/cse-16l-from-04-05-2022.json")
  }

  private lazy val glc = new GoodsLocationCodesConnector(appConfig)
  private lazy val codeListConnector = new FileBasedCodeListConnector(appConfig, glc)
  private lazy val goodsLocationCodes = new GoodsLocationCodes(codeListConnector)

  "GoodsLocationCodes.asListOfAutoCompleteItems" should {
    "return 'Goods Location Codes' from 'CSE' as AutoCompleteItems" in {

      val autoCompleteItems = goodsLocationCodes.asListOfAutoCompleteItems(ENGLISH)
      autoCompleteItems.size mustBe 66

      val item0 = "GBBUBELLCACSE - Belfast, Carne House, 20 Corry Place, Belfast, BT3 9HY"
      autoCompleteItems.head mustBe AutoCompleteItem(item0, "GBBUBELLCACSE")

      val lastItem = "GBBUSWALBCCSE - Swansea"
      autoCompleteItems.last mustBe AutoCompleteItem(lastItem, "GBBUSWALBCCSE")

    }
  }

}
